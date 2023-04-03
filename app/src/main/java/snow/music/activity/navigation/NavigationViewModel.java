package snow.music.activity.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.common.base.Preconditions;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import snow.music.R;
import snow.music.activity.browser.album.AlbumBrowserActivity;
import snow.music.activity.browser.artist.ArtistBrowserActivity;
import snow.music.activity.browser.musiclist.MusicListBrowserActivity;
import snow.music.activity.favorite.FavoriteActivity;
import snow.music.activity.history.HistoryActivity;
import snow.music.activity.localmusic.LocalMusicActivity;
import snow.music.activity.player.PlayerActivity;
import snow.music.activity.search.SearchActivity;
import snow.music.activity.setting.SettingActivity;
import snow.music.store.MusicStore;
import snow.music.util.FavoriteObserver;
import snow.music.util.MusicUtil;
import snow.music.util.SoundPoolUtils;
import snow.player.PlaybackState;
import snow.player.PlayerClient;
import snow.player.audio.MusicItem;
import snow.player.lifecycle.PlayerViewModel;
import snow.music.util.JsonParser;

public class NavigationViewModel extends ViewModel {
    private final MutableLiveData<Integer> mFavoriteDrawable;
    private final MutableLiveData<CharSequence> mSecondaryText;

    private final Observer<MusicItem> mPlayingMusicItemObserver;
    private final Observer<String> mArtistObserver;
    private final Observer<Boolean> mErrorObserver;
    private final FavoriteObserver mFavoriteObserver;

    private PlayerViewModel mPlayerViewModel;
    private boolean mInitialized;

    public NavigationViewModel() {
        mFavoriteDrawable = new MutableLiveData<>(R.drawable.ic_favorite_false);
        mSecondaryText = new MutableLiveData<>("");
        mFavoriteObserver = new FavoriteObserver(favorite -> mFavoriteDrawable.setValue(favorite ? R.drawable.ic_favorite_true : R.drawable.ic_favorite_false));
        mArtistObserver = artist -> updateSecondaryText();
        mErrorObserver = error -> updateSecondaryText();
        mPlayingMusicItemObserver = mFavoriteObserver::setMusicItem;
    }

    public void init(@NonNull PlayerViewModel playerViewModel) {
        Preconditions.checkNotNull(playerViewModel);

        if (mInitialized) {
            return;
        }

        mInitialized = true;
        mPlayerViewModel = playerViewModel;

        mFavoriteObserver.subscribe();
        mPlayerViewModel.getPlayingMusicItem().observeForever(mPlayingMusicItemObserver);
        mPlayerViewModel.getArtist().observeForever(mArtistObserver);
        mPlayerViewModel.isError().observeForever(mErrorObserver);
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

    public void playPause() {
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


    /**
     * 初始化监听器。
     */

    public void testAudio(View view) {
        mIatResults.clear();
        SoundPoolUtils.getInstance().playStart();
        RecognizerDialog mIatDialog = new RecognizerDialog(view.getContext(), new InitListener() {
            @Override
            public void onInit(int i) {

            }
        });
        mIatDialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                printResult(recognizerResult, isLast);
            }

            @Override
            public void onError(SpeechError speechError) {
                Log.e("", "");
            }
        });
        mIatDialog.show();
    }

    private HashMap<String, String> mIatResults = new LinkedHashMap<>();

    public final ObservableField<String> strResult = new ObservableField<>();

    private void printResult(RecognizerResult results, boolean isLast) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        String a = resultBuffer.toString();
        if (isLast) {
            SoundPoolUtils.getInstance().playStart();
            Log.e("printResult", a);
            strResult.set(a);
        }
    }
}
