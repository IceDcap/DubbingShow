package com.example.missevan.mydubbing.entity;

/**
 * Created by missevan on 2017/4/24.
 */

public class SRTEntity {
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

    public void setContent(String paramString) {
        this.content = paramString;
    }

    public void setEndtime(int paramInt) {
        this.endtime = paramInt;
    }

    public void setRole(String paramString) {
        this.role = paramString;
    }

    public void setStarttime(int paramInt) {
        this.starttime = paramInt;
    }

    public String toString() {
        return "SRTEntity{role='" + this.role + '\'' + ", starttime=" + this.starttime + ", endtime=" + this.endtime + ", content='" + this.content + '\'' + '}';
    }
}
