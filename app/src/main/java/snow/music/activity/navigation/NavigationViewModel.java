package snow.music.activity.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Preconditions;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;
import snow.music.R;
import snow.music.activity.BaseActivity;
import snow.music.activity.browser.album.AlbumBrowserActivity;
import snow.music.activity.browser.artist.ArtistBrowserActivity;
import snow.music.activity.browser.musiclist.MusicListBrowserActivity;
import snow.music.activity.favorite.FavoriteActivity;
import snow.music.activity.history.HistoryActivity;
import snow.music.activity.localmusic.LocalMusicActivity;
import snow.music.activity.navigation.adapter.ChatAdapter;
import snow.music.activity.navigation.bean.ChatMessage;
import snow.music.activity.player.PlayerActivity;
import snow.music.activity.search.SearchActivity;
import snow.music.activity.setting.SettingActivity;
import snow.music.store.Music;
import snow.music.store.MusicStore;
import snow.music.util.AudioCompletion;
import snow.music.util.FavoriteObserver;
import snow.music.util.MusicListUtil;
import snow.music.util.MusicUtil;
import snow.music.util.PlayerUtils;
import snow.music.util.SoundPoolUtils;
import snow.music.util.http.CallBackUtil;
import snow.music.util.http.HttpApi;
import snow.music.util.http.HyrcHttpUtil;
import snow.player.PlaybackState;
import snow.player.PlayerClient;
import snow.player.audio.MusicItem;
import snow.player.lifecycle.PlayerViewModel;
import snow.music.util.JsonParser;
import snow.player.playlist.Playlist;

public class NavigationViewModel extends ViewModel {
    private final MutableLiveData<Integer> mFavoriteDrawable;
    private final MutableLiveData<CharSequence> mSecondaryText;

    private final Observer<MusicItem> mPlayingMusicItemObserver;
    private final Observer<String> mArtistObserver;
    private final Observer<Boolean> mErrorObserver;
    private final FavoriteObserver mFavoriteObserver;

    private PlayerViewModel mPlayerViewModel;
    private boolean mInitialized;

    private List<ChatMessage> messages = new ArrayList<>();
    private RecyclerView recyclerView;
    private Context context;
    private ChatAdapter adapter;

    public NavigationViewModel() {
        mFavoriteDrawable = new MutableLiveData<>(R.drawable.ic_favorite_false);
        mSecondaryText = new MutableLiveData<>("");
        mFavoriteObserver = new FavoriteObserver(favorite -> mFavoriteDrawable.setValue(favorite ? R.drawable.ic_favorite_true : R.drawable.ic_favorite_false));
        mArtistObserver = artist -> updateSecondaryText();
        mErrorObserver = error -> updateSecondaryText();
        mPlayingMusicItemObserver = mFavoriteObserver::setMusicItem;
    }

