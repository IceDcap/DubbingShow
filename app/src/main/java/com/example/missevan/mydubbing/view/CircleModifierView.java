package com.example.missevan.mydubbing.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.example.missevan.mydubbing.utils.DimenUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dsq on 2017/5/5.
 * <p>
 * The custom editor view that modify volume for finished dubbing art.
 */
public class CircleModifierView extends FrameLayout {
    private ArcProgressStackView mArcProgressStackView;
    private List<ArcProgressStackView.Model> mModels = new ArrayList<>();
    private final int mBackgroundColor = 0xff282020;
    private final int mStartColor = 0xffb23939;
    private final int mEndColor = 0xffad4545;
    private int mSize;


    public CircleModifierView(Context context) {
        this(context, null);
    }

    public CircleModifierView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleModifierView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attributeSet, int defStyleAttr) {
        final ArcProgressStackView.Model model = new ArcProgressStackView.Model("", 50,
                mBackgroundColor, new int[]{mEndColor, mStartColor});
        mModels.add(model);
        mArcProgressStackView = new ArcProgressStackView(context, attributeSet, defStyleAttr);
//        mArcProgressStackView.setIsRounded(true);
//        mArcProgressStackView.setIsDragged(true);
//        mArcProgressStackView.setModelBgEnabled(true);
        mArcProgressStackView.setModels(mModels);
//        mArcProgressStackView.setIsAnimated(false);
//        addView(mArcProgressStackView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // Get measured sizes
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        mSize = Math.min(width, height);

        int spec = widthMeasureSpec < heightMeasureSpec ? widthMeasureSpec : heightMeasureSpec;

        super.onMeasure(spec, spec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)getLayoutParams();
        final int margin = DimenUtil.dip2px(getContext(), 8);
        lp.setMargins(margin, margin, margin, margin);
        lp.gravity = Gravity.CENTER;
        removeAllViews();
        addView(mArcProgressStackView, lp);
    }
}
