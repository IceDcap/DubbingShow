package com.example.missevan.mydubbing.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.missevan.mydubbing.R;
import com.example.missevan.mydubbing.utils.DimenUtil;

import java.util.Locale;

/**
 * Created by missevan on 2017/5/3.
 */

public class WaveformView extends View {
    public static final int MODE_DUBBING = 0x0;
    public static final int MODE_PREVIEW = 0x1;

    private final int CURRENT_TIME_TEXT_SP;
    private final int FPS = 33;
    private final int LINE_STROKE = 1;
    private final int TIME_COORDINATE_TEXT_SP;
    private final int TIME_LONG_LINE = 1000;
    private final int TIME_SHORT_LINE = 8;
    private final int TIME_TEXT_MARGINLEFT;


    protected int drawAreaHeight;

    private double[] heights;

    protected int indicatorFinalPos;

    private boolean indicatorReachMid;

    private int indicatorXtemp;

    protected boolean isFullScreen;

    private boolean isRecording;

    private boolean isSeeked;

    private boolean isTimeTextSolid;

    private long lastDrawtime;

    protected float mCurTime;

    private float mDuration;

    private int mDuration_Int;

    private GestureDetector mGestureDetector;

    private Paint mGrayLinePaint;

    private Paint mGridPaint;

    protected int[] mHeightsAtThisZoomLevel;

    private boolean mInitialized;

    private int mMaxPos;

    private int mOffset;

    private int mPaddingBottom;

    private int mPaddingTop;

    private int mParityCheck = -1;

    private int mPlaybackPos;

    private Paint mReviewShadowPaint;

    protected int mSampleRate;

    private int mSamplesPerFrame;

    private Paint mSelectedLinePaint;

    private int mSelectionEnd;

    private int mSelectionStart;

//    protected mSoundFile:Lcom/happyteam/dubbingshow/soundfile/CheapSoundFile;

    private Paint mTimecodePaint;

    private Paint mUnselectedBkgndLinePaint;

    private Paint mUnselectedLinePaint;

    private Paint mWavePaint;

    private int mZoomLevel;

    protected int measuredHeight;

    protected int measuredWidth;

    protected int mMode;

    private boolean needEveryFiveSecsLine;

    protected int numFrames;

    private Bitmap progressIndicator;

    private float reviewStartTime;

    private int seekGain;

    private int startSeekGain;


    private boolean mForbidTouch;

    private WaveformListener mListener;


    public WaveformView(Context context) {
        this(context, null);
    }

    public WaveformView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveformView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        CURRENT_TIME_TEXT_SP = DimenUtil.sp2px(context, 10);
        TIME_COORDINATE_TEXT_SP = DimenUtil.sp2px(context, 10);
        TIME_TEXT_MARGINLEFT = DimenUtil.dip2px(context, 18);


