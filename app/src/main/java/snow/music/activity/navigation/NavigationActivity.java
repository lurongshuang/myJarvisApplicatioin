package snow.music.activity.navigation;

import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.common.base.Preconditions;

import snow.music.GlideApp;
import snow.music.R;
import snow.music.activity.BaseActivity;
import snow.music.databinding.ActivityNavigationBinding;
import snow.music.dialog.PlaylistDialog;
import snow.music.dialog.ScannerDialog;
import snow.music.service.AppPlayerService;
import snow.music.util.DimenUtil;
import snow.music.util.PlayerUtil;
import snow.music.util.RecognizerAudioUtils;
import snow.player.PlaybackState;
import snow.player.lifecycle.PlayerViewModel;

public class NavigationActivity extends BaseActivity {
    private static final String KEY_SCAN_LOCAL_MUSIC = "scan_local_music";

    private ActivityNavigationBinding mBinding;
    private PlayerViewModel mPlayerViewModel;
    private NavigationViewModel mNavigationViewModel;

    private int mIconCornerRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_navigation);

        initAllViewModel();
        setPlayerClient(mPlayerViewModel.getPlayerClient());

        mBinding.setNavViewModel(mNavigationViewModel);
        mBinding.setLifecycleOwner(this);


        observerPlayingMusicItem();

//        if (shouldScanLocalMusic()) {
//            scanLocalMusic();
//        }

        mIconCornerRadius = DimenUtil.getDimenPx(getResources(), R.dimen.album_icon_corner_radius);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.USE_FULL_SCREEN_INTENT}, 123);
        Intent intent = getIntent();
        if (intent.getAction() == Intent.ACTION_VOICE_COMMAND) {
            mNavigationViewModel.testAudio(findViewById(R.id.tvTestAudio), null);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction() == Intent.ACTION_VOICE_COMMAND) {
            Bundle bundle = intent.getExtras();
            PlaybackState playbackState = null;
            if (bundle != null && bundle.containsKey("playbackState")) {
                playbackState = PlaybackState.valueOf(bundle.getString("playbackState"));
            }
            mNavigationViewModel.testAudio(findViewById(R.id.tvTestAudio), playbackState);
        }
    }


    private void initAllViewModel() {
        ViewModelProvider viewModelProvider = new ViewModelProvider(this);

        mPlayerViewModel = viewModelProvider.get(PlayerViewModel.class);
        mNavigationViewModel = viewModelProvider.get(NavigationViewModel.class);

        PlayerUtil.initPlayerViewModel(this, mPlayerViewModel, AppPlayerService.class);

        initNavigationViewModel();
    }

    private void initNavigationViewModel() {
        if (mNavigationViewModel.isInitialized()) {
            return;
        }

        mNavigationViewModel.init(mPlayerViewModel, this, mBinding.recyclerList);
        RecognizerAudioUtils.getInstance().init(mPlayerViewModel, this);

    }

    private void observerPlayingMusicItem() {
        mPlayerViewModel.getPlayingMusicItem().observe(this, musicItem -> {
            if (musicItem == null) {
                mBinding.ivDisk.setImageResource(R.mipmap.ic_album_default_icon_big);
                return;
            }

            loadMusicIcon(musicItem.getUri());
        });
    }

    private void loadMusicIcon(String musicUri) {
        GlideApp.with(this).load(musicUri).placeholder(R.mipmap.ic_album_default_icon_big).transform(new CenterCrop(), new RoundedCorners(mIconCornerRadius)).transition(DrawableTransitionOptions.withCrossFade()).into(mBinding.ivDisk);
    }

    private boolean shouldScanLocalMusic() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        return preferences.getBoolean(KEY_SCAN_LOCAL_MUSIC, true);
    }

    private void scanLocalMusic() {
        getPreferences(MODE_PRIVATE).edit().putBoolean(KEY_SCAN_LOCAL_MUSIC, false).apply();

        ScannerDialog scannerDialog = ScannerDialog.newInstance(true, true);
        scannerDialog.show(getSupportFragmentManager(), "scannerDialog");
    }

    public void showPlaylist(View view) {
        Preconditions.checkNotNull(view);

        PlaylistDialog.newInstance().show(getSupportFragmentManager(), "Playlist");
    }

    @Override
    public void addBean(String message, int type) {
//        super.addBean(message, type);
        mNavigationViewModel.adapterAddMessage(message, type);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
        return false;
    }
}