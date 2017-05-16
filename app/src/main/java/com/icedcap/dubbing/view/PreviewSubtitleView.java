package com.icedcap.dubbing.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.icedcap.dubbing.entity.SRTEntity;
import com.icedcap.dubbing.utils.DimenUtil;

import java.util.List;

/**
 * Created by dsq on 2017/5/10.
 */
public class PreviewSubtitleView extends TextView {
    private static final int TEXT_COLOR = 0xffffffff;
    private static final int TEXT_BACKGROUND_COLOR = 0xcc000000;
    private static final int TEXT_SIZE = 14; //SP
    private List<SRTEntity> mSRTEntities;

    public PreviewSubtitleView(Context context) {
        this(context, null);
    }

    public PreviewSubtitleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreviewSubtitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PreviewSubtitleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        final int paddingY = DimenUtil.dip2px(getContext(), 2);
        final int paddingX = DimenUtil.dip2px(getContext(), 5);
        setPadding(paddingX, paddingY, paddingX, paddingY);
        setBackgroundColor(TEXT_BACKGROUND_COLOR);
        setTextColor(TEXT_COLOR);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
        setVisibility(GONE);
    }

    public void setSRTEntities(List<SRTEntity> SRTEntities) {
        mSRTEntities = SRTEntities;
    }

    /** should set data by {@link #setSRTEntities(List)} */
    public void processTime(int time) {
        if (mSRTEntities == null || mSRTEntities.size() < 1) {
            return;
        }

        int index = calculateIndex(time);
        if (index >= 0 && index < mSRTEntities.size()) {
            setText(mSRTEntities.get(index).getContent());
            setVisibility(VISIBLE);
        }else {
            setVisibility(GONE);
        }
    }

    public void reset() {
        setVisibility(GONE);
    }

    private int calculateIndex(int time) {
        for (int i = 0; i < mSRTEntities.size(); i++) {
            SRTEntity entity = mSRTEntities.get(i);

            int start = entity.getStarttime();
            int end = entity.getEndtime();
            if (time >= start && time <= end) {
                return i;
            }
        }
        return -1;
    }
}
