package snow.music.util.http;// Created byjinengmao

import java.io.File;
import java.util.List;
import java.util.Map;

// on 2023/4/6
// Description：
public class HyrcHttpUtil {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";

    public static final String FILE_TYPE_FILE = "file/*";
    public static final String FILE_TYPE_IMAGE = "image/*";
    public static final String FILE_TYPE_AUDIO = "audio/*";
    public static final String FILE_TYPE_VIDEO = "video/*";


    /**
     * get请求
     */
    public static void httpGet(String url, CallBackUtil callBack) {
        httpGet(url, null, null, callBack);
    }

    /**
     * get请求，可以传递参数
     */
    public static void httpGet(String url, Map<String, String> paramsMap, CallBackUtil callBack) {
        httpGet(url, paramsMap, null, callBack);
    }

    /**
     * get请求，可以传递参数
     */
    public static void httpGet(String url, Map<String, String> paramsMap, Map<String, String> headerMap, CallBackUtil callBack) {
        new RequestUtil(METHOD_GET, url, paramsMap, headerMap, callBack).execute();
    }

    /**
     * post请求
     */
    public static void httpPost(String url, CallBackUtil callBack) {
        httpPost(url, null, callBack);
    }

    /**
     * post请求，可以传递参数
     */
    public static void httpPost(String url, Map<String, String> paramsMap, CallBackUtil callBack) {
        httpPost(url, paramsMap, null, callBack);
    }

    /**
     * post请求，可以传递参数
     */
    public static void httpPost(String url, Map<String, String> paramsMap, Map<String, String> headerMap, CallBackUtil callBack) {
        new RequestUtil(METHOD_POST, url, paramsMap, headerMap, callBack).execute();
    }

    /**
     * post请求
     */
    public static void httpPut(String url, CallBackUtil callBack) {
        httpPut(url, null, callBack);
    }

    /**
     * post请求，可以传递参数
     */
    public static void httpPut(String url, Map<String, String> paramsMap, CallBackUtil callBack) {
        httpPut(url, paramsMap, null, callBack);
    }

    /**
     * post请求，可以传递参数
     */
    public static void httpPut(String url, Map<String, String> paramsMap, Map<String, String> headerMap, CallBackUtil callBack) {
        new RequestUtil(METHOD_PUT, url, paramsMap, headerMap, callBack).execute();
    }

    /**
     * post请求
     */
    public static void httpDelete(String url, CallBackUtil callBack) {
        httpDelete(url, null, callBack);
    }

    /**
     * post请求，可以传递参数
     */
    public static void httpDelete(String url, Map<String, String> paramsMap, CallBackUtil callBack) {
        httpDelete(url, paramsMap, null, callBack);
    }

    /**
     * post请求，可以传递参数
     */
    public static void httpDelete(String url, Map<String, String> paramsMap, Map<String, String> headerMap, CallBackUtil callBack) {
        new RequestUtil(METHOD_DELETE, url, paramsMap, headerMap, callBack).execute();
    }

    /**
     * post请求，可以传递参数
     */
    public static void httpPostJson(String url, String jsonStr, CallBackUtil callBack) {
        httpPostJson(url, jsonStr, null, callBack);
    }

    /**
     * post请求，可以传递参数
     */
    public static void httpPostJson(String url, String jsonStr, Map<String, String> headerMap, CallBackUtil callBack) {
        new RequestUtil(url, jsonStr, headerMap, callBack).execute();
    }

    /**
     * post请求，上传单个文件
     */
    public static void httpUploadFile(String url, File file, String fileKey, String fileType, CallBackUtil callBack) {
        httpUploadFile(url, file, fileKey, fileType, null, callBack);
    }

    /**
     * post请求，上传单个文件
     */
    public static void httpUploadFile(String url, File file, String fileKey, String fileType, Map<String, String> paramsMap, CallBackUtil callBack) {
        httpUploadFile(url, file, fileKey, fileType, paramsMap, null, callBack);
    }

    /**
     * post请求，上传单个文件
     */
    public static void httpUploadFile(String url, File file, String fileKey, String fileType, Map<String, String> paramsMap, Map<String, String> headerMap, CallBackUtil callBack) {
        new RequestUtil(url, paramsMap, file, fileKey, fileType, headerMap, callBack).execute();
    }

    /**
     * post请求，上传多个文件，以list集合的形式
     */
    public static void httpUploadListFile(String url, List<File> fileList, String fileKey, String fileType, CallBackUtil callBack) {
        httpUploadListFile(url, null, fileList, fileKey, fileType, callBack);
    }

    /**
     * post请求，上传多个文件，以list集合的形式
     */
    public static void httpUploadListFile(String url, Map<String, String> paramsMap, List<File> fileList, String fileKey, String fileType, CallBackUtil callBack) {
        httpUploadListFile(url, paramsMap, fileList, fileKey, fileType, null, callBack);
    }

    /**
     * post请求，上传多个文件，以list集合的形式
     */
    public static void httpUploadListFile(String url, Map<String, String> paramsMap, List<File> fileList, String fileKey, String fileType, Map<String, String> headerMap, CallBackUtil callBack) {
        new RequestUtil(url, paramsMap, fileList, fileKey, fileType, headerMap, callBack).execute();
    }

    /**
     * post请求，上传多个文件，以map集合的形式
     */
    public static void httpUploadMapFile(String url, Map<String, File> fileMap, String fileType, CallBackUtil callBack) {
        httpUploadMapFile(url, fileMap, fileType, null, callBack);
    }

    /**
     * post请求，上传多个文件，以map集合的形式
     */
    public static void httpUploadMapFile(String url, Map<String, File> fileMap, String fileType, Map<String, String> paramsMap, CallBackUtil callBack) {
        httpUploadMapFile(url, fileMap, fileType, paramsMap, null, callBack);
    }

    /**
     * post请求，上传多个文件，以map集合的形式
     */
    public static void httpUploadMapFile(String url, Map<String, File> fileMap, String fileType, Map<String, String> paramsMap, Map<String, String> headerMap, CallBackUtil callBack) {
        new RequestUtil(url, paramsMap, fileMap, fileType, headerMap, callBack).execute();
    }

    /**
     * 下载文件,不带参数
     */
    public static void httpDownloadFile(String url, CallBackUtil.CallBackFile callBack) {
        httpDownloadFile(url, null, callBack);
    }

    /**
     * 下载文件,带参数
     */
    public static void httpDownloadFile(String url, Map<String, String> paramsMap, CallBackUtil.CallBackFile callBack) {
        httpGet(url, paramsMap, null, callBack);
    }

    /**
     * 加载图片
     */
    public static void httpGetBitmap(String url, CallBackUtil.CallBackBitmap callBack) {
        httpGetBitmap(url, null, callBack);
    }

    /**
     * 加载图片，带参数
     */
    public static void httpGetBitmap(String url, Map<String, String> paramsMap, CallBackUtil.CallBackBitmap callBack) {
        httpGet(url, paramsMap, null, callBack);
    }


}
