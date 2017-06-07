package com.icedcap.dubbing.view;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Build;
import android.support.annotation.FloatRange;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.icedcap.dubbing.R;
import com.icedcap.dubbing.utils.DimenUtil;

/**
 * Created by dsq on 2017/6/6.
 */

public class CircleProgressBar extends View {
    private final static float DEFAULT_START_ANGLE = 270.0F;
    private final static float DEFAULT_SWEEP_ANGLE = 360.0F;
    private final static float DEFAULT_MODEL_SIZE = 14;//dp
    private final static float DEFAULT_MODEL_PADDING = 8;//dp
    private final static int DEFAULT_MODEL_BG_COLOR = 0xff292020;
    // Max and min fraction values
    private final static float MAX_FRACTION = 1.0F;
    private final static float MIN_FRACTION = 0.0F;


    // Max and min end angle
    private final static float MAX_ANGLE = 360.0F;
    private final static float MIN_ANGLE = 0.0F;

    private static final float MAX_PROGRESS = 200;
    private static final float MIN_PROGRESS = 0;
    public final static float DEFAULT_PROGRESS = 100;


    // Action move constants
    private final static float POSITIVE_ANGLE = 90.0F;
    private final static float NEGATIVE_ANGLE = 270.0F;
    private final static int POSITIVE_SLICE = 1;
    private final static int NEGATIVE_SLICE = -1;
    private final static int DEFAULT_SLICE = 0;


    private final static int DEFAULT_ACTION_MOVE_ANIMATION_DURATION = 150;

    private int[] mModelColors = {0xffad4545, 0xffb23939};
    private int mSquareLength;
    private float mSquareRadius;
    private float mModelPadding;
    private RectF mModelBound;
    private Path mModelPath;
    private float mModelSize;
    private float mProgress;
    private float mLastProgress;
    private SweepGradient mSweepGradient;
    private PathMeasure mPathMeasure;

    // Start and end angles
    private float mStartAngle;
    private float mSweepAngle;

    private RectF mTempRectF;
    private float[] mPos = new float[2];
    private float[] mTan = new float[2];

    private boolean isAnimated = true;
    private boolean mIsDragged = true;

    // ValueAnimator and interpolator for progress animating
    private final ValueAnimator mProgressAnimator = new ValueAnimator();
    private ValueAnimator.AnimatorListener mAnimatorListener;
    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener;
    private Interpolator mInterpolator;
    private int mAnimationDuration;
    private float mAnimatedFraction;


    // Is >= VERSION_CODES.HONEYCOMB
    private boolean mIsFeaturesAvailable;

