package snow.music.service;// Created byjinengmao

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import snow.music.activity.navigation.NavigationActivity;
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
        Intent intent = new Intent(context, NavigationActivity.class);
        intent.setAction(Intent.ACTION_VOICE_COMMAND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("playbackState", playbackState.name());
        context.startActivity(intent);
    }
}
