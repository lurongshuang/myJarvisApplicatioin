package snow.music.util;// Created byjinengmao

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.common.base.Preconditions;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Response;
import snow.music.Application;
import snow.music.activity.BaseActivity;
import snow.music.activity.navigation.bean.ChatMessage;
import snow.music.store.Music;
import snow.music.util.http.CallBackUtil;
import snow.music.util.http.HttpApi;
import snow.music.util.http.HyrcHttpUtil;
import snow.player.PlaybackState;
import snow.player.PlayerClient;
import snow.player.audio.MusicItem;
import snow.player.lifecycle.PlayerViewModel;
import snow.player.playlist.Playlist;

// on 2023/4/11
// Description：
public class RecognizerAudioUtils {
    private static RecognizerAudioUtils recognizerAudioUtils;

    private static BaseActivity context;

    private PlayerClient mPlayerClient;
    private SpeechRecognizer mIat;

    private PlayerViewModel playerViewModel;

    public static RecognizerAudioUtils getInstance() {
        if (recognizerAudioUtils == null) {
            recognizerAudioUtils = new RecognizerAudioUtils();
        }
        return recognizerAudioUtils;
    }


    public void init(@NonNull PlayerViewModel playerViewModel, BaseActivity context) {
        Preconditions.checkNotNull(playerViewModel);
        this.context = context;
        if (playerViewModel.isInitialized()) {
            mPlayerClient = playerViewModel.getPlayerClient();
        }
        this.playerViewModel = playerViewModel;
        //初始化识别无UI识别对象
        mIat = SpeechRecognizer.createRecognizer(context, new InitListener() {
            @Override
            public void onInit(int i) {

            }
        });
    }

    public void playAudio(PlaybackState playbackState) {
        if (mIat == null) {
            return;
        }
        if (playerViewModel.getPlayerClient().isPlaying()) {
            playerViewModel.pause();
        }
        if (playbackState != null) {
            SoundPoolUtils.getInstance().playDi();
            // 设置语音前端点:静音超时时间，单位ms，即用户多长时间不说话则当做超时处理
            //取值范围{1000～10000}
            mIat.setParameter(SpeechConstant.VAD_BOS, "2000");
        } else {
            SoundPoolUtils.getInstance().playStart();
            mIat.setParameter(SpeechConstant.VAD_BOS, "5000");
        }
        //设置语法ID和 SUBJECT 为空，以免因之前有语法调用而设置了此参数；或直接清空所有参数，具体可参考 DEMO 的示例。
        mIat.setParameter(SpeechConstant.CLOUD_GRAMMAR, null);
        mIat.setParameter(SpeechConstant.SUBJECT, null);
        //设置返回结果格式，目前支持json,xml以及plain 三种格式，其中plain为纯听写文本内容
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        //此处engineType为“cloud”
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, "cloud");
        //设置语音输入语言，zh_cn为简体中文
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        //设置结果返回语言
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");

        //设置语音后端点:后端点静音检测时间，单位ms，即用户停止说话多长时间内即认为不再输入，
        //自动停止录音，范围{0~10000}
        mIat.setParameter(SpeechConstant.VAD_EOS, "1800");
        //设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        mIat.startListening(new RecognizerListener() {
            @Override
            public void onVolumeChanged(int i, byte[] bytes) {

            }

            @Override
            public void onBeginOfSpeech() {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                printResult(context, recognizerResult, isLast, playbackState);
            }

