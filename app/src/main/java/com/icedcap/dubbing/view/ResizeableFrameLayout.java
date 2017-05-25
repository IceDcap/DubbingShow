package com.icedcap.dubbing.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by dsq on 2017/5/25.
 */

public class ResizeableFrameLayout extends FrameLayout {
    public ResizeableFrameLayout(Context context) {
        super(context);
    }

    public ResizeableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizeableFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // measure mode
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // Get measured sizes
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = (int) (width * 9 / 16f);

        final int heightMS = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMS);
    }
}