    // paint
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
        {
            setDither(true);
        }
    };

    private final Paint mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
        {
            setDither(true);
            setStyle(Style.FILL);
        }
    };

    private final Paint mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
        {
            setDither(true);
            setStyle(Style.STROKE);
        }
    };

    private final Paint mLevelPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
        {
            setDither(true);
            setStyle(Style.FILL_AND_STROKE);
            setPathEffect(new CornerPathEffect(0.5F));
        }
    };

    private final Paint mMiniCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
        {
            setDither(true);
            setStyle(Style.STROKE);
            setStrokeWidth(DimenUtil.dip2px(getContext(), 0.5f));
            setColor(0xffffffff);
        }
    };

    private OnDragProgressListener mListener;
    private int mActionMoveLastSlice = 0;
    private int mActionMoveSliceCounter;
    private boolean mIsActionMoved;

    public CircleProgressBar(Context context) {
        this(context, null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // Set preview models
        if (isInEditMode()) {
            measure(0, 0);
        } else {
            init(context, attrs, defStyleAttr);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // Set preview models
        if (isInEditMode()) {
            measure(0, 0);
        } else {
            init(context, attrs, defStyleAttr);
        }
    }


    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        // Detect if features available
        mIsFeaturesAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

        mStartAngle = DEFAULT_START_ANGLE;
        mSweepAngle = DEFAULT_SWEEP_ANGLE;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);
        try {
            mPathMeasure = new PathMeasure();
            mModelBound = new RectF();
            mTempRectF = new RectF();
            mModelPath = new Path();
            mTextPaint.setColor(0xffbdbdbd);
            mTextPaint.setTextSize(DimenUtil.sp2px(context, 8));
            mBgPaint.setColor(0xff151515);
            mProgressPaint.setColor(0xffb23939);

            mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressPaint.setStrokeJoin(Paint.Join.ROUND);
            mModelSize = DimenUtil.dip2px(context, DEFAULT_MODEL_SIZE);
            mModelPadding = DimenUtil.dip2px(context, DEFAULT_MODEL_PADDING) + mModelSize * 0.5f;
            mProgressPaint.setStrokeWidth(mModelSize);

            mProgress = typedArray.getFloat(R.styleable.CircleProgressBar_modifier_default_progress, DEFAULT_PROGRESS);
            setProgress(mProgress);

            setAnimated(true);

            // Retrieve interpolator
            Interpolator interpolator = null;
            try {
                final int interpolatorId = typedArray.getResourceId(
                        R.styleable.CircleProgressBar_modifier_interpolator, 0
                );
                interpolator = interpolatorId == 0 ? null :
                        AnimationUtils.loadInterpolator(context, interpolatorId);
            } catch (Resources.NotFoundException exception) {
                interpolator = null;
                exception.printStackTrace();
            } finally {
                setInterpolator(interpolator);
            }


            // Set animation info if is available
            if (mIsFeaturesAvailable) {
                mProgressAnimator.setFloatValues(MIN_FRACTION, MAX_FRACTION);
                mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(final ValueAnimator animation) {
                        mAnimatedFraction = (float) animation.getAnimatedValue();
                        if (mAnimatorUpdateListener != null) {
                            mAnimatorUpdateListener.onAnimationUpdate(animation);
                        }
                        postInvalidate();
                    }
                });
            }

        } finally {
            typedArray.recycle();
        }
    }

    private void initAfterMeasure() {
        mSweepGradient = new SweepGradient(mModelBound.centerX(), mModelBound.centerY(), mModelColors, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);

        //here need a square shape
        mSquareLength = Math.min(width, height);
        mSquareRadius = mSquareLength * 0.5f;

        mModelBound.set(mModelPadding, mModelPadding,
                mSquareLength - mModelPadding, mSquareLength - mModelPadding);
        initAfterMeasure();
        setMeasuredDimension(mSquareLength, mSquareLength);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircleBackground(canvas);
        // Save and rotate to start angle
        canvas.save();
        canvas.rotate(mStartAngle, mSquareRadius, mSquareRadius);

        // Get progress for current model
        float progressFraction = isAnimated && !isInEditMode() ? (mLastProgress + (mAnimatedFraction *
                (mProgress - mLastProgress))) / MAX_PROGRESS :
                mProgress / MAX_PROGRESS;
        final float progress = progressFraction * mSweepAngle;
        // Draw gradient progress or solid
        mProgressPaint.setShader(null);
        mProgressPaint.setStyle(Paint.Style.STROKE);

        mProgressPaint.setColor(DEFAULT_MODEL_BG_COLOR);
        canvas.drawArc(mModelBound, 0.0F, mSweepAngle, false, mProgressPaint);
        if (!isInEditMode()) mProgressPaint.clearShadowLayer();

        // Set model arc progress
        mModelPath.reset();
        mModelPath.addArc(mModelBound, 0, progress);
        mProgressPaint.setShader(mSweepGradient);

        mProgressPaint.setAlpha(255);
        canvas.drawPath(mModelPath, mProgressPaint);

        mPathMeasure.setPath(mModelPath, false);

        if (mProgress != 0) {
            mPathMeasure.getPosTan(0, mPos, mTan);
            mLevelPaint.setColor(mModelColors[0]);

            // Get bounds of start pump
            final float halfSize = mModelSize * 0.5F;
            mTempRectF.set(mPos[0] - halfSize, mPos[1] - halfSize,
                    mPos[0] + halfSize, mPos[1] + halfSize);
            canvas.drawArc(mTempRectF, 0.0F, -180.0F, true, mLevelPaint);

            mPathMeasure.getPosTan(mPathMeasure.getLength(), mPos, mTan);
            drawEndPopupIfIntersect(canvas, progress, mPos[0] - halfSize, mPos[1] - halfSize,
                    mPos[0] + halfSize, mPos[1] + halfSize);
            drawMiniCircle(canvas);
        } else {
            canvas.rotate(360 - mStartAngle, mSquareRadius, mSquareRadius);
            mPos[0] = mModelBound.centerX();
            mPos[1] = mModelPadding;
            drawMiniCircle(canvas);
        }

        // Restore after drawing
        canvas.restore();
    }


    private void drawCircleBackground(Canvas canvas) {
        canvas.drawCircle(mSquareRadius, mSquareRadius, mSquareRadius, mBgPaint);
    }

    private void drawMiniCircle(Canvas canvas) {
        canvas.drawCircle(
                mPos[0],
                mPos[1],
                DimenUtil.dip2px(getContext(), 2.5f),
                mMiniCirclePaint);
    }

    private void drawEndPopupIfIntersect(Canvas canvas, float progress,
                                         float l, float t, float r, float b) {
        final RectF rectF = new RectF(l, t, r, b);
        mLevelPaint.setPathEffect(null);
        if (rectF.intersect(mTempRectF)) {
            mLevelPaint.setColor(mModelColors[1]);
            canvas.drawArc(rectF, progress, progress + 180F, false, mLevelPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsDragged) return super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                // Get current move angle and check whether touched angle is in sweep angle zone
                float currentAngle = getActionMoveAngle(event.getX(), event.getY());
                if (currentAngle > mSweepAngle && currentAngle < MAX_ANGLE) break;

                final float distance = (float) Math.sqrt(Math.pow(event.getX() - mSquareRadius, 2) +
                        Math.pow(event.getY() - mSquareRadius, 2));
                if (distance >= mModelBound.width() * 0.5f - mModelSize /* * 0.5f */&& distance <= mSquareRadius) {
                    mIsActionMoved = true;
                    handleActionMoveModel(event);
                    animateActionMoveProgress();
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsActionMoved) break;
                if (mProgressAnimator.isRunning()) break;
                handleActionMoveModel(event);
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            default:
                // Reset values
                mActionMoveLastSlice = DEFAULT_SLICE;
                mActionMoveSliceCounter = 0;
                mIsActionMoved = false;
                if (mListener != null) {
                    mListener.onModified(mProgress);
                }
        }
        return true;
    }

    private float getActionMoveAngle(final float x, final float y) {
        // Get degrees without offset
        float degrees = (float) ((Math.toDegrees(Math.atan2(y - mSquareRadius, x - mSquareRadius)) + 360.0F) % 360.0F);
        if (degrees < 0) degrees += 2.0F * Math.PI;
        // Get point with offset relative to start angle
        final float newActionMoveX =
                (float) (mSquareRadius * Math.cos((degrees - mStartAngle) / 180.0F * Math.PI));
        final float newActionMoveY =
                (float) (mSquareRadius * Math.sin((degrees - mStartAngle) / 180.0F * Math.PI));

        // Set new angle with offset
        degrees = (float) ((Math.toDegrees(Math.atan2(newActionMoveY, newActionMoveX)) + 360.0F) % 360.0F);
        if (degrees < 0) degrees += 2.0F * Math.PI;

        return degrees;
    }

    private float handleActionMoveModel(final MotionEvent event) {
        // Get current move angle
        float currentAngle = getActionMoveAngle(event.getX(), event.getY());

        // Check if angle in slice zones
        final int actionMoveCurrentSlice;
        if (currentAngle > MIN_ANGLE && currentAngle < POSITIVE_ANGLE) {
            actionMoveCurrentSlice = POSITIVE_SLICE;
        } else if (currentAngle > NEGATIVE_ANGLE && currentAngle < MAX_ANGLE) {
            actionMoveCurrentSlice = NEGATIVE_SLICE;
        } else {
            actionMoveCurrentSlice = DEFAULT_SLICE;
        }

        // Check for handling counter
        if (actionMoveCurrentSlice != 0 &&
                ((mActionMoveLastSlice == NEGATIVE_SLICE && actionMoveCurrentSlice == POSITIVE_SLICE) ||
                        (actionMoveCurrentSlice == NEGATIVE_SLICE && mActionMoveLastSlice == POSITIVE_SLICE))) {
            if (mActionMoveLastSlice == NEGATIVE_SLICE) mActionMoveSliceCounter++;
            else mActionMoveSliceCounter--;

            // Limit counter for 1 and -1, we don`t need take the race
            if (mActionMoveSliceCounter > 1) mActionMoveSliceCounter = 1;
            else if (mActionMoveSliceCounter < -1) mActionMoveSliceCounter = -1;
        }
        mActionMoveLastSlice = actionMoveCurrentSlice;

        // Set total traveled angle
        float actionMoveTotalAngle = currentAngle + (MAX_ANGLE * mActionMoveSliceCounter);

        // Check whether traveled angle out of limit
        if (actionMoveTotalAngle < MIN_ANGLE || actionMoveTotalAngle > MAX_ANGLE) {
            actionMoveTotalAngle =
                    actionMoveTotalAngle > MAX_ANGLE ? MAX_ANGLE + 1.0F : -1.0F;
            currentAngle = actionMoveTotalAngle;
        }

        // Set model progress and invalidate
        float touchProgress = Math.round(MAX_PROGRESS / mSweepAngle * currentAngle);
        touchProgress = touchProgress < 0 ? 0 : touchProgress;
        touchProgress = touchProgress > MAX_PROGRESS ? MAX_PROGRESS : touchProgress;
        setProgress(touchProgress);
        if (mListener != null) {
            mListener.onProgress(touchProgress);
        }
        return touchProgress;
    }

    public void setSmoothProgress(int progress) {
        if (!mIsDragged) return;
        setProgress(progress);
        if (mListener != null) {
            mListener.onProgress(progress);
            mListener.onModified(progress);
        }
        if (isAnimated) {
            animateActionMoveProgress();
        } else {
            postInvalidate();
        }
    }

    // Animate progress
    private void animateActionMoveProgress() {
        if (!isAnimated || mProgressAnimator == null) return;
        if (mProgressAnimator.isRunning()) return;

        mProgressAnimator.setDuration(DEFAULT_ACTION_MOVE_ANIMATION_DURATION);
        mProgressAnimator.setInterpolator(null);
        if (mAnimatorListener != null) mProgressAnimator.removeListener(mAnimatorListener);
        mProgressAnimator.start();
    }

    public void setOnDragProgressListener(OnDragProgressListener l) {
        mListener = l;
    }

    public void setProgress(@FloatRange(from = MIN_PROGRESS, to = MAX_PROGRESS) final float progress) {
        mLastProgress = mProgress;
        mProgress = Math.max(MIN_PROGRESS, Math.min(progress, MAX_PROGRESS));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setInterpolator(final Interpolator interpolator) {
        mInterpolator = interpolator == null ? new AccelerateDecelerateInterpolator() : interpolator;
        mProgressAnimator.setInterpolator(mInterpolator);
    }

    public void setAnimated(boolean animated) {
        isAnimated = mIsFeaturesAvailable && animated;
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public void setIsDragged(final boolean isDragged) {
        mIsDragged = isDragged;
    }

    public void setColors(int[] colors) {
        if (colors != null && colors.length >= 2) {
            mModelColors = colors;
        }
    }

    public void setSweepGradient(SweepGradient sweepGradient) {
        if (sweepGradient != null) {
            mSweepGradient = sweepGradient;
        }
    }

    public RectF getModelBound() {
        return mModelBound;
    }

    public interface OnDragProgressListener {
        void onProgress(float progress);

        void onModified(float progress);
    }
}
