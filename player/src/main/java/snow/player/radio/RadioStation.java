package snow.player.radio;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import snow.player.media.MusicItem;

/**
 * 用于存储 “电台” 的基本信息。
 * <p>
 * 如果需要存储一些额外的信息，可以使用 {@link #setExtra(Bundle)} 方法与 {@link #getExtra()} 方法。
 */
public class RadioStation implements Parcelable {
    private String mId;
    private String mName;
    private String mDescription;
    private Bundle mExtra;

    /**
     * 创建一个 RadioStation 对象。
     *
     * @param id          电台的 id（不能为 null）
     * @param name        电台的名称（不能为 null）
     * @param description 电台的描述信息（不能为 null）
     */
    public RadioStation(@NonNull String id, @NonNull String name, @NonNull String description) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(description);

        mId = id;
        mName = name;
        mDescription = description;
    }

    /**
     * 拷贝构造器。
     */
    public RadioStation(@NonNull RadioStation source) {
        Preconditions.checkNotNull(source);

        mId = source.mId;
        mName = source.mName;
        mDescription = source.mDescription;
        mExtra = source.mExtra;
    }

    /**
     * 创建一个默认的 RadioStation 对象。
     * <p>
     * 相当于：RadioStation("", "unknown", "")
     */
    public RadioStation() {
        mId = "";
        mName = "unknown";
        mDescription = "";
    }

    protected RadioStation(Parcel in) {
        mId = in.readString();
        mName = in.readString();
        mDescription = in.readString();
        mExtra = in.readBundle(Thread.currentThread().getContextClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mName);
        dest.writeString(mDescription);
        dest.writeBundle(mExtra);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RadioStation> CREATOR = new Creator<RadioStation>() {
        @Override
        public RadioStation createFromParcel(Parcel in) {
            return new RadioStation(in);
        }

        @Override
        public RadioStation[] newArray(int size) {
            return new RadioStation[size];
        }
    };

    public String getId() {
        return mId;
    }

    public void setId(@NonNull String id) {
        Preconditions.checkNotNull(id);
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(@NonNull String name) {
        Preconditions.checkNotNull(name);
        mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(@NonNull String description) {
        Preconditions.checkNotNull(description);
        mDescription = description;
    }

    @Nullable
    public Bundle getExtra() {
        return mExtra;
    }

    public void setExtra(@Nullable Bundle extra) {
        mExtra = extra;
    }

    /**
     * 两个 RadioStation 对象相等的条件：两个对象的 id、name、description 都相等，忽略携带的 Extra.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof RadioStation)) {
            return false;
        }

        RadioStation other = (RadioStation) obj;

        return Objects.equal(mId, other.mId)
                && Objects.equal(mName, other.mName)
                && Objects.equal(mDescription, other.mDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mName, mDescription);
    }

    /**
     * @return 格式：[id, name, description]
     */
    @NonNull
    @Override
    public String toString() {
        return "[" + mId + ", " + mName + ", " + mDescription + "]";
    }

    /**
     * 用于提供电台要播放的音乐。
     * <p>
     * {@link RadioStation} 本身并不包含任何 {@link MusicItem} 对象，而是通过将一个 {@link RadioStation}
     * 对象传递给一个 {@link MusicItemProvider} 对象来获取要播放的 {@link MusicItem} 对象。
     */
    public interface MusicItemProvider {
        /**
         * 获取电台的下一首音乐。
         * <p>
         * 该方法会在异步线程中调用，因此可以执行耗时操作，例如访问数据库或者网络。
         *
         * @param radioStation 正在播放的电台
         * @return 电台中要播放的音乐
         */
        MusicItem next(RadioStation radioStation);
    }
}
