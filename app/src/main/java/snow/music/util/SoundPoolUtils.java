package snow.music.util;// Created byjinengmao

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;


import java.util.HashMap;

import snow.music.R;

// on 2023/3/31
// Description：
public class SoundPoolUtils {
    private static SoundPoolUtils soundPoolUtils;

    private SoundPool soundPool;
    private HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();

    public void init(Context context) {
        if (Build.VERSION.SDK_INT >= 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setMaxStreams(100);
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            builder.setAudioAttributes(attrBuilder.build());
            soundPool = builder.build();
        } else {
            soundPool = new SoundPool(100, AudioManager.STREAM_SYSTEM, 5);
        }

        if (soundMap.size() > 0) {
            soundMap.clear();
        }
        soundMap.put(0, soundPool.load(context, R.raw.start, 1));
        soundMap.put(1, soundPool.load(context, R.raw.end, 1));
        soundMap.put(2, soundPool.load(context, R.raw.playing, 1));
        soundMap.put(3, soundPool.load(context, R.raw.pause, 1));
        soundMap.put(4, soundPool.load(context, R.raw.di, 1));


    }

    SoundPoolUtils() {
    }

    public static SoundPoolUtils getInstance() {
        if (soundPoolUtils == null) {
            soundPoolUtils = new SoundPoolUtils();
        }
        return soundPoolUtils;
    }

    public void playStart() {
        soundPool.play(soundMap.get(0), 1, 1, 2, 0, 1);
    }

    public void playEnd() {
        soundPool.play(soundMap.get(1), 1, 1, 2, 0, 1);
    }

    public void playPlaying() {
        soundPool.play(soundMap.get(2), 1, 1, 2, 0, 1);
    }

    public void playPause() {
        soundPool.play(soundMap.get(3), 1, 1, 2, 0, 1);
    }

    public void playDi() {
        soundPool.play(soundMap.get(4), 1, 1, 2, 0, 1);
    }
}
