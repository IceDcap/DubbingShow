package com.example.missevan.mydubbing.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.missevan.mydubbing.R;
import com.example.missevan.mydubbing.utils.DimenUtil;

/**
 * Created by dsq on 2017/5/5.
 * The custom editor view that modify magic voice for finished dubbing art.
 */
public class UprightModifierView extends FrameLayout implements View.OnTouchListener {
    private RoundCornerProgressBar mMixVoiceProgressBar;
    private RoundCornerProgressBar mSpaceProgressBar;
    private RoundCornerProgressBar mEchoProgressBar;
    private LinearLayout mMixVoiceProgress;
    private LinearLayout mSpaceProgress;
    private LinearLayout mEchoProgress;
    private TextView mProgressIndicator;
    private View mScalePlate;
    private int mDegreeTextWidth;
    private int mProgressBarAreaWidth;
    private int mCanDragAreaRange;
    private int mProgressBarHeight;

    private int mMixPositionX;
    private int mMixPositionY;
    private int mSpacePositionX;
    private int mSpacePositionY;
    private int mEchoPositionX;
    private int mEchoPositionY;

    private OnModifierListener mOnModifierListener;

    public UprightModifierView(Context context) {
        this(context, null);
    }

    public UprightModifierView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UprightModifierView(Context context, AttributeSet attrs, int defStyleAttr) {
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

    private void init(final Context context, AttributeSet attrs, int defStyleAttr) {
        mCanDragAreaRange = DimenUtil.dip2px(context, 40);
        mDegreeTextWidth = DimenUtil.dip2px(context, 20);
        final LayoutInflater inflater = LayoutInflater.from(context);
        // init background view
        mScalePlate = inflater.inflate(R.layout.layout_scale_plate, null, false);

        mMixVoiceProgressBar = (RoundCornerProgressBar) inflater.inflate(R.layout.voice_control_bar, null, false);
        mMixVoiceProgressBar.setId(R.id.mix_voice_progress_bar);
        mMixVoiceProgress = (LinearLayout) mMixVoiceProgressBar.findViewById(R.id.layout_progress);
        mMixVoiceProgressBar.setTitle("混响");

        mSpaceProgressBar = (RoundCornerProgressBar) inflater.inflate(R.layout.voice_control_bar, null, false);
        mSpaceProgressBar.setId(R.id.space_progress_bar);
        mSpaceProgress = (LinearLayout) mSpaceProgressBar.findViewById(R.id.layout_progress);
        mSpaceProgressBar.setTitle("空间");

        mEchoProgressBar = (RoundCornerProgressBar) inflater.inflate(R.layout.voice_control_bar, null, false);
        mEchoProgressBar.setId(R.id.echo_progress_bar);
        mEchoProgress = (LinearLayout) mEchoProgressBar.findViewById(R.id.layout_progress);
        mEchoProgressBar.setTitle("回声");

        mMixVoiceProgressBar.setOnTouchListener(this);
        mSpaceProgressBar.setOnTouchListener(this);
        mEchoProgressBar.setOnTouchListener(this);

        mProgressIndicator = new TextView(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mProgressIndicator.setBackground(getResources().getDrawable(R.drawable.bg_progress_indicator));
        } else {
            mProgressIndicator.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_progress_indicator));
        }
        mProgressIndicator.setGravity(Gravity.CENTER);
        mProgressIndicator.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
        mProgressIndicator.setTextColor(0xffd8d8d8);