            @Override
            public void onError(SpeechError speechError) {

            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });
    }

    private HashMap<String, String> mIatResults = new LinkedHashMap<>();

    public void clearMIatResults() {
        if (mIatResults != null && mIatResults.size() > 0) {
            mIatResults.clear();
        }
    }

    public void printResult(Context context, RecognizerResult results, boolean isLast, PlaybackState playbackState) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
            adapterAddMessage(e.getMessage(), ChatMessage.TYPE_SEND);
        }
        mIatResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        String a = resultBuffer.toString();
        if (isLast) {
            if (playerViewModel != null && playbackState != null && playerViewModel.getPlayerClient().getPlaylistSize() > 0 && (a.isEmpty() || (a.length() > 0 && isAllPunctuation(a)))) {
                if (PlaybackState.PLAYING == playbackState) {
                    SoundPoolUtils.getInstance().playPlaying();
                    playerViewModel.play();
                } else if (PlaybackState.PAUSED == playbackState) {
                    SoundPoolUtils.getInstance().playPause();
                    playerViewModel.pause();
                }
                return;
            }
            SoundPoolUtils.getInstance().playEnd();
            adapterAddMessage(a, ChatMessage.TYPE_SEND);
            postString(context, a);

        }
    }

    public boolean isAllPunctuation(String input) {
        // 定义正则表达式，匹配所有标点符号
        String regex = "\\p{Punct}+";
        // 使用正则表达式进行匹配
        return input.matches(regex);
    }


    String audioUrl = null;
    String contentUrl = null;

    public void postString(Context context, String audioContext) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_asr", audioContext);
            if (contentUrl != null) {
                jsonObject.put("content_audio_url", contentUrl);
            }
        } catch (JSONException e) {
            adapterAddMessage(e.getMessage(), ChatMessage.TYPE_SEND);
        }
        String strJson = jsonObject.toString();
        ((BaseActivity) context).showLoading();
        HyrcHttpUtil.httpPostJson(Application.HOST + Application.userRequest, strJson, new CallBackUtil<String>() {
            @Override
            public String onParseResponse(Call call, Response response) {
                try {
                    return response.body().string();
                } catch (Exception e) {
                    adapterAddMessage(e.getMessage(), ChatMessage.TYPE_SEND);
                }
                return null;
            }

            @Override
            public void onFailure(Call call, Exception e) {
                ((BaseActivity) context).clearLoading();
                adapterAddMessage(e.getMessage(), ChatMessage.TYPE_SEND);
            }

            @Override
            public void onResponse(String response) {
                ((BaseActivity) context).clearLoading();
                if (response != null) {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.has("reply_audio_url")) {
                            audioUrl = json.getString("reply_audio_url");
                        }
                        if (json.has("content_audio_url")) {
                            contentUrl = json.getString("content_audio_url");
                        }

                        if (audioUrl != null && !audioUrl.isEmpty()) {
                            playAudio(context, audioUrl, contentUrl);
                        }
                        if (json.has("reply_text")) {
                            adapterAddMessage(json.getString("reply_text"), ChatMessage.TYPE_RECEIVED);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        adapterAddMessage(e.getMessage(), ChatMessage.TYPE_RECEIVED);
                    }
                }
            }
        });
    }

    private void playAudio(Context context, String audio_url, String contentUrl) {
        ((BaseActivity) context).showLoading();
        if (playerViewModel.getPlayerClient().isPlaying()) {
            playerViewModel.pause();
        }
        PlayerUtils.getInstance().play(Uri.parse(audio_url), new AudioCompletion() {
            @Override
            public void onCompletionListener() {
                ((BaseActivity) context).clearLoading();
                if (contentUrl != null && !contentUrl.isEmpty()) {
                    playMusic(contentUrl);
                }
            }

            @Override
            public void onErrorListener() {
                ((BaseActivity) context).clearLoading();
            }
        });
    }

    private void playMusic(String contentUrl) {

        final Music musicC = new Music(
                21313,
                "爱情转移",
                "artist3",
                "album3",
                contentUrl,
                "https://www.test.com/test3.png",
                60_000,
                System.currentTimeMillis());
        MusicItem musicItem = playerViewModel.getPlayingMusicItem().getValue();
        if (musicItem != null && musicItem.getMusicId() == String.valueOf(musicC.getId())) {

            return;
        }
        List<Music> list = new ArrayList<Music>();
        list.add(musicC);
        Playlist playlist = MusicListUtil.asPlaylist(""/*empty*/, list, 0);
        playerViewModel.setPlaylist(playlist, true);
    }

    private void adapterAddMessage(String message, int type) {
        if (context == null) {
            return;
        }
        context.addBean(message, type);
    }
}
