package com.icedcap.dubbing.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.SweepGradient;
import android.os.Build;
import android.support.annotation.RequiresApi;
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


/**
 * Created by dsq on 2017/6/6.
 * <p>
 * The custom editor view that modify volume for finished dubbing art.
 */
public class CircleModifierLayout extends FrameLayout implements CircleProgressBar.OnDragProgressListener{
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

    private CircleProgressBar mStackView;
    private TextView mModifierTitleTv;
    private TextView mModifierProgressTv;
    private TextView mMaxTv;
    private TextView mMinTv;
    private View mContentView;

    private String mMaxText = DEFAULT_MAX_TEXT;
    private String mMinText = DEFAULT_MIN_TEXT;
    private int[] mProgressColor = new int[2];

    private int mSize;
    private OnModifierListener mOnModifierListener;
    public CircleModifierLayout(Context context) {
        this(context, null);
    }

    public CircleModifierLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleModifierLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            previewLayout(context);
        } else {
            init(context, attrs, defStyleAttr);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CircleModifierLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CircleProgressBar, defStyleAttr, 0);
        mMaxText = typedArray.getString(R.styleable.CircleProgressBar_modifier_max_text);
        if (TextUtils.isEmpty(mMaxText)) {
            mMaxText = DEFAULT_MAX_TEXT;
        }
        mMinText = typedArray.getString(R.styleable.CircleProgressBar_modifier_min_text);
        if (TextUtils.isEmpty(mMinText)) {
            mMinText = DEFAULT_MIN_TEXT;
        }
        typedArray.recycle();
        mStackView = new CircleProgressBar(context, attributeSet, defStyleAttr);

        mMaxTv = new TextView(context);
        mMaxTv.setPadding(0, 10, 10, 0);
        mMaxTv.setText(mMaxText);
        mMaxTv.setTextColor(DEFAULT_TEXT_COLOR);
        mMaxTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE);
        mMaxTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mStackView.setSmoothProgress(DEFAULT_MAX_PROGRESS);
            }
        });

        mMinTv = new TextView(context);
        mMinTv.setText(mMinText);
        mMinTv.setPadding(10, 10, 0, 0);
        mMinTv.setTextColor(DEFAULT_TEXT_COLOR);
        mMinTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE);
        mMinTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mStackView.setSmoothProgress(0);
            }
        });


        final LayoutParams stackviewlp = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.END | Gravity.RIGHT | Gravity.BOTTOM);
        stackviewlp.gravity = Gravity.CENTER;
        addView(mStackView, stackviewlp);

        final LayoutParams maxlp = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.START | Gravity.LEFT | Gravity.BOTTOM);
        addView(mMaxTv, maxlp);

        final LayoutParams minlp = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.END | Gravity.RIGHT | Gravity.BOTTOM);
        addView(mMinTv, minlp);

        mContentView = LayoutInflater.from(context).
                inflate(R.layout.volume_modifier_content, null, false);
        mModifierProgressTv = (TextView) mContentView.findViewById(R.id.modifier_progress_text);
        mModifierTitleTv = (TextView) mContentView.findViewById(R.id.modifier_title);
        final LayoutParams contentlp = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        addView(mContentView, contentlp);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
        mProgressColor[0] = enable ? END_COLOR : UN_ENABLE_END_COLOR;
        mProgressColor[1] = enable ? START_COLOR : UN_ENABLE_START_COLOR;
        mStackView.setColors(mProgressColor);
        mStackView.setSweepGradient(new SweepGradient(
                mStackView.getModelBound().centerX(),
                mStackView.getModelBound().centerY(),
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
        mStackView.setOnDragProgressListener(this);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        mStackView.setOnDragProgressListener(null);
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
