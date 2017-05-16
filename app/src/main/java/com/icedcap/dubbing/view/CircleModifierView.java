package com.icedcap.dubbing.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.SweepGradient;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.icedcap.dubbing.R;
import com.icedcap.dubbing.utils.DimenUtil;

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
    private static final int UN_ENABLE_START_COLOR = 0xff532e2b;
    private static final int UN_ENABLE_END_COLOR = 0xff513230;
    private static final int DEFAULT_PROGRESS = 100;
    private static final int DEFAULT_TEXT_SIZE = 8;
    private static final int DEFAULT_TEXT_COLOR = 0xffbdbdbd;
    private static final int DEFAULT_PROGRESS_TEXT = 100;
    public static final int DEFAULT_MAX_PROGRESS = 200;
    private static final String DEFAULT_MAX_TEXT = "MAX";
    private static final String DEFAULT_MIN_TEXT = "MIN";

    private int mModifierProgress = DEFAULT_PROGRESS_TEXT;
    private String mMaxText = DEFAULT_MAX_TEXT;
    private String mMinText = DEFAULT_MIN_TEXT;
    private int[] mProgressColor = new int[2];

    private ArcProgressStackView mStackView;
    private TextView mModifierTitleTv;
    private TextView mModifierProgressTv;
    private TextView mMaxTv;
    private TextView mMinTv;
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
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ArcProgressStackView, defStyleAttr, 0);
        mMaxText = typedArray.getString(R.styleable.ArcProgressStackView_modifier_max_text);
        if (TextUtils.isEmpty(mMaxText)) {
            mMaxText = DEFAULT_MAX_TEXT;
        }
        mMinText = typedArray.getString(R.styleable.ArcProgressStackView_modifier_min_text);
        if (TextUtils.isEmpty(mMinText)) {
            mMinText = DEFAULT_MIN_TEXT;
        }
        typedArray.recycle();
        final ArcProgressStackView.Model model = new ArcProgressStackView.Model("", DEFAULT_PROGRESS,
                BACKGROUND_COLOR, new int[]{END_COLOR, START_COLOR});
        mModels.add(model);
        mStackView = new ArcProgressStackView(context, attributeSet, defStyleAttr);
        mStackView.setModels(mModels);

        mMaxTv = new TextView(context);
        mMaxTv.setText(mMaxText);
        mMaxTv.setTextColor(DEFAULT_TEXT_COLOR);
        mMaxTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE);

        mMinTv = new TextView(context);
        mMinTv.setText(mMinText);
        mMinTv.setTextColor(DEFAULT_TEXT_COLOR);
        mMinTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE);

        final FrameLayout.LayoutParams maxlp = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.START | Gravity.LEFT | Gravity.BOTTOM);
        addView(mMaxTv, maxlp);

        final FrameLayout.LayoutParams minlp = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.END | Gravity.RIGHT | Gravity.BOTTOM);
        addView(mMinTv, minlp);

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
        mContentView.setOnTouchListener(mStackView);
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


    @Override
    public void setEnabled(boolean enabled) {
        onViewEnable(enabled);
        super.setEnabled(enabled);
    }

    private void onViewEnable(boolean enable) {
        final int textColor = enable ? DEFAULT_TEXT_COLOR : 0xff3d3d3d;
        mMaxTv.setTextColor(textColor);
        mMinTv.setTextColor(textColor);
        //change progress color to gray & can not to drag
        mStackView.setIsDragged(enable);
        final ArcProgressStackView.Model model = mStackView.getModels().get(0);
        mProgressColor[0] = enable ? END_COLOR : UN_ENABLE_END_COLOR;
        mProgressColor[1] = enable ? START_COLOR : UN_ENABLE_START_COLOR;
        model.setColors(mProgressColor);
        model.setSweepGradient(new SweepGradient(
                model.getBounds().centerX(),
                model.getBounds().centerY(),
                mProgressColor, null));
        mStackView.postInvalidate();
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

    public void setBottomText(String max, String min) {

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
        mModifierProgress = (int) progress;
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
