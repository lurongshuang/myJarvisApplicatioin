package snow.music.util.http;// Created byjinengmao

import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

// on 2023/4/6
// Description：
public class RequestUtil {
    /*
    请求方式，目前只支持get和post
     */
    private final String mMetyodType;
    //接口
    private String mUrl;
    /*
    键值对类型的参数，只有这一种情况下区分post和get。
     */
    private final Map<String, String> mParamsMap;
    /*
    json类型的参数，post方式
     */
    private final String mJsonStr;
    //文件的参数，post方式,只有一个文件
    private final File mFile;
    //文件集合，这个集合对应一个key，即mfileKey
    private final List<File> mfileList;
    //上传服务器的文件对应的key
    private final String mfileKey;
    //文件集合，每个文件对应一个key
    private final Map<String, File> mfileMap;
    //文件类型的参数，与file同时存在
    private final String mFileType;
    //头参数
    private final Map<String, String> mHeaderMap;
    //回调接口
    private final CallBackUtil mCallBack;
    //OKhttpClient对象
    private OkHttpClient mOkHttpClient;
    //请求对象
    private Request mOkHttpRequest;
    //请求对象的构建者
    private Request.Builder mRequestBuilder;


    RequestUtil(String methodType, String url, Map<String, String> paramsMap, Map<String, String> headerMap, CallBackUtil callBack) {
        this(methodType, url, null, null, null, null, null, null, paramsMap, headerMap, callBack);
    }

    RequestUtil(String url, String jsonStr, Map<String, String> headerMap, CallBackUtil callBack) {
        this(HyrcHttpUtil.METHOD_POST, url, jsonStr, null, null, null, null, null, null, headerMap, callBack);
    }

    RequestUtil(String url, Map<String, String> paramsMap, File file, String fileKey, String fileType, Map<String, String> headerMap, CallBackUtil callBack) {
        this(HyrcHttpUtil.METHOD_POST, url, null, file, null, fileKey, null, fileType, paramsMap, headerMap, callBack);
    }

    RequestUtil(String url, Map<String, String> paramsMap, List<File> fileList, String fileKey, String fileType, Map<String, String> headerMap, CallBackUtil callBack) {
        this(HyrcHttpUtil.METHOD_POST, url, null, null, fileList, fileKey, null, fileType, paramsMap, headerMap, callBack);
    }

    RequestUtil(String url, Map<String, String> paramsMap, Map<String, File> fileMap, String fileType, Map<String, String> headerMap, CallBackUtil callBack) {
        this(HyrcHttpUtil.METHOD_POST, url, null, null, null, null, fileMap, fileType, paramsMap, headerMap, callBack);
    }

    private RequestUtil(String methodType, String url, String jsonStr, File file, List<File> fileList, String fileKey, Map<String, File> fileMap, String fileType, Map<String, String> paramsMap, Map<String, String> headerMap, CallBackUtil callBack) {
        mMetyodType = methodType;
        mUrl = url;
        mJsonStr = jsonStr;
        mFile = file;
        mfileList = fileList;
        mfileKey = fileKey;
        mfileMap = fileMap;
        mFileType = fileType;
        mParamsMap = paramsMap;
        mHeaderMap = headerMap;
        mCallBack = callBack;
        getInstance();
    }


