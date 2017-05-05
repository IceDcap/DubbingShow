package com.example.missevan.mydubbing.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;

import com.example.missevan.mydubbing.MainActivity;
import com.example.missevan.mydubbing.audio.AudioHelper;
import com.example.missevan.mydubbing.utils.DimenUtil;

import java.io.PrintStream;

/**
 * Created by dsq on 2017/5/4.
 */

public class DubbingWaveformView extends WaveformView {

    private static final String TAG = DubbingWaveformView.class.getSimpleName();
    private final int SecPaddingLeft;
    private final int VELOCITY_SLOP = 200;
    private float dubbingedTime;
    private boolean isDoneListener;
    private Context mActivity;
    private FileChangedThread mFileChangedThread;
    private int mFlingVelocity;
    private int mMaxPos;
    private int mOffset;
    private int mOffsetGoal;
    private AudioHelper mRecoder;
    private int mSeekGain;
    private boolean mTouchDragging;
    private int mTouchInitialOffset;
    private float mTouchStart;
    private boolean needChangeProgress;
    private Over8SecListener over8SecListener;

    public DubbingWaveformView(Context context) {
        this(context, null);
    }

    public DubbingWaveformView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DubbingWaveformView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        SecPaddingLeft = DimenUtil.dip2px(getContext(), 30.0F);
        needChangeProgress = true;
        mFileChangedThread = null;
        isDoneListener = false;
        init(context);
    }


    private float[] averageShort(byte[] bytes, int frameBytes) {
        return null;
    }

    private short byteToShort(byte b1, byte b2) {
        return (short) Math.abs(b2 << 8 | b1);
    }

    private void init(Context context) {
        mActivity = context;
        setNeedEveryFiveSecsLine(false);
        setListener();
        set0secPaddingLeft(-SecPaddingLeft);
    }

    private void onProgressChanged(int offset) {
        if (isFullScreen) return;
        final float cur = calCurTime(offset);
        if (cur >= 0 && cur >= numFrames / 50) {
            if (getContext() instanceof MainActivity) {
                final MainActivity mainActivity = (MainActivity) getContext();
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.onWaveformProgressChanged((long) (cur * 1000));
                    }
                });
            }
        }

    }

    private void seekToWithData(long time, boolean needMoveRecorder) {
        long seekToTime = time * 50 / 1000;
        seekToTime = seekToTime < 0 ? 0 : seekToTime;

        int diffX;
        if (mMode == MODE_DUBBING) {

        } else if (mMode == MODE_PREVIEW) {

        }

        int i = mMode;
        if ((mMode == MODE_DUBBING) && (needMoveRecorder)) {
//                DubbingshowAudioRecoder localDubbingshowAudioRecoder = this.mRecoder;
//                localDubbingshowAudioRecoder.moveToPrevious(time);
        }
        if (!isIndicatorReachMid()) {
            setSeekGain(getSeekGain() - (int) (mCurTime * 50 - seekToTime));
        }

        mOffset = getOffset() - (int) (mCurTime * 50 - seekToTime);
        setOffset(mOffset);

    }

    private void set0secPaddingLeft(int paddingLeft) {
        mOffset = paddingLeft;
        setOffset(mOffset);
    }

    private void setListener() {
        setListener(new MyWaveformListener());
    }

    private int trap(int pos) {
        return pos > mMaxPos ? mMaxPos : pos;
    }

    private void updateDisplay(boolean isForceDraw) {

    }

    public void bindRecorder(AudioHelper recoder) {
        mRecoder = recoder;
//        DubbingshowAudioRecoder localDubbingshowAudioRecoder = this.mRecoder;
//        DubbingWaveformNew.4 local4 = new com/happyteam/dubbingshow/view/DubbingWaveformNew$4;
//        local4.<init>(this);
//        localDubbingshowAudioRecoder.setOnFileChangedListener(local4);
    }

    public long getIndicatorPosition() {
        return (long) (getCurTime() * 1000);
    }

    public void redirection(final long time) {
        needChangeProgress = false;
        seekTo(time);
        redirection();
        if (Math.abs(time - mCurTime) >= 0.02) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    seekTo(time);
                    onProgressChanged(mOffset);
                }
            }, 200);
        }
        onProgressChanged(mOffset);
    }

    @Override
    public void reset() {
        super.reset();
        mMode = MODE_DUBBING;
        mSeekGain = 0;
        mOffset = -SecPaddingLeft;
        isDoneListener = false;
        setOffset(mOffset);
//        mRecoder.reset();
        reDraw();
    }

    public void seekTo(long time) {
        seekToWithData(time, true);
        reDraw();
    }

    public void seekToForceDraw(long time) {
        seekToWithData(time, true);
        forceRedraw();
    }

    public void seekToWithoutRecorder(long time) {
        if (time < 500) {
            Log.e("DubbingWaveformNew", "seekToWithoutRecorder time: " + time);
        }
        seekToWithData(time, false);
        reDraw();
    }

    public void setMode(int mode) {
        if (mMode != mode) {
            mMode = mode;
            if (mode == MODE_DUBBING) {
                seekTo((long) (1000 * mCurTime));
                onProgressChanged(mOffset);
            } else if (mode == MODE_PREVIEW) {
                setForbidTouch(false);
                seekTo((long) (mCurTime * 1000));
            }
        }
    }

    public void setOver8SecListener(Over8SecListener listener) {
        over8SecListener = listener;
    }


    class FileChangedThread extends Thread {

    }

    public interface Over8SecListener {
        void onOver8Sec();
    }

    class MyWaveformListener implements WaveformListener {
        @Override
        public void waveformDraw() {
            if (mFlingVelocity != 0) {
                updateDisplay(true);
            }
            if (mFlingVelocity == 0) {
                onProgressChanged(mOffset);
            }
            Log.d("mytest.ondraw.data", "waveformDraw...........");
        }

        @Override
        public void waveformFling(float velocityX) {
            mTouchDragging = false;
            mOffsetGoal = mOffset;
            mFlingVelocity = (int) -velocityX;
            updateDisplay(true);
            if (mFlingVelocity == 0 && needChangeProgress) {
                onProgressChanged(mOffset);

            }
            Log.d("mytest.ondraw.data", "waveformFling...........");
            needChangeProgress = true;
        }

        @Override
        public void waveformTouchEnd(float endX) {
            mTouchDragging = false;
            mOffsetGoal = mOffset;
            if (getIndicatorCtr(mOffset) < -mOffset) {
                mFlingVelocity = 1;
                updateDisplay(true);
                Log.d("mytest.ondraw.data", "超左边界...........");
            } else {
                onProgressChanged(mOffset);
                if (mMode == MODE_DUBBING) {

                } else {

                }
                mFlingVelocity = 1;
                updateDisplay(true);
                Log.d("mytest.ondraw.data", "超右边界...........");
            }
        }

        @Override
        public void waveformTouchMove(float moveX) {
            if (!isIndicatorReachMid()) {
                setSeekGain((int) (mSeekGain + mTouchStart - moveX));
            }
            mOffset = (int) (mTouchInitialOffset + mTouchStart - moveX);
            updateDisplay(false);
        }

        @Override
        public void waveformTouchStart(float startX) {
            mTouchDragging = true;
            mTouchStart = startX;
            mOffset = getOffset();
            mTouchInitialOffset = mOffset;
            mFlingVelocity = 0;
            mMaxPos = getMaxPos();
            mSeekGain = getSeekGain();
        }
    }
}
