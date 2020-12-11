package snow.player.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;

import java.util.ArrayList;
import java.util.List;

import snow.player.ui.R;
import snow.player.ui.equalizer.EqualizerViewModel;

public class EqualizerBandView extends LinearLayout {
    private List<Band> mAllBand;
    private LayoutInflater mLayoutInflater;

    @Nullable
    private OnBandChangeListener mOnBandChangeListener;

    public EqualizerBandView(Context context) {
        this(context, null);
    }

    public EqualizerBandView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EqualizerBandView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mAllBand = new ArrayList<>();
        mLayoutInflater = LayoutInflater.from(context);

        setBaselineAligned(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setLayoutDirection(LAYOUT_DIRECTION_LTR);
        }
    }

    public void init(@NonNull EqualizerViewModel equalizerViewModel) {
        int numberOfBands = equalizerViewModel.getEqualizerNumberOfBands();
        for (int band = 0; band < numberOfBands; band++) {
            addBand(new Band((short) band, equalizerViewModel));
        }
    }

    public void setOnBandChangeListener(@Nullable OnBandChangeListener onBandChangeListener) {
        mOnBandChangeListener = onBandChangeListener;

        for (Band band : mAllBand) {
            band.setOnBandChangeListener(mOnBandChangeListener);
        }
    }

    private void addBand(Band band) {
        mAllBand.add(band);
        band.setOnBandChangeListener(mOnBandChangeListener);
        View itemView = band.createItemView(mLayoutInflater, this);

        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1.0F;

        addView(itemView, layoutParams);
    }

    public void notifyEqualizerSettingChanged() {
        for (Band band : mAllBand) {
            band.notifyItemDataChanged();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        for (Band band : mAllBand) {
            band.getSeekBar().setEnabled(enabled);
        }
    }

    public static class Band {
        private EqualizerViewModel mEqualizerViewModel;
        private short mBand;

        private VerticalSeekBar seekBar;
        private TextView tvText;

        @Nullable
        private OnBandChangeListener mOnBandChangeListener;

        public Band(short band, @NonNull EqualizerViewModel equalizerViewModel) {
            mEqualizerViewModel = equalizerViewModel;
            mBand = band;
        }

        public VerticalSeekBar getSeekBar() {
            return seekBar;
        }

        public void setOnBandChangeListener(@Nullable OnBandChangeListener onBandChangeListener) {
            mOnBandChangeListener = onBandChangeListener;
        }

        public void notifyItemDataChanged() {
            short[] bandLevelRange = mEqualizerViewModel.getEqualizerBandLevelRange();
            short bandLevel = mEqualizerViewModel.getEqualizerBandLevel(mBand);
            int progress = bandLevel - bandLevelRange[0];
            if (mEqualizerViewModel.getEqualizerCurrentPreset() == 0) {
                progress = -bandLevelRange[0];
            }
            seekBar.setProgress(progress);
        }

        public View createItemView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
            View itemView = inflater.inflate(R.layout.snow_ui_item_equalizer_band, parent, false);

            seekBar = itemView.findViewById(R.id.seekBar);
            tvText = itemView.findViewById(R.id.tvText);

            initViews();

            return itemView;
        }

        @SuppressLint("SetTextI18n")
        private void initViews() {
            short[] bandLevelRange = mEqualizerViewModel.getEqualizerBandLevelRange();
            short minLevel = bandLevelRange[0];
            short maxLevel = bandLevelRange[1];
            short bandLevel = mEqualizerViewModel.getEqualizerBandLevel(mBand);
            int centerFreq = mEqualizerViewModel.getEqualizerCenterFreq(mBand);

            final int max = maxLevel - minLevel;
            final int center = max / 2;
            seekBar.setMax(max);

            int progress = bandLevel - minLevel;
            if (mEqualizerViewModel.getEqualizerCurrentPreset() == 0) {
                progress = center;
            }
            seekBar.setProgress(progress);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        mEqualizerViewModel.setEqualizerBandLevel(mBand, (short) (progress - center));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if (mOnBandChangeListener != null) {
                        mOnBandChangeListener.onBandChanged();
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mEqualizerViewModel.applyChanges();
                }
            });

            tvText.setText((centerFreq / 1000) + "Hz");
        }
    }

    public interface OnBandChangeListener {
        void onBandChanged();
    }
}
