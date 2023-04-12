package snow.music;

import androidx.multidex.MultiDexApplication;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.tencent.mmkv.MMKV;

import snow.music.store.MusicStore;
import snow.music.util.NightModeUtil;
import snow.music.util.PlayerUtils;
import snow.music.util.RecognizerAudioUtils;
import snow.music.util.SoundPoolUtils;

public class Application extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        MMKV.initialize(this);
        NightModeUtil.applyNightMode(this);
        MusicStore.init(this);

        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=1904e480");
        SoundPoolUtils.getInstance().init(this);
        PlayerUtils.getInstance().init(this);
    }
}