        init(context);
    }

    private void init(Context context) {
        mMode = MODE_DUBBING;
        reviewStartTime = -1;
        setFocusable(false);
        mGridPaint = new Paint();
        mGridPaint.setAntiAlias(true);
        mGridPaint.setStrokeWidth(LINE_STROKE);

        mWavePaint = new Paint();
        mWavePaint.setColor(ContextCompat.getColor(context, R.color.white));

        mSelectedLinePaint = new Paint();
        mSelectedLinePaint.setAntiAlias(true);
        mSelectedLinePaint.setStrokeWidth(LINE_STROKE);
        mSelectedLinePaint.setColor(ContextCompat.getColor(context, R.color.waveform_selected));

        mUnselectedLinePaint = new Paint();
        mUnselectedLinePaint.setAntiAlias(true);
        mUnselectedLinePaint.setStrokeWidth(LINE_STROKE);
        mUnselectedLinePaint.setColor(ContextCompat.getColor(context, R.color.waveform_unselected));

        mUnselectedBkgndLinePaint = new Paint();
        mUnselectedBkgndLinePaint.setAntiAlias(true);
        mUnselectedBkgndLinePaint.setStrokeWidth(LINE_STROKE);
        mUnselectedBkgndLinePaint.setColor(ContextCompat.getColor(context, R.color.waveform_unselected_bkgnd_overlay));

        mGrayLinePaint = new Paint();
        mGrayLinePaint.setAntiAlias(true);
        mGrayLinePaint.setStrokeWidth(LINE_STROKE);
        mGrayLinePaint.setColor(ContextCompat.getColor(context, R.color.waveform_gray));

        mReviewShadowPaint = new Paint();
        mReviewShadowPaint.setAntiAlias(true);
        mGrayLinePaint.setColor(ContextCompat.getColor(context, R.color.review_shadow));

        mTimecodePaint = new Paint();
        mTimecodePaint.setTextSize(CURRENT_TIME_TEXT_SP);
        mTimecodePaint.setAntiAlias(true);
        mTimecodePaint.setTextAlign(Paint.Align.CENTER);
        mTimecodePaint.setColor(ContextCompat.getColor(context, R.color.timecode));

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (null != mListener) {
                    mListener.waveformFling(velocityX);
                    return true;
                } else {
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            }
        });
        mPaddingBottom = DimenUtil.dip2px(context, 15f);
        mPaddingTop = DimenUtil.dip2px(context, 15f);
        setPadding(0, mPaddingTop, 0, mPaddingBottom);
        progressIndicator = BitmapFactory.decodeResource(getResources(), R.drawable.dubbing_bar);

    }


    private float calCurTimeByIndicatorX(int indicatorX) {
        int curX = progressIndicator.getWidth() / 2 + mOffset + indicatorX;
        float curTime = (float) (curX * pixelsToSeconds(LINE_STROKE));
        if (curTime < 0) {
            curTime = 0;
        } else if (curTime < mDuration) {
            curTime = mDuration;
        }
        return curTime;
    }

    private int calIndicatorX(int offset) {
        int indicatorX;
        if (!indicatorReachMid) {
            indicatorX = mMaxPos - offset - progressIndicator.getWidth() / 2 + seekGain;
            if (indicatorX > indicatorFinalPos) {
                indicatorReachMid = true;
            }
        } else {
            indicatorX = indicatorFinalPos;
        }

        return indicatorX;
    }

    private void computeDoublesForAllZoomLevels() {

    }

    private void computeIntsForThisZoomLevel() {
        int halfHeight = drawAreaHeight / 2 - 1;
        if (heights != null && mHeightsAtThisZoomLevel == null) {
            mHeightsAtThisZoomLevel = new int[numFrames];
            for (int i = 0; i < heights.length; i++) {
                mHeightsAtThisZoomLevel[i] = (int) heights[i] * halfHeight;
            }
        }
    }

    private void drawEmpty() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // no - op
            }
        });
        thread.start();
    }

    private String generateTime(long position) {
        String res;
        int totalSeconds = (int) (0.5 + position / 1000);
        int seconds = totalSeconds % 60;
        int minutes = totalSeconds / 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            res = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            res = String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
        return res;
    }

    protected float calCurTime(int offset) {
        int indicatorX = calIndicatorX(offset);
        return calCurTimeByIndicatorX(indicatorX);
    }

    protected void drawWaveformLine(Canvas canvas, int x, int y0, int y1, Paint paint) {
        canvas.drawLine(x, y0, x, y1, paint);
    }

    public void forceRedraw() {
        postInvalidate();
    }

    public float getCurTime() {
        return mCurTime;
    }

    public int getEnd() {
        return mSelectionEnd;
    }

    public int getEndPos(int offset) {
        return getMaxPos() - offset;
    }

    protected int getIndicatorCtr(int offset) {
        return calIndicatorX(offset) + progressIndicator.getWidth() / 2;
    }

    public int getOffset() {
        return mOffset;
    }

    public int millisecsToPixels(int msecs) {
        return (int) (msecs * mSampleRate / mSamplesPerFrame * 1000 + 0.5D);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long onDrawStartTime = System.currentTimeMillis();
        mHeightsAtThisZoomLevel = null;
        computeIntsForThisZoomLevel();
        int borderOffset = DimenUtil.dip2px(getContext(), 9);
        canvas.drawLine(0, mPaddingTop, measuredWidth, mPaddingTop, mGridPaint);
        canvas.drawLine(0, measuredHeight - mPaddingBottom, measuredWidth,
                measuredHeight - mPaddingBottom, mGridPaint);
        final double onePixelInSecs = pixelsToSeconds(LINE_STROKE);
        boolean onlyEveryFiveSecs = true;
        if (onePixelInSecs <= 0.02) {
            onlyEveryFiveSecs = false;
        }
        double fractionalSecs = onePixelInSecs + mOffset;
        int integerSecs = (int) fractionalSecs;
        int i = 0, diffX = 0;
        if (!indicatorReachMid) {
            int indicatorX = calIndicatorX(mOffset);

        }
        //todo
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (measuredWidth <= 0) {
            this.measuredWidth = getMeasuredWidth();
            this.measuredHeight = getMeasuredHeight();
            this.drawAreaHeight = measuredHeight - mPaddingTop - mPaddingBottom - DimenUtil.dip2px(getContext(), 6);
            this.indicatorFinalPos = (measuredWidth - progressIndicator.getWidth()) / 2;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public int pixelsToMillisecs(int pixels) {
        return (int) (pixels * mSamplesPerFrame * 1000 / mSampleRate + 0.5D);
    }

    public double pixelsToSeconds(int pixels) {
        return pixels * mSamplesPerFrame / mSampleRate;
    }

    public void reDraw() {
        long t = System.currentTimeMillis();
        if (t - lastDrawtime >= 33) {
            postInvalidate();
        }
    }

    protected void redirection() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (null != mListener) {
                    mListener.waveformFling(LINE_STROKE);
                }
            }
        }, 100);
    }

    protected void reset() {
        for (int i = 0; i < mHeightsAtThisZoomLevel.length; i++) {
            mHeightsAtThisZoomLevel[i] = 0;
        }
        this.mMaxPos = 0;
        this.seekGain = 0;
        this.startSeekGain = 0;
        this.mCurTime = 0.0F;
        this.mSelectionStart = 0;
        this.mSelectionEnd = 0;
        this.mForbidTouch = true;
        this.indicatorReachMid = false;
        this.isSeeked = false;
    }


    public int secondsToFrames(double seconds) {
        return (int) (seconds * mSampleRate / mSamplesPerFrame + 0.5D);
    }

    public int secondsToPixels(double seconds) {
        return (int) (mSampleRate * seconds / mSamplesPerFrame + 0.5D);
    }

    public void setForbidTouch(boolean forbidTouch) {
        mForbidTouch = forbidTouch;
    }

    public void setIsRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }

    public void setMaxPos(int maxPos) {
    }

    public int getDrawAreaHeight() {
        return drawAreaHeight;
    }

    public void setDrawAreaHeight(int drawAreaHeight) {
        this.drawAreaHeight = drawAreaHeight;
    }

    public double[] getHeights() {
        return heights;
    }

    public void setHeights(double[] heights) {
        this.heights = heights;
    }

    public int getIndicatorFinalPos() {
        return indicatorFinalPos;
    }

    public void setIndicatorFinalPos(int indicatorFinalPos) {
        this.indicatorFinalPos = indicatorFinalPos;
    }

    public boolean isIndicatorReachMid() {
        return indicatorReachMid;
    }

    public void setIndicatorReachMid(boolean indicatorReachMid) {
        this.indicatorReachMid = indicatorReachMid;
    }

    public int getIndicatorXtemp() {
        return indicatorXtemp;
    }

    public void setIndicatorXtemp(int indicatorXtemp) {
        this.indicatorXtemp = indicatorXtemp;
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    public boolean isSeeked() {
        return isSeeked;
    }

    public void setSeeked(boolean seeked) {
        isSeeked = seeked;
    }

    public boolean isTimeTextSolid() {
        return isTimeTextSolid;
    }

    public void setTimeTextSolid(boolean timeTextSolid) {
        isTimeTextSolid = timeTextSolid;
    }

    public long getLastDrawtime() {
        return lastDrawtime;
    }

    public void setLastDrawtime(long lastDrawtime) {
        this.lastDrawtime = lastDrawtime;
    }

    public void setCurTime(float curTime) {
        mCurTime = curTime;
    }

    public float getDuration() {
        return mDuration;
    }

    public void setDuration(float duration) {
        mDuration = duration;
    }

    public int getDuration_Int() {
        return mDuration_Int;
    }

    public void setDuration_Int(int duration_Int) {
        mDuration_Int = duration_Int;
    }

    public GestureDetector getGestureDetector() {
        return mGestureDetector;
    }

    public void setGestureDetector(GestureDetector gestureDetector) {
        mGestureDetector = gestureDetector;
    }

    public Paint getGrayLinePaint() {
        return mGrayLinePaint;
    }

    public void setGrayLinePaint(Paint grayLinePaint) {
        mGrayLinePaint = grayLinePaint;
    }

    public Paint getGridPaint() {
        return mGridPaint;
    }

    public void setGridPaint(Paint gridPaint) {
        mGridPaint = gridPaint;
    }

    public int[] getHeightsAtThisZoomLevel() {
        return mHeightsAtThisZoomLevel;
    }

    public void setHeightsAtThisZoomLevel(int[] heightsAtThisZoomLevel) {
        mHeightsAtThisZoomLevel = heightsAtThisZoomLevel;
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    public void setInitialized(boolean initialized) {
        mInitialized = initialized;
    }

    public int getMaxPos() {
        return mMaxPos;
    }

    public void setOffset(int offset) {
        mOffset = offset;
    }


    public void setPaddingBottom(int paddingBottom) {
        mPaddingBottom = paddingBottom;
    }

    public void setPaddingTop(int paddingTop) {
        mPaddingTop = paddingTop;
    }

    public int getParityCheck() {
        return mParityCheck;
    }

    public void setParityCheck(int parityCheck) {
        mParityCheck = parityCheck;
    }

    public int getPlaybackPos() {
        return mPlaybackPos;
    }

    public void setPlaybackPos(int playbackPos) {
        mPlaybackPos = playbackPos;
    }

    public Paint getReviewShadowPaint() {
        return mReviewShadowPaint;
    }

    public void setReviewShadowPaint(Paint reviewShadowPaint) {
        mReviewShadowPaint = reviewShadowPaint;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public void setSampleRate(int sampleRate) {
        mSampleRate = sampleRate;
    }

    public int getSamplesPerFrame() {
        return mSamplesPerFrame;
    }

    public void setSamplesPerFrame(int samplesPerFrame) {
        mSamplesPerFrame = samplesPerFrame;
    }

    public Paint getSelectedLinePaint() {
        return mSelectedLinePaint;
    }

    public void setSelectedLinePaint(Paint selectedLinePaint) {
        mSelectedLinePaint = selectedLinePaint;
    }

    public int getSelectionEnd() {
        return mSelectionEnd;
    }

    public void setSelectionEnd(int selectionEnd) {
        mSelectionEnd = selectionEnd;
    }

    public int getSelectionStart() {
        return mSelectionStart;
    }

    public void setSelectionStart(int selectionStart) {
        mSelectionStart = selectionStart;
    }

    public Paint getTimecodePaint() {
        return mTimecodePaint;
    }

    public void setTimecodePaint(Paint timecodePaint) {
        mTimecodePaint = timecodePaint;
    }

    public Paint getUnselectedBkgndLinePaint() {
        return mUnselectedBkgndLinePaint;
    }

    public void setUnselectedBkgndLinePaint(Paint unselectedBkgndLinePaint) {
        mUnselectedBkgndLinePaint = unselectedBkgndLinePaint;
    }

    public Paint getUnselectedLinePaint() {
        return mUnselectedLinePaint;
    }

    public void setUnselectedLinePaint(Paint unselectedLinePaint) {
        mUnselectedLinePaint = unselectedLinePaint;
    }

    public Paint getWavePaint() {
        return mWavePaint;
    }

    public void setWavePaint(Paint wavePaint) {
        mWavePaint = wavePaint;
    }

    public int getZoomLevel() {
        return mZoomLevel;
    }

    public void setZoomLevel(int zoomLevel) {
        mZoomLevel = zoomLevel;
    }


    public void setMeasuredHeight(int measuredHeight) {
        this.measuredHeight = measuredHeight;
    }

    public void setMeasuredWidth(int measuredWidth) {
        this.measuredWidth = measuredWidth;
    }

    public int getMode() {
        return mMode;
    }

    public void setMode(int mode) {
        this.mMode = mode;
    }

    public boolean isNeedEveryFiveSecsLine() {
        return needEveryFiveSecsLine;
    }

    public void setNeedEveryFiveSecsLine(boolean needEveryFiveSecsLine) {
        this.needEveryFiveSecsLine = needEveryFiveSecsLine;
    }

    public int getNumFrames() {
        return numFrames;
    }

    public void setNumFrames(int numFrames) {
        this.numFrames = numFrames;
    }

    public Bitmap getProgressIndicator() {
        return progressIndicator;
    }

    public void setProgressIndicator(Bitmap progressIndicator) {
        this.progressIndicator = progressIndicator;
    }

    public float getReviewStartTime() {
        return reviewStartTime;
    }

    public void setReviewStartTime(float reviewStartTime) {
        this.reviewStartTime = reviewStartTime;
    }

    public int getSeekGain() {
        return seekGain;
    }

    public void setSeekGain(int seekGain) {
        this.seekGain = seekGain;
    }

    public int getStartSeekGain() {
        return startSeekGain;
    }

    public void setStartSeekGain(int startSeekGain) {
        this.startSeekGain = startSeekGain;
    }

    public boolean isForbidTouch() {
        return mForbidTouch;
    }

    public WaveformListener getListener() {
        return mListener;
    }

    public void setListener(WaveformListener listener) {
        mListener = listener;
    }

    public interface WaveformListener {
        void waveformDraw();

        void waveformFling(float velocityX);

        void waveformTouchEnd(float end);

        void waveformTouchMove(float move);

        void waveformTouchStart(float start);
    }


}
