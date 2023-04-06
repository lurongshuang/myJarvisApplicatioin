package snow.music.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import snow.music.widget.LoadingDialog;
import snow.player.PlayerClient;

public class BaseActivity extends AppCompatActivity {
    @Nullable
    private PlayerClient mPlayerClient;
    private LoadingDialog dialog;

    public void setPlayerClient(@Nullable PlayerClient playerClient) {
        mPlayerClient = playerClient;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPlayerClient != null && !mPlayerClient.isConnected()) {
            mPlayerClient.connect();
        }
    }

    public void showLoading() {
        if (dialog == null) {
            dialog = new LoadingDialog(this);
        }
        dialog.show();
    }

    public void clearLoading() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
