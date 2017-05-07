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
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.missevan.mydubbing.R;
import com.example.missevan.mydubbing.soundfile.CheapSoundFile;
import com.example.missevan.mydubbing.utils.DimenUtil;

import java.util.Locale;

/**
 * Created by dsq on 2017/5/3.
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


    protected int drawAreaHeight;// audio caputer area

    private double[] heights;

    // indicator relevant
    protected int indicatorFinalPos;
    private boolean indicatorReachMid;
    private int indicatorXtemp;
    private Bitmap progressIndicator;

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
    private Paint mSelectedLinePaint;
    private Paint mReviewShadowPaint;
    private Paint mTimecodePaint;
    private Paint mUnselectedBkgndLinePaint;
    private Paint mUnselectedLinePaint;
    private Paint mWavePaint;

    protected int[] mHeightsAtThisZoomLevel;

    private boolean mInitialized;

    private int mMaxPos;

    private int mOffset;

    private int mPaddingBottom;

    private int mPaddingTop;

    private int mParityCheck = -1;

    private int mPlaybackPos;

    protected int mSampleRate = 44100;

    private int mSamplesPerFrame;

    private int mSelectionEnd;

    private int mSelectionStart;

    protected CheapSoundFile mSoundFile;

    private int mZoomLevel;

    protected int measuredHeight;

    protected int measuredWidth;

    protected int mMode;

    private boolean needEveryFiveSecsLine;

    protected int numFrames;

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
        mGridPaint.setColor(ContextCompat.getColor(context, R.color.wave_line));


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
        mPaddingTop = DimenUtil.dip2px(context, 0.5f);
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
        numFrames = mSoundFile.getNumFrames();
        int[] frameGains = mSoundFile.getFrameGains();
        double[] smoothedGains = new double[numFrames];
        if (numFrames == 1) {
            smoothedGains[0] = frameGains[0];
        }

        double maxGain = smoothedGains[0];
        for (int i = 0; i < maxGain; i++) {
            if (numFrames == 2) {
                smoothedGains[0] = frameGains[0];
                smoothedGains[1] = frameGains[1];
            } else if (numFrames > 2) {
                smoothedGains[0] = frameGains[0] / 2 + frameGains[1] / 2;
            }
        }
        //fixme
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
//        if (mSoundFile == null) return;

        if (mHeightsAtThisZoomLevel == null) {
            computeIntsForThisZoomLevel();
        }

        int start = mOffset;

        int width = mMaxPos - start;

        int ctr = measuredHeight / 2;

        if (width > measuredWidth) {
            width = measuredWidth;
        }


        int borderOffset = DimenUtil.dip2px(getContext(), 9);

        // draw two line
        canvas.drawLine(0, mPaddingTop, measuredWidth, mPaddingTop, mGridPaint);
        canvas.drawLine(0, measuredHeight - mPaddingBottom, measuredWidth,
                measuredHeight - mPaddingBottom, mGridPaint);


        final double onePixelInSecs = pixelsToSeconds(LINE_STROKE);
        boolean onlyEveryFiveSecs = false;
        int indicatorX = 0;
        if (onePixelInSecs < 0.02) {
            onlyEveryFiveSecs = true;
            double fractionalSecs = mOffset * onePixelInSecs;
            int integerSecs = (int) fractionalSecs;

            if (indicatorReachMid) {
                indicatorX = indicatorFinalPos;
            }
        }

        //todo draw time form degree


//        Object localObject1 = "draw()";
//        Logger.d("dubbingshow.dubbingWaveform", (String)localObject1);
//        long l1 = System.currentTimeMillis();
//        Object localObject2 = this.mSoundFile;
//        if ((localObject2 == null) || (canvas == null)) {
//            return;
//        }
//        localObject2 = this.mHeightsAtThisZoomLevel;
//        if (localObject2 == null) {
//            computeIntsForThisZoomLevel();
//        }
//        int i = this.mOffset;
//        int j = i;
//        int k = this.mMaxPos - i;
//        int m = this.measuredHeight / 2;
//        int n = this.measuredWidth;
//        i = k;
//        if (k > n) {
//            i = this.measuredWidth;
//            k = i;
//        }
//        int i9 = DimenUtil.dip2px(getContext(), 9.0F);
//        float f1 = this.mPaddingTop;
//        float f2 = this.measuredWidth;
//        float f3 = this.mPaddingTop;
//        Paint localPaint1 = this.mGridPaint;
//        localObject2 = canvas;
//        canvas.drawLine(0.0F, f1, f2, f3, localPaint1);
//        int i10 = 0;
//        float f4 = 0.0F;
//        localObject1 = null;
//        n = this.measuredHeight;
//        int i11 = this.mPaddingBottom;
//        f1 = n - i11;
//        f2 = this.measuredWidth;
//        n = this.measuredHeight;
//        int i12 = this.mPaddingBottom;
//        f3 = n - i12;
//        localPaint1 = this.mGridPaint;
//        canvas.drawLine(0.0F, f1, f2, f3, localPaint1);
//        float f5 = Float.MIN_VALUE;
//        double d1 = pixelsToSeconds(1);
//        long l2 = 4581421828931458171L;
//        double d2 = 0.02D;
//        boolean bool1 = d1 < d2;
//        int i13;
//        int i14;
//        int i16;
//        int i17;
//        if (bool1) {
//            i13 = 1;
//            d2 = this.mOffset;
//            d3 = d2 * d1;
//            i = (int) d3;
//            i14 = i;
//            i15 = 0;
//            f1 = 0.0F;
//            i16 = 0;
//            bool1 = this.indicatorReachMid;
//            if (!bool1) {
//                break label546;
//            }
//            i = this.indicatorFinalPos;
//            i17 = i;
//        }
//        int i18;
//        int i19;
//        for (; ; ) {
//        localObject2 = this.progressIndicator;
        int indicatorCtr = indicatorX + progressIndicator.getWidth() / 2;
        int shadowX = -1;
//        i10 = 0;
//        f4 = 0.0F;
//        localObject1 = null;
        if (reviewStartTime >= 0) {
//            f5 = this.reviewStartTime / 20.0F;
//            f4 = this.mOffset;
//            f5 -= f4;
//            int i2 = (int) f5;
//            i10 = 0;
//            f4 = 0.0F;
//            localObject1 = null;
            shadowX = Math.max((int) (reviewStartTime / 20 - mOffset), 0);
        }
//        boolean bool3 = this.needEveryFiveSecsLine;
        if (!needEveryFiveSecsLine) {
//            int i15 = 0;
//            f1 = 0.0F;
//            int i5 = j + 0;
//            if (i5 < 0) {
//                i = j;
//                i15 = -j;
//            }
//            i5 = this.mParityCheck;
//            if (i5 < 0) {
//                i5 = (j + i15) % 2;
//                this.mParityCheck = i5;
//            }
//            break label768;
        }
//        for (; ; ) {
//            i = k;
//            if (i15 >= k) {
//                break;
//            }
//            int i20 = i15 + 1;
//            d3 += d1;
//            i = (int) d3;
//            if (i != i14) {
//                i14 = i;
//                if (i13 != 0) {
//                    i3 = i % 5;
//                    if (i3 != 0) {
//                    }
//                } else {
//                    i = i20;
//                    f4 = i20;
//                    i15 = 0;
//                    f1 = 0.0F;
//                    f2 = i20;
//                    i3 = this.measuredHeight;
//                    f3 = i3;
//                    canvas.drawLine(f4, 0.0F, f2, f3, mGridPaint);
//                }
//            }
//            i15 = i20;
//        }
//        i13 = 0;
//        break;
//        label546:
//        int i3 = this.mOffset;
//        indicatorX = calIndicatorX(i3);
//        boolean bool4 = this.isRecording;
//        if (bool4) {
//            int i4 = this.mSelectionEnd;
//            i10 = this.mOffset;
//            i4 -= i10;
//            i10 = this.mOffset;
//            i10 = getIndicatorCtr(i10);
//            i16 = i4 - i10;
//            if (i16 > 0) {
//                indicatorX += i16;
//                i4 = this.startSeekGain + i16;
//                this.seekGain = i4;
//                i4 = this.indicatorFinalPos;
//                i = indicatorX;
//                if (indicatorX >= i4) {
//                    i4 = 1;
//                    f5 = Float.MIN_VALUE;
//                    this.indicatorReachMid = i4;
//                    i = this.indicatorFinalPos;
//                    indicatorX = i;
//                }
//            }
//        }
//        boolean bool5 = this.isSeeked;
//        if (bool5) {
//            f5 = 0.0F;
//            localObject2 = null;
//            this.isSeeked = false;
//            i5 = this.indicatorXtemp;
//            i = indicatorX;
//            if (indicatorX < i5) {
//                i = this.indicatorXtemp;
//                indicatorX = i;
//                i5 = this.seekGain;
//                i10 = this.indicatorXtemp - i;
//                i5 += i10;
//                this.seekGain = i5;
//            }
//        }
//        i = indicatorX;
//        this.indicatorXtemp = indicatorX;
////        }
//        label768:
//        int i15 = 0;
//        f1 = 0.0F;
//        int i5 = j + 0;
//        if (i5 < 0) {
//            i = j;
//            i15 = -j;
//        }
//        i5 = this.mParityCheck;
//        if (i5 < 0) {
//            i5 = (j + i15) % 2;
//            this.mParityCheck = i5;
//        }
//        for (; ; ) {
//            i5 = 1028443341;
//            f4 = this.drawAreaHeight;
//            f5 = 0.05F * f4;
//            i10 = 1073741824;
//            f4 = 2.0F;
//            f5 /= f4;
//            i = (int) f5;
//            int i21 = i;
//            for (; ; ) {
//                i = k;
//                if (i15 >= k) {
//                    break;
//                }
//                localPaint1 = this.mWavePaint;
//                localObject2 = this.mHeightsAtThisZoomLevel;
//                i10 = j + i15;
//                i5 = localObject2[i10];
//                i = i21;
//                if (i5 < i21) {
//                    localObject2 = this.mHeightsAtThisZoomLevel;
//                    i10 = j + i15;
//                    localObject2[i10] = i21;
//                }
//                i5 = m + -1;
//                localObject1 = this.mHeightsAtThisZoomLevel;
//                i11 = j + i15;
//                i10 = localObject1[i11];
//                i11 = i5 - i10;
//                i5 = m + 1;
//                localObject1 = this.mHeightsAtThisZoomLevel;
//                i12 = j + i15;
//                i10 = localObject1[i12];
//                i12 = i5 + i10;
//                localObject2 = this;
//                localObject1 = canvas;
//                drawWaveformLine(canvas, i15, i11, i12, localPaint1);
//                i15 += 2;
//            }
//            i5 = this.mParityCheck;
//            i10 = (j + i15) % 2;
//            if (i5 != i10) {
//                i15 += 1;
//            }
//        }
//        double d4 = 1.0D;
//        d2 = d4 / d1;
//        long l3 = 4632233691727265792L;
//        double d5 = 50.0D;
//        boolean bool6 = d2 < d5;
//        if (bool6) {
//            d4 = 5.0D;
//        }
//        d2 = d4 / d1;
//        l3 = 4632233691727265792L;
//        d5 = 50.0D;
//        bool6 = d2 < d5;
//        if (bool6) {
//            d4 = 15.0D;
//        }
//        double d3 = this.mOffset * d1;
//        d2 = d3 / d4;
//        i = (int) d2;
//        int i22 = i;
//        int i6 = this.mOffset;
//        if ((i6 < 0) && (i == 0)) {
//            i22 = -1;
//        }
//        i15 = 0;
//        f1 = 0.0F;
//        int i23;
//        float f6;
//        float f7;
//        float f8;
//        float f9;
//        Paint localPaint2;
//        label1429:
//        do {
//            do {
//                boolean bool7;
//                do {
//                    i6 = this.measuredWidth;
//                    if (i15 >= i6) {
//                        break;
//                    }
//                    i15 += 1;
//                    d3 += d1;
//                    l2 = 0L;
//                    d2 = 0.0D;
//                    bool7 = d3 < d2;
//                } while (bool7);
//                i14 = (int) d3;
//                d2 = d3 / d4;
//                i = (int) d2;
//            } while ((i == i22) || (i14 < 0));
//            i22 = i;
//            i7 = this.measuredHeight;
//            i10 = this.mPaddingBottom;
//            i23 = i7 - i10;
//            i7 = i14 % 5;
//            if (i7 != 0) {
//                break label2000;
//            }
//            i = i14;
//            l2 = i14 * 1000;
//            String str1 = generateTime(l2);
//            f6 = i15;
//            i7 = this.TIME_LONG_LINE;
//            f7 = i23 - i7;
//            f8 = i15;
//            i = i23;
//            f9 = i23;
//            localPaint2 = this.mGridPaint;
//            localObject3 = canvas;
//            canvas.drawLine(f6, f7, f8, f9, localPaint2);
//            i7 = this.TIME_TEXT_MARGINLEFT + i15;
//            f5 = i7;
//            localObject1 = getContext();
//            i11 = 1090519040;
//            f2 = 8.0F;
//            i10 = DimenUtil.dip2px((Context) localObject1, f2);
//            i10 = i23 - i10;
//            f4 = i10;
//            localObject4 = this.mTimecodePaint;
//            canvas.drawText(str1, f5, f4, (Paint) localObject4);
//            i7 = this.mDuration_Int;
//            i = i22;
//        } while (i22 < i7);
//        if (shadowX >= 0) {
//            i = shadowX;
//            if (shadowX < indicatorCtr) {
//                f6 = shadowX;
//                f7 = this.mPaddingTop;
//                i = indicatorCtr;
//                f8 = indicatorCtr;
//                i7 = this.measuredHeight;
//                i10 = this.mPaddingBottom;
//                i7 -= i10;
//                f9 = i7;
//                localPaint2 = this.mReviewShadowPaint;
//                localObject3 = canvas;
//                canvas.drawRect(f6, f7, f8, f9, localPaint2);
//            }
//        }
//        f5 = calCurTimeByIndicatorX(indicatorX);
//        this.mCurTime = f5;
//        localObject2 = new java / lang / StringBuilder;
//        ((StringBuilder) localObject2).<init> ();
//        l3 = (this.mCurTime * 1000.0F);
//        localObject1 = generateTime(l3);
//        localObject2 = ((StringBuilder) localObject2).append((String) localObject1).append("/");
//        f4 = this.mDuration;
//        i11 = 1148846080;
//        l3 = (f4 * 1000.0F);
//        localObject1 = generateTime(l3);
//        String str2 = (String) localObject1;
//        int i7 = this.progressIndicator.getWidth() / 2 + indicatorX;
//        f5 = i7;
//        i10 = this.CURRENT_TIME_TEXT_SP;
//        f4 = i10;
//        Object localObject4 = this.mTimecodePaint;
//        canvas.drawText(str2, f5, f4, (Paint) localObject4);
//        localObject2 = this.progressIndicator;
//        i = indicatorX;
//        f4 = indicatorX;
//        f2 = i9;
//        Object localObject5 = this.mGridPaint;
//        canvas.drawBitmap((Bitmap) localObject2, f4, f2, (Paint) localObject5);
//        localObject2 = this.mListener;
//        if (localObject2 != null) {
//            bool8 = this.forbidTouch;
//            if (!bool8) {
//                localObject2 = this.mListener;
//                ((WaveformView.WaveformListener) localObject2).waveformDraw();
//            }
//        }
//        l2 = System.currentTimeMillis();
//        long l4 = l2 - l1;
//        localObject1 = new java / lang / StringBuilder;
//        ((StringBuilder) localObject1).<init> ();
//        localObject1 = "Waveform ondraw used time=" + l4;
//        Log.d("mytest.ondraw", (String) localObject1);
//        localObject1 = "mytest.ondraw.data";
//        localObject4 = "indicatorX=%d,mCurTime=%.2f,offset=%d,seekGain=%d,mMaxPos=%d,diffX=%d,reachMid=%d,ss=%d,se=%d";
//        localObject5 = new Object[9];
//        localObject2 = null;
//        Object localObject3 = Integer.valueOf(indicatorX);
//        localObject5[0] = localObject3;
//        localObject3 = Float.valueOf(this.mCurTime);
//        localObject5[1] = localObject3;
//        localObject3 = Integer.valueOf(this.mOffset);
//        localObject5[2] = localObject3;
//        localObject3 = Integer.valueOf(this.seekGain);
//        localObject5[3] = localObject3;
//        localObject3 = Integer.valueOf(this.mMaxPos);
//        localObject5[4] = localObject3;
//        f5 = 7.0E-45F;
//        localObject3 = Integer.valueOf(i16);
//        localObject5[5] = localObject3;
//        int i24 = 6;
//        boolean bool8 = this.indicatorReachMid;
//        int i8;
//        if (bool8) {
//            i8 = 11;
//            f5 = 1.5E-44F;
//        }
//        for (; ; ) {
//            localObject2 = Integer.valueOf(i8);
//            localObject5[i24] = localObject2;
//            localObject3 = Integer.valueOf(this.mSelectionStart);
//            localObject5[7] = localObject3;
//            i8 = 8;
//            f5 = 1.1E-44F;
//            i24 = this.mSelectionEnd;
//            localObject3 = Integer.valueOf(i24);
//            localObject5[i8] = localObject3;
//            localObject2 = String.format((String) localObject4, (Object[]) localObject5);
//            Log.d((String) localObject1, (String) localObject2);
//            break;
//            label2000:
//            f6 = i15;
//            i8 = this.TIME_SHORT_LINE;
//            i8 = i23 - i8;
//            f7 = i8;
//            f8 = i15;
//            i = i23;
//            f9 = i23;
//            localPaint2 = this.mGridPaint;
//            localObject3 = canvas;
//            canvas.drawLine(f6, f7, f8, f9, localPaint2);
//            break label1429;
//            i8 = 0;
//            f5 = 0.0F;
//            localObject2 = null;
//        }


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
        if (!mForbidTouch) {
            return mGestureDetector.onTouchEvent(event);
        }
        switch (event.getAction()) {
            default:
                return super.onTouchEvent(event);
            case MotionEvent.ACTION_DOWN:
                mListener.waveformTouchStart(event.getX());
                break;
            case MotionEvent.ACTION_MOVE:
                mListener.waveformTouchMove(event.getX());
                break;
            case MotionEvent.ACTION_UP:
                mListener.waveformTouchEnd(event.getX());
        }
        return true;
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

        void waveformTouchEnd(float endX);

        void waveformTouchMove(float moveX);

        void waveformTouchStart(float startX);
    }


}
