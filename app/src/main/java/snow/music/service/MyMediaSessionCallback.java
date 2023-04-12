package snow.music.service;// Created byjinengmao

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.List;

import snow.music.activity.navigation.NavigationActivity;
import snow.music.util.RecognizerAudioUtils;
import snow.player.PlaybackState;
import snow.player.PlayerService;

// on 2023/4/11
// Description：
public class MyMediaSessionCallback extends PlayerService.MediaSessionCallback {
    private Context context;

    public MyMediaSessionCallback(@NonNull PlayerService playerService, @NonNull Context context) {
        super(playerService);
        this.context = context;
    }

    @Override
    public void onPlay() {
//        super.onPlay();
        intent(PlaybackState.PLAYING);
    }

    @Override
    public void onPause() {
//        super.onPause();
        intent(PlaybackState.PAUSED);
    }

    private void intent(PlaybackState playbackState) {
        if (!isBackground(context)) {
            Intent intent = new Intent(context, NavigationActivity.class);
            intent.setAction(Intent.ACTION_VOICE_COMMAND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("playbackState", playbackState.name());
            context.startActivity(intent);
        } else {
            RecognizerAudioUtils.getInstance().playAudio(playbackState);
        }
    }

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        Iterator var3 = appProcesses.iterator();

        ActivityManager.RunningAppProcessInfo appProcess;
        do {
            if (!var3.hasNext()) {
                return false;
            }

            appProcess = (ActivityManager.RunningAppProcessInfo) var3.next();
        } while (!appProcess.processName.equals(context.getPackageName()));

        if (appProcess.importance == 100) {

            return false;
        } else {
            return true;
        }
    }

}
