package snow.music.util;// Created byjinengmao

import static com.google.android.exoplayer2.C.WAKE_MODE_NETWORK;

import android.content.Context;

import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.MediaItem;

import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.util.Util;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import snow.player.audio.MusicPlayer;
import snow.player.exo.ExoMusicPlayer;
import snow.player.exo.util.OkHttpUtil;


// on 2023/4/4
// Description：
public class PlayerUtils {
    private Context context;
    private static PlayerUtils playerUtils;

    public static PlayerUtils getInstance() {
        if (playerUtils == null) {
            playerUtils = new PlayerUtils();
        }
        return playerUtils;
    }

    public void init(Context context) {
        this.context = context;

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS);

        OkHttpUtil.enableTls12OnPreLollipop(builder, true);

        OkHttpClient okHttpClient = builder.build();

        OkHttpDataSource.Factory httpDataSourceFactory = new OkHttpDataSource.Factory(
                request -> {
                    Request rq = new Request.Builder(request)
                            // Note: must add head: 'user-agent'
                            .addHeader("user-agent", Util.getUserAgent(context, context.getPackageName()))
                            .build();

                    return okHttpClient.newCall(rq);
                }
        );

        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(
                context, httpDataSourceFactory);

        mProgressiveMediaSourceFactory = new ProgressiveMediaSource.Factory(dataSourceFactory);
        mHlsMediaSourceFactory = new HlsMediaSource.Factory(dataSourceFactory);

    }

    private ProgressiveMediaSource.Factory mProgressiveMediaSourceFactory;
    private HlsMediaSource.Factory mHlsMediaSourceFactory;
    MusicPlayer player;

    public void play(Uri url) {
        if (context == null) {
            return;
        }
        if (player != null) {
            player.release();
        }


        String path = url.getLastPathSegment();
        if (path != null && path.endsWith(".m3u8")) {
            player = new ExoMusicPlayer(context, mHlsMediaSourceFactory, url);
        }

        player = new ExoMusicPlayer(context, mProgressiveMediaSourceFactory, url);

        player.setOnCompletionListener(new MusicPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MusicPlayer mp) {
                Log.e("","");
            }
        });
        player.setOnErrorListener(new MusicPlayer.OnErrorListener() {
            @Override
            public void onError(MusicPlayer mp, int errorCode) {
                Log.e("","");
            }
        });
        try {
            player.prepare();
            player.start();
        } catch (Exception e) {
        }
    }
}
