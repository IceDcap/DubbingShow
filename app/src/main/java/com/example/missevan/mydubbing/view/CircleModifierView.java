package com.example.missevan.mydubbing.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.missevan.mydubbing.R;
import com.example.missevan.mydubbing.utils.DimenUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dsq on 2017/5/5.
 * <p>
 * The custom editor view that modify volume for finished dubbing art.
 */
public class CircleModifierView extends FrameLayout implements ArcProgressStackView.OnDragProgressListener {
    private List<ArcProgressStackView.Model> mModels = new ArrayList<>();
    private static final int BACKGROUND_COLOR = 0xff282020;
    private static final int START_COLOR = 0xffb23939;
    private static final int END_COLOR = 0xffad4545;
    private static final int DEFAULT_PROGRESS = 50;
    private static final int DEFAULT_TEXT_SIZE = 8;
    private static final int DEFAULT_TEXT_COLOR = 0xffbdbdbd;
    private static final int DEFAULT_PROGRESS_TEXT = 100;
    private static final int DEFAULT_MAX_PROGRESS = 200;
    private static final String DEFAULT_MAX_TEXT = "MAX";
    private static final String DEFAULT_MIN_TEXT = "MIN";

    private int mModifierProgress = DEFAULT_PROGRESS_TEXT;
    private String mMaxText = DEFAULT_MAX_TEXT;
    private String mMinText = DEFAULT_MIN_TEXT;

    private ArcProgressStackView mStackView;
    private TextView mModifierTitleTv;
    private TextView mModifierProgressTv;
    private View mContentView;

    private int mSize;
    private OnModifierListener mOnModifierListener;

    public CircleModifierView(Context context) {
        this(context, null);
    }

    public CircleModifierView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleModifierView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            previewLayout(context);
        } else {
            init(context, attrs, defStyleAttr);
        }
    }

    private void previewLayout(Context context) {
        TextView tv = new TextView(context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        tv.setLayoutParams(params);
        tv.setGravity(Gravity.CENTER);
        tv.setText(getClass().getSimpleName());
        tv.setTextColor(Color.WHITE);
        tv.setBackgroundColor(Color.GRAY);
        addView(tv);
    }

    private void init(Context context, AttributeSet attributeSet, int defStyleAttr) {
        final ArcProgressStackView.Model model = new ArcProgressStackView.Model("", DEFAULT_PROGRESS,
                BACKGROUND_COLOR, new int[]{END_COLOR, START_COLOR});
        mModels.add(model);
        mStackView = new ArcProgressStackView(context, attributeSet, defStyleAttr);
        mStackView.setModels(mModels);

        final TextView maxTV = new TextView(context);
        maxTV.setText(mMaxText);
        maxTV.setTextColor(DEFAULT_TEXT_COLOR);
        maxTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE);

        final TextView minTV = new TextView(context);
        minTV.setText(mMinText);
        minTV.setTextColor(DEFAULT_TEXT_COLOR);
        minTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE);

        final FrameLayout.LayoutParams maxlp = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.START | Gravity.LEFT | Gravity.BOTTOM);
        addView(maxTV, maxlp);

        final FrameLayout.LayoutParams minlp = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.END | Gravity.RIGHT | Gravity.BOTTOM);
        addView(minTV, minlp);

        final FrameLayout.LayoutParams stackviewlp = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.END | Gravity.RIGHT | Gravity.BOTTOM);
        final int margin = DimenUtil.dip2px(getContext(), 8);
        stackviewlp.setMargins(margin, margin, margin, margin);
        stackviewlp.gravity = Gravity.CENTER;
        addView(mStackView, stackviewlp);

        mContentView = LayoutInflater.from(context).
                inflate(R.layout.volume_modifier_content, null, false);
        mModifierProgressTv = (TextView) mContentView.findViewById(R.id.modifier_progress_text);
        mModifierTitleTv = (TextView) mContentView.findViewById(R.id.modifier_title);
        final FrameLayout.LayoutParams contentlp = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        addView(mContentView, contentlp);
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
        removeView(mContentView);
        addView(mContentView);
    }

    public void setModifierProgress(int modifierProgress) {
        mModifierProgress = modifierProgress;
        mModifierProgressTv.setText(String.valueOf(mModifierProgress));
        requestLayout();
    }

    public void setModifierTitle(String modifierTitle) {
        mModifierTitleTv.setText(modifierTitle);
        requestLayout();
    }

    public int getModifierProgress() {
        return mModifierProgress;
    }

    public int getSize() {
        return mSize;
    }

    public ArcProgressStackView getStackView() {
        return mStackView;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setIsAnimated(final boolean isAnimated) {
        mStackView.setIsAnimated(isAnimated);
    }

    @Override
    public void onProgress(float progress) {
        mModifierProgress = (int) progress * DEFAULT_MAX_PROGRESS / 100;
        mModifierProgressTv.setText(String.valueOf(mModifierProgress));
        requestLayout();
        if (mOnModifierListener != null) {
            mOnModifierListener.onModifying(progress);
        }
    }

    @Override
    public void onModified(float progress) {
        if (mOnModifierListener != null) {
            mOnModifierListener.onModified(progress);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        mStackView.registerListener(this);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        mStackView.unregisterListener(this);
        super.onDetachedFromWindow();
    }

    public void setOnModifierListener(OnModifierListener onModifierListener) {
        mOnModifierListener = onModifierListener;
    }

    public interface OnModifierListener {
        void onModified(float progress);

        void onModifying(float progress);
    }
}
