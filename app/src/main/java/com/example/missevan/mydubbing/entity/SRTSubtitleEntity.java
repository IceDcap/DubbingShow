package com.example.missevan.mydubbing.entity;

import android.util.Log;

/**
 * Created by missevan on 2017/4/24.
 */

public class SRTSubtitleEntity extends SRTEntity {
    public static final int ENDTYPE = 3;
    public static final int SHOWLONGBREAK_TYPE = -2;
    public static final int SHOWNORMAL_TYPE = 1;
    public static final int SHOWROLE_TYPE = 2;
    private boolean isShowAnim = false;
    private boolean retracementFlag = false;
    private int type;

    public SRTSubtitleEntity(int type) {
        this.type = type;
    }

    public SRTSubtitleEntity(int type, SRTEntity srtEntity, boolean isShowAnim) {
        this.type = type;
        if (srtEntity != null) {
            setContent(srtEntity.getContent());
            setEndtime(srtEntity.getEndtime());
            setRole(srtEntity.getRole());
            setStarttime(srtEntity.getStarttime());
        }
        this.isShowAnim = isShowAnim;
    }

    public SRTSubtitleEntity(int type, String role, int starttime, int endtime, String content, boolean isShowAnim) {
        this.type = type;
        setContent(content);
        setEndtime(endtime);
        setRole(role);
        setStarttime(starttime);
        this.isShowAnim = isShowAnim;
    }

    public SRTSubtitleEntity(String role, int starttime, int endtime, String content) {
        super(role, starttime, endtime, content);
    }

    public int getType() {
        return this.type;
    }

    public boolean isRetracementFlag() {
        return this.retracementFlag;
    }

    public boolean isShowAnim() {
        return this.isShowAnim;
    }

    public void setIsShowAnim(boolean isShowAnim) {
        this.isShowAnim = isShowAnim;
    }

    public void setRetracementFlag(boolean retracementFlag) {
        this.retracementFlag = retracementFlag;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
