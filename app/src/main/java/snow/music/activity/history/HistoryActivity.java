package snow.music.activity.history;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import java.util.Objects;

import snow.music.R;
import snow.music.activity.ListActivity;
import snow.music.dialog.MessageDialog;
import snow.music.service.AppPlayerService;
import snow.music.store.Music;
import snow.music.store.MusicStore;
import snow.music.util.MusicListUtil;
import snow.music.util.PlayerUtil;
import snow.player.lifecycle.PlayerViewModel;
import snow.player.playlist.Playlist;

public class HistoryActivity extends ListActivity {
    private PlayerViewModel mPlayerViewModel;
    private HistoryViewModel mHistoryViewModel;
    private HistoryAdapter mHistoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViewModel();
        initRecyclerView();
    }

    public void finishSelf(View view) {
        finish();
    }

    public void clearHistory(View view) {
        if (view.getId() != R.id.btnClearHistory) {
            return;
        }

        MessageDialog messageDialog = new MessageDialog.Builder(getApplicationContext())
                .setMessage(R.string.message_clear_history)
                .setPositiveTextColor(getResources().getColor(R.color.red_500))
                .setPositiveButtonClickListener((dialog, which) -> mHistoryViewModel.clearHistory())
                .build();

        messageDialog.show(getSupportFragmentManager(), "clearHistory");
    }

    private void initViewModel() {
        ViewModelProvider viewModelProvider = new ViewModelProvider(this);

        mPlayerViewModel = viewModelProvider.get(PlayerViewModel.class);
        mHistoryViewModel = viewModelProvider.get(HistoryViewModel.class);

        PlayerUtil.initPlayerViewModel(this, mPlayerViewModel, AppPlayerService.class);
        setPlayerClient(mPlayerViewModel.getPlayerClient());
    }

    private void initRecyclerView() {
        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        mHistoryAdapter = new HistoryAdapter(Objects.requireNonNull(mHistoryViewModel.getHistory().getValue()));
        rvHistory.setAdapter(mHistoryAdapter);

        mHistoryAdapter.setOnItemClickListener(new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(int position, @NonNull Music music) {
                playMusic(position);
            }

            @Override
            public void onRemoveClicked(int position, @NonNull Music music) {
                removeHistory(music);
            }
        });

        mHistoryViewModel.getHistory()
                .observe(this, history -> mHistoryAdapter.setHistory(history));
    }

    private void playMusic(int position) {
        MessageDialog messageDialog = new MessageDialog.Builder(getApplicationContext())
                .setMessage(R.string.message_play_all_music)
                .setPositiveButtonClickListener((dialog, which) -> {
                    Playlist playlist = MusicListUtil.asPlaylist(MusicStore.MUSIC_LIST_HISTORY, mHistoryViewModel.getAllHistory(), position);

                    mPlayerViewModel.setPlaylist(playlist, position, true);
                })
                .build();

        messageDialog.show(getSupportFragmentManager(), "playMusic");
    }

    private void removeHistory(Music music) {
        MessageDialog messageDialog = new MessageDialog.Builder(getApplicationContext())
                .setMessage(R.string.message_remove_history)
                .setPositiveTextColor(getResources().getColor(R.color.red_500))
                .setPositiveButtonClickListener((dialog, which) -> mHistoryViewModel.removeHistory(music))
                .build();

        messageDialog.show(getSupportFragmentManager(), "removeHistory");
    }
}