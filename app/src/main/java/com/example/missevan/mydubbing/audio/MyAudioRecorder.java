package com.example.missevan.mydubbing.audio;

import android.content.Context;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.example.missevan.mydubbing.utils.Config;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by dsq on 2017/5/2.
 */

public class MyAudioRecorder {
    private static final String LOG_TAG = MyAudioRecorder.class.getSimpleName();
    private final static int STATUS_PAUSE = 2;
    private final static int STATUS_PREPARED = 1;
    private final static int STATUS_RECORDING = 0;

    private int STATUS = STATUS_PREPARED;

    private AudioRecord mAudioRecord;
    private AudioTrack mAudioTrack;

    private Thread mAudioTrackThread;
    private boolean mAudiotrackflag;
    private int bufferSize;
    private byte[] mBytes;
    private long mCurFileLength;
    private long endPos;
    private long mFilePointer;
    private int mFrequency = 44100 / 2;
    int i;
    private boolean isCalledonOverEightSeconds;
    private boolean isRewrite;
    int j;
    private Context mContext;
    private boolean mThreadFlag;
    private int mMinBufferSize;
    private String mNewPath;
    private long offset;
    private long oldtime;


    public RandomAccessFile mRandomAccessFile;
    public RandomAccessFile mNewRandomAccessFile;
    private int RECORDER_BPP = 1024;
    private String RECORD_TEMP_PATH;

    private String mOutPath;
    private String mPath;
    private int mRecBufSize;
    private String mSourceid;
    private long mStartPos;

    private Thread mThread;
    private int mTime;
    private long mTotallength;



    private long mDuration;

    private OnFileChangedListener mOnFileChangedListener;
    private OnEventListener mOnEventListener;


    private Handler mErrHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mOnEventListener.onError(msg.obj.toString());
        }
    };

    public MyAudioRecorder(Context context, OnEventListener onEventListener, String sourceId, long duration) {
        mContext = context;
        mOnEventListener = onEventListener;
        mSourceid = sourceId;
        mDuration = duration;
        StringBuilder sb = new StringBuilder();
        //fixme use 'util' fetch temp path to cache record file
        String path = context.getExternalFilesDir(null).getAbsolutePath();
        sb.append(path);
        sb.append("/audio");
        RECORD_TEMP_PATH = sb.toString();

        initRecorder(true);
        initAudioTrack();
    }

    private void WriteWaveFileHeader1(long totalAudioLen,
                                      long totalDataLen, long longSampleRate,
                                      int channels, long byteRate) {

    }

    private void WriteWaveFileHeader1(FileOutputStream out, long totalAudioLen,
                                      long totalDataLen, long longSampleRate,
                                      int channels, long byteRate) {

    }

    private void closeStream() {
        if (null != mRandomAccessFile) {
            try {
                mRandomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyWaveFile(String inFilename, String outFilename, boolean isStereo) {
        int in = 0;
        int out = 0;
        long totalAudioLen = 0;
        long totalDataLen = 0;
        long longSampleRate = 0;
    }


    private void createWaveFile(RandomAccessFile randomAccessFile, boolean isStereo, long totalLength) {

    }

    private void getMinBuffSize() {

    }

    private void initAudioTrack() {

    }

    private void startThread() {

    }

    public String createFile() {
        return "";
    }

    public void createSteoroFile(boolean isStory) {
    }

    public void fillFileIfNotComplete() {
    }

    public String getOutPath() {
        return "";
    }

    public void initAudioFile(boolean deleteOld) {

    }

    public void initRecorder(boolean createNewFile) {
        initAudioFile(createNewFile);
    }

    public boolean isRecording() {
        return false;
    }

    public void moveToPrevious(long time) {
    }

    public void onPause() {

    }

    public void onResume() {

    }

    public void pauseRecord() {

    }

    public void pauseReview() {
    }

    public void processToWavFile() {

    }

    public void reset() {
    }

    public void resumeRecord() {
    }

    public void setCalledonOverEightSeconds(boolean calledonOverEightSeconds) {
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public void setFileToDefault() {

    }

    public void setOnFileChangedListener(OnFileChangedListener onFileChangedListener) {
        mOnFileChangedListener = onFileChangedListener;
    }

    public void startRecording() {
    }

    public void startReview(boolean startThread) {
    }

    public void stopRecord() {
    }

    public interface OnEventListener{
        void onError(String err);

        void onOverEightSeconds();

        void onRecordComplete();

        void onVolumnChanged(double volumn);
    }

    public interface OnFileChangedListener{
        void onBufferRecevied(long l1, long l2, byte[] bytes, boolean b);
    }
}