    /**
     * 创建OKhttpClient实例。
     */
    private void getInstance() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.connectTimeout(60, TimeUnit.SECONDS);
        builder.callTimeout(60, TimeUnit.SECONDS);
        builder.readTimeout(60, TimeUnit.SECONDS);
        mOkHttpClient = builder.build();
        mRequestBuilder = new Request.Builder();
        if (mFile != null || mfileList != null || mfileMap != null) {//先判断是否有文件，
            setFile();
        } else {
            //设置参数
            switch (mMetyodType) {
                case HyrcHttpUtil.METHOD_GET:
                    setGetParams();
                    break;
                case HyrcHttpUtil.METHOD_POST:
                    mRequestBuilder.post(getRequestBody());
                    break;
                case HyrcHttpUtil.METHOD_PUT:
                    mRequestBuilder.put(getRequestBody());
                    break;
                case HyrcHttpUtil.METHOD_DELETE:
                    mRequestBuilder.delete(getRequestBody());
                    break;
            }
        }
        mRequestBuilder.url(mUrl);
        if (mHeaderMap != null) {
            setHeader();
        }
        //mRequestBuilder.addHeader("Authorization","Bearer "+"token");可以把token添加到这儿
        mOkHttpRequest = mRequestBuilder.build();
    }

    /**
     * 得到body对象
     */
    private RequestBody getRequestBody() {
        /**
         * 首先判断mJsonStr是否为空，由于mJsonStr与mParamsMap不可能同时存在，所以先判断mJsonStr
         */
        if (!TextUtils.isEmpty(mJsonStr)) {
//
//            MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，
//            return RequestBody.create(JSON, mJsonStr);//json数据，

//            MediaType mediaType = MediaType.Companion.parse("application/json;charset=utf-8");
            MediaType mediaType = MediaType.Companion.parse("application/json");
            RequestBody requestBody = RequestBody.Companion.create(mJsonStr, mediaType);
            return requestBody;
        }

        /**
         * post,put,delete都需要body，但也都有body等于空的情况，此时也应该有body对象，但body中的内容为空
         */
        FormBody.Builder formBody = new FormBody.Builder();
        if (mParamsMap != null) {
            for (String key : mParamsMap.keySet()) {

                formBody.add(key, mParamsMap.get(key));
            }
        }
        return formBody.build();
    }


    /**
     * get请求，只有键值对参数
     */
    private void setGetParams() {
        if (mParamsMap != null) {
            mUrl = mUrl + "?";
            for (String key : mParamsMap.keySet()) {
                mUrl = mUrl + key + "=" + mParamsMap.get(key) + "&";
            }
            mUrl = mUrl.substring(0, mUrl.length() - 1);
        }
    }


    /**
     * 设置上传文件
     */
    private void setFile() {
        if (mFile != null) {//只有一个文件，且没有文件名
            if (mParamsMap == null) {
                setPostFile();
            } else {
                setPostParameAndFile();
            }
        } else if (mfileList != null) {//文件集合，只有一个文件名。所以这个也支持单个有文件名的文件
            setPostParameAndListFile();
        } else if (mfileMap != null) {//多个文件，每个文件对应一个文件名
            setPostParameAndMapFile();
        }

    }

    /**
     * 只有一个文件，且提交服务器时不用指定键，没有参数
     */
    private void setPostFile() {
        if (mFile != null && mFile.exists()) {
            MediaType fileType = MediaType.parse(mFileType);
            RequestBody body = RequestBody.create(fileType, mFile);//json数据，
            mRequestBuilder.post(new ProgressRequestBody(body, mCallBack));
        }
    }

    /**
     * 只有一个文件，且提交服务器时不用指定键，带键值对参数
     */
    private void setPostParameAndFile() {
        if (mParamsMap != null && mFile != null) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            for (String key : mParamsMap.keySet()) {
                builder.addFormDataPart(key, mParamsMap.get(key));
            }
            builder.addFormDataPart(mfileKey, mFile.getName(), RequestBody.create(MediaType.parse(mFileType), mFile));
            mRequestBuilder.post(new ProgressRequestBody(builder.build(), mCallBack));
        }
    }

    /**
     * 文件集合，可能带有键值对参数
     */
    private void setPostParameAndListFile() {
        if (mfileList != null) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            if (mParamsMap != null) {
                for (String key : mParamsMap.keySet()) {
                    builder.addFormDataPart(key, mParamsMap.get(key));
                }
            }
            for (File f : mfileList) {
                builder.addFormDataPart(mfileKey, f.getName(), RequestBody.create(MediaType.parse(mFileType), f));
            }
            mRequestBuilder.post(builder.build());
        }
    }

    /**
     * 文件Map，可能带有键值对参数
     */
    private void setPostParameAndMapFile() {
        if (mfileMap != null) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            if (mParamsMap != null) {
                for (String key : mParamsMap.keySet()) {
                    builder.addFormDataPart(key, mParamsMap.get(key));
                }
            }

            for (String key : mfileMap.keySet()) {
                builder.addFormDataPart(key, mfileMap.get(key).getName(), RequestBody.create(MediaType.parse(mFileType), mfileMap.get(key)));
            }
            mRequestBuilder.post(builder.build());
        }
    }


    /**
     * 设置头参数
     */
    private void setHeader() {
        if (mHeaderMap != null) {
            for (String key : mHeaderMap.keySet()) {
                mRequestBuilder.addHeader(key, mHeaderMap.get(key));
            }
        }
    }


    void execute() {
        mOkHttpClient.newCall(mOkHttpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                if (mCallBack != null) {
                    mCallBack.onError(call, e);
                }
            }

            @Override
            public void onResponse(final Call call, final Response response) {
                if (mCallBack != null) {
                    mCallBack.onSeccess(call, response);
                }
            }

        });
    }


    /**
     * 自定义RequestBody类，得到文件上传的进度
     */
    private static class ProgressRequestBody extends RequestBody {
        //实际的待包装请求体
        private final RequestBody requestBody;
        //包装完成的BufferedSink
        private BufferedSink bufferedSink;
        private final CallBackUtil callBack;

        ProgressRequestBody(RequestBody requestBody, CallBackUtil callBack) {
            this.requestBody = requestBody;
            this.callBack = callBack;
        }

        /**
         * 重写调用实际的响应体的contentType
         */
        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        /**
         * 重写调用实际的响应体的contentLength ，这个是文件的总字节数
         */
        @Override
        public long contentLength() throws IOException {
            return requestBody.contentLength();
        }

        /**
         * 重写进行写入
         */
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            if (bufferedSink == null) {
                bufferedSink = Okio.buffer(sink(sink));
            }
            requestBody.writeTo(bufferedSink);
            //必须调用flush，否则最后一部分数据可能不会被写入
            bufferedSink.flush();
        }

        /**
         * 写入，回调进度接口
         */
        private Sink sink(BufferedSink sink) {
            return new ForwardingSink(sink) {
                //当前写入字节数
                long bytesWritten = 0L;
                //总字节长度，避免多次调用contentLength()方法
                long contentLength = 0L;

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);//这个方法会循环调用，byteCount是每次调用上传的字节数。
                    if (contentLength == 0) {
                        //获得总字节长度
                        contentLength = contentLength();
                    }
                    //增加当前写入的字节数
                    bytesWritten += byteCount;
                    final float progress = bytesWritten * 1.0f / contentLength;
                    CallBackUtil.mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onProgress(progress, contentLength);
                        }
                    });
                }
            };
        }
    }

}
