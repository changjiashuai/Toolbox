package cn.nekocode.toolbox.beans;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by nekocode on 2015/4/15 0015.
 */
public class TypeBean implements Parcelable {
    private String objectId;
    private String name;
    private ArrayList<ToolBean> toolBeans;

    public TypeBean() {
    }

    public TypeBean(String objectId, String name, ArrayList<ToolBean> toolBeans) {
        this.objectId = objectId;
        this.name = name;
        this.toolBeans = toolBeans;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ToolBean> getToolBeans() {
        return toolBeans;
    }

    public void setToolBeans(ArrayList<ToolBean> toolBeans) {
        this.toolBeans = toolBeans;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.objectId);
        dest.writeString(this.name);
        dest.writeSerializable(this.toolBeans);
    }

    private TypeBean(Parcel in) {
        this.objectId = in.readString();
        this.name = in.readString();
        this.toolBeans = (ArrayList<ToolBean>) in.readSerializable();
    }

    public static final Parcelable.Creator<TypeBean> CREATOR = new Parcelable.Creator<TypeBean>() {
        public TypeBean createFromParcel(Parcel source) {
            return new TypeBean(source);
        }

        public TypeBean[] newArray(int size) {
            return new TypeBean[size];
        }
    };
}