    public void init(@NonNull PlayerViewModel playerViewModel, Context context, RecyclerView recyclerView) {
        Preconditions.checkNotNull(playerViewModel);
        this.context = context;
        if (mInitialized) {
            return;
        }

        mInitialized = true;
        mPlayerViewModel = playerViewModel;

        mFavoriteObserver.subscribe();
        mPlayerViewModel.getPlayingMusicItem().observeForever(mPlayingMusicItemObserver);
        mPlayerViewModel.getArtist().observeForever(mArtistObserver);
        mPlayerViewModel.isError().observeForever(mErrorObserver);
        this.recyclerView = recyclerView;
        initRecyclerView();
        if (mPlayerViewModel.isInitialized()) {
        }
    }

    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setStackFromEnd(false);
        manager.setReverseLayout(false);
        recyclerView.setLayoutManager(manager);
        adapter = new ChatAdapter(messages);
        recyclerView.setAdapter(adapter);


    }

    private void updateSecondaryText() {
        PlayerClient playerClient = mPlayerViewModel.getPlayerClient();

        CharSequence text = mPlayerViewModel.getArtist().getValue();

        if (playerClient.isError()) {
            text = playerClient.getErrorMessage();
            SpannableString colorText = new SpannableString(text);
            colorText.setSpan(new ForegroundColorSpan(Color.parseColor("#F44336")), 0, text.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            text = colorText;
        }

        mSecondaryText.setValue(text);
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    @Override
    protected void onCleared() {
        mFavoriteObserver.unsubscribe();
        if (isInitialized()) {
            mPlayerViewModel.getPlayingMusicItem().removeObserver(mPlayingMusicItemObserver);
            mPlayerViewModel.getArtist().removeObserver(mArtistObserver);
            mPlayerViewModel.isError().removeObserver(mErrorObserver);
        }
    }

    public LiveData<String> getMusicTitle() {
        if (!mInitialized) {
            throw new IllegalStateException("NavigationViewModel not init yet.");
        }

        return mPlayerViewModel.getTitle();
    }

    public LiveData<CharSequence> getSecondaryText() {
        if (!mInitialized) {
            throw new IllegalStateException("NavigationViewModel not init yet.");
        }

        return mSecondaryText;
    }

    @NonNull
    public LiveData<Integer> getFavoriteDrawable() {
        return mFavoriteDrawable;
    }

    @NonNull
    public LiveData<Integer> getPlayPauseDrawable() {
        if (!mInitialized) {
            throw new IllegalStateException("NavigationViewModel not init yet.");
        }

        return Transformations.map(mPlayerViewModel.getPlaybackState(), playbackState -> {
            if (playbackState == PlaybackState.PLAYING) {
                return R.mipmap.ic_pause;
            } else {
                return R.mipmap.ic_play;
            }
        });
    }

    public void togglePlayingMusicFavorite() {
        if (!isInitialized()) {
            return;
        }

        MusicItem playingMusicItem = mPlayerViewModel.getPlayingMusicItem().getValue();
        if (playingMusicItem == null) {
            return;
        }

        MusicStore.getInstance().toggleFavorite(MusicUtil.asMusic(playingMusicItem));
    }

    public void skipToPrevious() {
        if (!mInitialized) {
            throw new IllegalStateException("NavigationViewModel not init yet.");
        }

        mPlayerViewModel.skipToPrevious();
    }

    public void playPause(View view) {
//        if (mPlayerViewModel.getPlayerClient().getPlaylistSize() == 0) {
//            testAudio(view);
//        }
        if (!mInitialized) {
            throw new IllegalStateException("NavigationViewModel not init yet.");
        }
        mPlayerViewModel.playPause();
    }

    public void skipToNext() {
        if (!mInitialized) {
            throw new IllegalStateException("NavigationViewModel not init yet.");
        }

        mPlayerViewModel.skipToNext();
    }

    public LiveData<Integer> getDuration() {
        if (!mInitialized) {
            throw new IllegalStateException("NavigationViewModel not init yet.");
        }

        return mPlayerViewModel.getDuration();
    }

    public LiveData<Integer> getPlayProgress() {
        if (!mInitialized) {
            throw new IllegalStateException("NavigationViewModel not init yet.");
        }

        return mPlayerViewModel.getPlayProgress();
    }

    public void navigateToSearch(View view) {
        Context context = view.getContext();
        SearchActivity.start(context, SearchActivity.Type.MUSIC_LIST, MusicStore.MUSIC_LIST_LOCAL_MUSIC);
    }

    public void navigateToSetting(View view) {
        startActivity(view.getContext(), SettingActivity.class);
    }

    public void navigateToLocalMusic(View view) {
        startActivity(view.getContext(), LocalMusicActivity.class);
    }

    public void navigateToFavorite(View view) {
        startActivity(view.getContext(), FavoriteActivity.class);
    }

    public void navigateToMusicListBrowser(View view) {
        startActivity(view.getContext(), MusicListBrowserActivity.class);
    }

    public void navigateToArtistBrowser(View view) {
        startActivity(view.getContext(), ArtistBrowserActivity.class);
    }

    public void navigateToAlbum(View view) {
        startActivity(view.getContext(), AlbumBrowserActivity.class);
    }

    public void navigateToHistory(View view) {
        startActivity(view.getContext(), HistoryActivity.class);
    }

    public void navigateToPlayer(View view) {
        startActivity(view.getContext(), PlayerActivity.class);
    }

    private void startActivity(Context context, Class<? extends Activity> activity) {
        Intent intent = new Intent(context, activity);
        context.startActivity(intent);
    }


    public void testAudio1(View view) {
        postString(view.getContext(), ((Button) view).getText().toString());
        adapterAddMessage(((Button) view).getText().toString(), ChatMessage.TYPE_SEND);
    }

    private void adapterAddMessage(String message, int type) {
        ChatMessage leftMessage = new ChatMessage(message, type);
        messages.add(leftMessage);
        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    public void clickTestAudio(View view) {
        testAudio(view, null);
    }

    /**
     * 初始化监听器。
     */

    public void testAudio(View view, PlaybackState playbackState) {
        if (mPlayerViewModel.getPlayerClient().isPlaying()) {
            mPlayerViewModel.pause();
        }
        if (playbackState != null) {
            SoundPoolUtils.getInstance().playDi();
        } else {
            SoundPoolUtils.getInstance().playStart();
        }
        new Handler().postDelayed(new Runnable() {  // 开启的ru    nnable也会在这个handler所依附线程中运行，即主线程
            @Override
            public void run() {
                mIatResults.clear();
                RecognizerDialog mIatDialog = new RecognizerDialog(view.getContext(), new InitListener() {
                    @Override
                    public void onInit(int i) {

                    }
                });
                mIatDialog.setListener(new RecognizerDialogListener() {
                    @Override
                    public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                        printResult(view.getContext(), recognizerResult, isLast, playbackState);
                    }

                    @Override
                    public void onError(SpeechError speechError) {
                        adapterAddMessage(speechError.getMessage(), ChatMessage.TYPE_SEND);
                    }
                });
                if (playbackState != null) {
                    mIatDialog.setParameter(SpeechConstant.VAD_BOS, "2000");
                }
                mIatDialog.show();
            }
        }, 1000);

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
        HyrcHttpUtil.httpPostJson(HttpApi.userRequest, strJson, new CallBackUtil<String>() {
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
                    }
                }
            }
        });


    }

    private void playAudio(Context context, String audio_url, String contentUrl) {
        ((BaseActivity) context).showLoading();
        if (mPlayerViewModel.getPlayerClient().isPlaying()) {
            mPlayerViewModel.pause();
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
        MusicItem musicItem = mPlayerViewModel.getPlayingMusicItem().getValue();
        if (musicItem != null && musicItem.getMusicId() == String.valueOf(musicC.getId())) {

            return;
        }
        List<Music> list = new ArrayList<Music>();
        list.add(musicC);
        Playlist playlist = MusicListUtil.asPlaylist(""/*empty*/, list, 0);
        mPlayerViewModel.setPlaylist(playlist, true);
    }

    private HashMap<String, String> mIatResults = new LinkedHashMap<>();

//    public final ObservableField<String> strResult = new ObservableField<>();

    private void printResult(Context context, RecognizerResult results, boolean isLast, PlaybackState playbackState) {
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
            if (playbackState != null && a.isEmpty() && mPlayerViewModel.getPlayerClient().getPlaylistSize() > 0) {
                if (mPlayerViewModel == null) {
                    return;
                }
                if (PlaybackState.PLAYING == playbackState) {
                    SoundPoolUtils.getInstance().playPlaying();
                    mPlayerViewModel.play();
                } else if (PlaybackState.PAUSED == playbackState) {
                    SoundPoolUtils.getInstance().playPause();
                    mPlayerViewModel.pause();
                }
                return;
            }
            SoundPoolUtils.getInstance().playEnd();
            adapterAddMessage(a, ChatMessage.TYPE_SEND);
            postString(context, a);

        }
    }
}
