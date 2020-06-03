package snow.player;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tencent.mmkv.MMKV;

import snow.player.playlist.PlaylistPlayer;

/**
 * 用于对播放队列的状态进行持久化。
 */
class PersistentPlaylistState extends PlaylistState {
    static final String KEY_PLAY_PROGRESS = "play_progress";
    static final String KEY_SOUND_QUALITY = "sound_quality";
    static final String KEY_AUDIO_EFFECT_ENABLED = "audio_effect_enabled";
    static final String KEY_ONLY_WIFI_NETWORK = "only_wifi_network";
    static final String KEY_IGNORE_LOSS_AUDIO_FOCUS = "ignore_loss_audio_focus";

    private static final String KEY_POSITION = "position";
    private static final String KEY_PLAY_MODE = "play_mode";

    private MMKV mMMKV;

    PersistentPlaylistState(@NonNull Context context, @NonNull String id) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(id);

        MMKV.initialize(context);

        mMMKV = MMKV.mmkvWithID(id);

        super.setPlayProgress(mMMKV.decodeLong(KEY_PLAY_PROGRESS, 0L));
        super.setSoundQuality(mMMKV.decodeInt(KEY_SOUND_QUALITY, Player.SoundQuality.STANDARD));
        super.setAudioEffectEnabled(mMMKV.decodeBool(KEY_AUDIO_EFFECT_ENABLED, false));
        super.setOnlyWifiNetwork(mMMKV.decodeBool(KEY_ONLY_WIFI_NETWORK, true));
        super.setIgnoreLossAudioFocus(mMMKV.decodeBool(KEY_IGNORE_LOSS_AUDIO_FOCUS, false));

        super.setPosition(mMMKV.decodeInt(KEY_POSITION, 0));
        super.setPlayMode(mMMKV.decodeInt(KEY_PLAY_MODE, PlaylistPlayer.PlayMode.SEQUENTIAL));
    }

    @Override
    void setPlayProgress(long playProgress) {
        super.setPlayProgress(playProgress);

        mMMKV.encode(KEY_PLAY_PROGRESS, playProgress);
    }

    @Override
    void setSoundQuality(int soundQuality) {
        super.setSoundQuality(soundQuality);

        mMMKV.encode(KEY_SOUND_QUALITY, soundQuality);
    }

    @Override
    void setAudioEffectEnabled(boolean audioEffectEnabled) {
        super.setAudioEffectEnabled(audioEffectEnabled);

        mMMKV.encode(KEY_AUDIO_EFFECT_ENABLED, audioEffectEnabled);
    }

    @Override
    void setOnlyWifiNetwork(boolean onlyWifiNetwork) {
        super.setOnlyWifiNetwork(onlyWifiNetwork);

        mMMKV.encode(KEY_ONLY_WIFI_NETWORK, onlyWifiNetwork);
    }

    @Override
    void setIgnoreLossAudioFocus(boolean ignoreLossAudioFocus) {
        super.setIgnoreLossAudioFocus(ignoreLossAudioFocus);

        mMMKV.encode(KEY_IGNORE_LOSS_AUDIO_FOCUS, ignoreLossAudioFocus);
    }

    @Override
    void setPosition(int position) {
        super.setPosition(position);

        mMMKV.encode(KEY_POSITION, position);
    }

    @Override
    void setPlayMode(int playMode) {
        super.setPlayMode(playMode);

        mMMKV.encode(KEY_PLAY_MODE, playMode);
    }
}
