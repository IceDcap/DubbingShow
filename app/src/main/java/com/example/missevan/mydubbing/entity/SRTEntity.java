package com.example.missevan.mydubbing.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by missevan on 2017/4/24.
 */

public class SRTEntity implements Parcelable {
    private String content;
    private int endtime;
    private String role;
    private int starttime;

    public SRTEntity() {
    }

    public SRTEntity(String role, int starttime, int endtime, String content) {
        this.role = role;
        this.starttime = starttime;
        this.endtime = endtime;
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public int getEndtime() {
        return this.endtime;
    }

    public String getRole() {
        return this.role;
    }

    public int getStarttime() {
        return this.starttime;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setEndtime(int endtime) {
        this.endtime = endtime;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setStarttime(int starttime) {
        this.starttime = starttime;
    }

    public String toString() {
        return "SRTEntity{role='" + this.role + '\'' + ", starttime=" + this.starttime + ", endtime=" + this.endtime + ", content='" + this.content + '\'' + '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.content);
        dest.writeInt(this.endtime);
        dest.writeString(this.role);
        dest.writeInt(this.starttime);
    }

    protected SRTEntity(Parcel in) {
        this.content = in.readString();
        this.endtime = in.readInt();
        this.role = in.readString();
        this.starttime = in.readInt();
    }

    public static final Parcelable.Creator<SRTEntity> CREATOR = new Parcelable.Creator<SRTEntity>() {
        @Override
        public SRTEntity createFromParcel(Parcel source) {
            return new SRTEntity(source);
        }

        @Override
        public SRTEntity[] newArray(int size) {
            return new SRTEntity[size];
        }
    };
}