        mMixVoiceProgressBar.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mMixPositionX = left + (right - left) / 2;
                mMixPositionY = ((RoundCornerProgressBar) v).getLayoutProgress().getTop() + DimenUtil.dip2px(context, 20);
            }
        });

        mSpaceProgressBar.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mSpacePositionX = left + (right - left) / 2;
                mSpacePositionY = ((RoundCornerProgressBar) v).getLayoutProgress().getTop() + DimenUtil.dip2px(context, 20);
            }
        });

        mEchoProgressBar.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mEchoPositionX = left + (right - left) / 2;
                mEchoPositionY = ((RoundCornerProgressBar) v).getLayoutProgress().getTop() + DimenUtil.dip2px(context, 20);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        mProgressBarAreaWidth = width;// - mDegreeTextWidth;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawViews();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        drawViews();
    }

    private void drawViews() {
        removeAllViews();
        addView(mScalePlate);
        final FrameLayout.LayoutParams mix = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mix.leftMargin = mDegreeTextWidth;
        addView(mMixVoiceProgressBar, mix);

        final int viewSpace = mProgressBarAreaWidth / 3;


        final FrameLayout.LayoutParams space = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        space.leftMargin = mDegreeTextWidth + viewSpace;
        addView(mSpaceProgressBar, space);

        final FrameLayout.LayoutParams echo = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        echo.leftMargin = mDegreeTextWidth + 2 * viewSpace;
        addView(mEchoProgressBar, echo);

    }

    private void drawProgressIndicator(RoundCornerProgressBar bar) {
        mProgressIndicator.setText(String.format("%d%%", (int) bar.getProgress()));

        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        final int progressIndicatorWidth = mProgressIndicator.getWidth() == 0 ?
                DimenUtil.dip2px(getContext(), 24) : mProgressIndicator.getWidth();
        final int progressIndicatorHeight =  mProgressIndicator.getHeight() == 0 ?
                DimenUtil.dip2px(getContext(), 18) : mProgressIndicator.getHeight();
        switch (bar.getId()) {
            case R.id.mix_voice_progress_bar:
                params.leftMargin = mMixPositionX - progressIndicatorWidth / 2;
                params.topMargin = mMixPositionY - progressIndicatorHeight - DimenUtil.dip2px(getContext(), 8);
                break;
            case R.id.space_progress_bar:
                params.leftMargin = mSpacePositionX - progressIndicatorWidth / 2;
                params.topMargin = mSpacePositionY - progressIndicatorHeight - DimenUtil.dip2px(getContext(), 8);
                break;
            case R.id.echo_progress_bar:
                params.leftMargin = mEchoPositionX - progressIndicatorWidth / 2;
                params.topMargin = mEchoPositionY - progressIndicatorHeight - DimenUtil.dip2px(getContext(), 8);
                break;
        }
        params.topMargin = params.topMargin < 0 ? 0 : params.topMargin;

        removeView(mProgressIndicator);
        addView(mProgressIndicator, params);
    }

    private void removeProgressIndicator(RoundCornerProgressBar bar) {
        removeView(mProgressIndicator);
    }


    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE &&
                changedView instanceof UprightModifierView &&
                ((UprightModifierView) changedView).getChildCount() == 0) {
            drawViews();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final float totalHeight = mMixVoiceProgressBar.getSecondaryProgressHeight();
        final float touchY = event.getY();
        int barTopY = 0;
        if (v.getId() == R.id.mix_voice_progress_bar) {
            barTopY = mMixVoiceProgress.getTop();
            mMixVoiceProgressBar.getHeight();
        } else if (v.getId() == R.id.space_progress_bar) {
            barTopY = mSpaceProgress.getTop();
        } else if (v.getId() == R.id.echo_progress_bar) {
            barTopY = mEchoProgress.getTop();
        }
        final float centerY = barTopY + mCanDragAreaRange / 2;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // control the touch area locate the right place
                if (Math.abs(touchY - centerY) < mCanDragAreaRange / 2) {
                    // toggle progress indicator
                    drawProgressIndicator((RoundCornerProgressBar) v);
                } else {
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float progress = (totalHeight - touchY) / totalHeight * 100;
                progress = progress <= 0 ? 0: progress >= 100 ? 100 : progress;
                RoundCornerProgressBar rb = (RoundCornerProgressBar) v;
                rb.setProgress(progress);
                drawProgressIndicator((RoundCornerProgressBar) v);
                if (mOnModifierListener != null) {
                    mOnModifierListener.onModifying(v, progress);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            default:
                // Reset values
                removeProgressIndicator((RoundCornerProgressBar) v);
                if (mOnModifierListener != null) {
                    float p = (totalHeight - touchY) / totalHeight * 100;
                    p = p <= 0 ? 0: p >= 100 ? 100 : p;
                    mOnModifierListener.onModified(v, p);
                }
                break;
        }

        // If we have parent, so requestDisallowInterceptTouchEvent
        if (event.getAction() == MotionEvent.ACTION_MOVE && getParent() != null)
            getParent().requestDisallowInterceptTouchEvent(true);

        return true;
    }


    public void setOnModifierListener(OnModifierListener onModifierListener) {
        mOnModifierListener = onModifierListener;
    }

    public interface OnModifierListener {
        void onModified(View view, float progress);

        void onModifying(View view, float progress);
    }
}
