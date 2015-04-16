package cn.nekocode.toolbox.beans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by nekocode on 2015/4/14 0014.
 */
public class ToolBean implements Parcelable {
    private String objectId;
    private String iconUrl;
    private String title;
    private String linkUrl;
    private String type;

    public ToolBean(ToolBean toolBean) {
        this(toolBean.getObjectId(), toolBean.getIconUrl(), toolBean.getTitle(), toolBean.getLinkUrl(), toolBean.getType());
    }

    public ToolBean(String objectId, String iconUrl, String title, String linkUrl, String type) {
        this.objectId = objectId;
        this.iconUrl = iconUrl;
        this.title = title;
        this.linkUrl = linkUrl;
        this.type = type;
    }

    public ToolBean() {
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.objectId);
        dest.writeString(this.iconUrl);
        dest.writeString(this.title);
        dest.writeString(this.linkUrl);
        dest.writeString(this.type);
    }

    private ToolBean(Parcel in) {
        this.objectId = in.readString();
        this.iconUrl = in.readString();
        this.title = in.readString();
        this.linkUrl = in.readString();
        this.type = in.readString();
    }

    public static final Parcelable.Creator<ToolBean> CREATOR = new Parcelable.Creator<ToolBean>() {
        public ToolBean createFromParcel(Parcel source) {
            return new ToolBean(source);
        }

        public ToolBean[] newArray(int size) {
            return new ToolBean[size];
        }
    };
}
