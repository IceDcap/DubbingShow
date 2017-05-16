package com.icedcap.dubbing;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.drawable.LevelListDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.icedcap.dubbing.audio.AudioHelper;
import com.icedcap.dubbing.audio.AudioRecordHelper;
import com.icedcap.dubbing.entity.SRTEntity;
import com.icedcap.dubbing.utils.Config;
import com.icedcap.dubbing.utils.MediaUtil;
import com.icedcap.dubbing.utils.SRTUtil;
import com.icedcap.dubbing.view.DubbingVideoView;
import com.icedcap.dubbing.view.DubbingSubtitleView;
import com.icedcap.dubbing.view.WaveformView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.icedcap.dubbing.view.DubbingVideoView.MODE_DUBBING;
import static com.icedcap.dubbing.view.DubbingVideoView.MODE_IDEL;

public class DubbingActivity extends AppCompatActivity implements DubbingVideoView.OnEventListener,
        AudioHelper.OnAudioRecordPlaybackListener, WaveformView.WaveformListener
        , AudioRecordHelper.OnAudioRecordListener {
    private static final int MATERIAL = 1;
    private static final int[] SUBTITLE = new int[]{
            R.raw.subtitle,
            R.raw.subtitle1,
    };
    private static final String[] VIDEO = new String[]{
            "material/4803081086444687938.mp4",
            "material2/4911222198272423589.mp4"
    };
    private static final String[][] AUDIO = {
            {
                    "material/5314291602012189567.mp3"
            },
            {
                    "material2/4961513952477274315.mp3",
                    "material2/5228034864680729630.mp3",
            }
    };
    private File mVideoFile;
    private static final String BASE = "/sdcard/MyDubbing/audio_temp/";
    //    private static String MP3_PATH;
    private static String WAV_PATH;

//    private AudioHelper mAudioHelper;


    private void setScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);
        Config.screen_width = displayMetrics.widthPixels;
    }

    // data 4 ui
    private String primaryRole = "";
    private String secondRole = "";
    private long mDuration;
    private int mWaitingNumber = 3;

    private int reviewFlagTime = 0;

    boolean isReviewing = false;

    // data set
    List<SRTEntity> mSrtEntityList;
    private boolean isDubbing;
//    private long mRecordTime;

    // audio record relevant
    private AudioRecordHelper mAudioRecordHelper;
    private long mRecordedDuration;
    private long mWroteAccessFilePointer;
    private boolean isRecording;
    private long mHadRecordTime;
    private MediaPlayer mMediaPlayer;
    private CountDownTimer mCountDownTimer; // review by count down timer
    private long MAX_DURATION = 0;
    private File mUpSoundFile;
    private File mFile;
    private long mLastSeek;
    private long mPlayTime;
    private int mWaveIndex;
    private int[] mWaveHeights = new int[2400];
    private boolean isFinishArt;

    // view component
    private DubbingSubtitleView mDubbingSubtitleView;
    private TextView mVideoTime;
    private ProgressBar mProgressBar;
    private DubbingVideoView mDubbingVideoView;
    private ImageView mAction;
    private TextView mWaitingNum;
    private ImageView mTryListenBtn;
    private TextView mCompleteBtn;
    private WaveformView mWaveformView;
    private FrameLayout mWithdrawContainer;
    private TextView mWithdrawCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenWidth();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_dubbing);
        mFile = new File(getExternalCacheDir(), "tmp.wav");
        mAudioRecordHelper = new AudioRecordHelper(mFile);
        mAudioRecordHelper.setOnAudioRecordListener(this);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // TODO: show explanation
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO}, 1);
            }
        } else {
            initView();
            checkRoles(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDubbingVideoView != null) {
            mDubbingVideoView.onResume();
        }
//        hideNavigationBar();
        if (!MediaUtil.isHasEnoughSdcardSpace(MediaUtil.getAvailableExternalMemorySize())) {
            Toast.makeText(this, "存储空间不足！！\n5秒后退出程序", Toast.LENGTH_SHORT).show();
            mDubbingVideoView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.exit(0);
                }
            }, 5000);
        }

        if (isFinishArt) {
            showFinishedStateUi();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDubbingVideoView != null) {
            mDubbingVideoView.onPause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the task you need to do.
                    initView();
                    checkRoles(null);
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == 0) {
            mSrtEntityList = data.getParcelableArrayListExtra(SubtitleEditActivity.EXTRA_RESULT_SUBTITLE_LIST_KEY);
            mDubbingSubtitleView.init(mSrtEntityList);
        }
    }

    private void initView() {
        mDubbingSubtitleView = (DubbingSubtitleView) findViewById(R.id.subtitleView);
        mVideoTime = (TextView) findViewById(R.id.video_time);
        mWaitingNum = (TextView) findViewById(R.id.waitingNum);
        mCompleteBtn = (TextView) findViewById(R.id.complete);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mWaveformView = (WaveformView) findViewById(R.id.dubbingWaveform);
        mDubbingVideoView = (DubbingVideoView) findViewById(R.id.videoView);
        mWithdrawContainer = (FrameLayout) findViewById(R.id.withdraw_container);
        mWithdrawCount = (TextView) findViewById(R.id.withdraw_count);

        mWithdrawContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onWithdraw(mWaveformView.getCurrentTimeByIndicator());
            }
        });

        new AsyncTask<Void, Void, Void>() {
            String video = "";
            AlertDialog dialog = new AlertDialog.Builder(DubbingActivity.this)
                    .setMessage("正在处理...")
                    .create();

            @Override
            protected void onPreExecute() {
                dialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                video = processMaterialMp4FromAssets();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                dialog.cancel();
                mDubbingVideoView.setPara(video, "", false, 0, "", DubbingActivity.this, DubbingActivity.this);
            }
        }.execute();

        mAction = (ImageView) findViewById(R.id.action);
        mAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDubbing = !isDubbing;
                dubbing();
            }
        });

        mTryListenBtn = (ImageView) findViewById(R.id.review);
        mTryListenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTryListenClick();
            }
        });

        mCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processCompleteArtInNewActivity();
            }
        });
    }

    private void hideNavigationBar() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void showFinishedStateUi() {
        mDubbingVideoView.seekTo((int) mDuration);
        mWaveformView.setVisibility(View.VISIBLE);
        mWaveformView.refreshToEndPos();
        mTryListenBtn.setVisibility(View.VISIBLE);
        mAction.setVisibility(View.VISIBLE);
        mAction.setEnabled(false);
    }

    private void processCompleteArtInNewActivity() {
        final AlertDialog dialog = new AlertDialog.Builder(DubbingActivity.this)
                .setMessage("正在处理中....")
                .create();

        dialog.cancel();

        new AsyncTask<Void, Void, Void>() {
            String record;
            String video;
            String[] background;

            @Override
            protected void onPreExecute() {
                dialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
//                        record = mAudioHelper.getRecordFilePath();
//                File wave = new File(getExternalFilesDir(null), "tmp.wav");
//                record = wave.getAbsolutePath();
                record = mFile.getAbsolutePath();
                video = processMaterialMp4FromAssets();
                background = processMaterialMp3FromAssets();
                // pcm -> wav
//                if (mAudioHelper.getRecordFile() != null) {
//                    try {
//                        mAudioHelper.rawToWaveFile(mAudioHelper.getRecordFile(), wave);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
                isFinishArt = true;
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                dialog.cancel();
                DubbingPreviewActivity.launch(DubbingActivity.this,
                        record,
                        video,
                        background,
                        mSrtEntityList);
            }
        }.execute();
    }

    private String processMaterialMp4FromAssets() {
        File dir = getExternalFilesDir("material");
        String name = VIDEO[MATERIAL].split("/")[1];
        mVideoFile = new File(dir, name/*"material.mp4"*/);
        InputStream is = null;
        if (!mVideoFile.exists()) {
            AssetManager manager = getAssets();
            FileOutputStream fos = null;
            try {
                is = manager.open(VIDEO[MATERIAL]);
                fos = new FileOutputStream(mVideoFile);
                byte[] bytes = new byte[1024];
                int read;
                while ((read = is.read(bytes)) != -1) {
                    fos.write(bytes, 0, read);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return mVideoFile.getAbsolutePath();
    }

    private String[] processMaterialMp3FromAssets() {
        String[] res = new String[AUDIO[MATERIAL].length];
        File dir = getExternalFilesDir("material");
        for (int i = 0; i < AUDIO[MATERIAL].length; i++) {
            File mp3 = new File(dir, AUDIO[MATERIAL][i].split("/")[1]);
            res[i] = mp3.getAbsolutePath();
            InputStream is = null;
            if (!mp3.exists()) {
                AssetManager manager = getAssets();
                FileOutputStream fos = null;
                try {
                    is = manager.open(AUDIO[MATERIAL][0]);
                    fos = new FileOutputStream(mp3);
                    byte[] bytes = new byte[1024];
                    int read;
                    while ((read = is.read(bytes)) != -1) {
                        fos.write(bytes, 0, read);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return res;
    }

    /**
     * REVIEW BTN PERFORM CLICK
     */
    public void onTryListenClick() {
        final LevelListDrawable tryListenDrawable = (LevelListDrawable) mTryListenBtn.getDrawable();
        final int level = tryListenDrawable.getLevel();
        mTryListenBtn.setImageLevel((level + 1) % 2);
        switch (level) {
            case 0:
                startReview();
                break;
            case 1:
                stopReview();
                break;
        }
    }


    /**
     * ACTION-BTN PERFORM CLICK
     */
    private void dubbing() {
        if (isDubbing) {
            mCompleteBtn.setVisibility(View.GONE);
            toggleWaitingIndicator();
        } else {
            if (mDubbingVideoView.isPlaying()) {
                mAudioRecordHelper.stopRecord();
                mWaveformView.setVisibility(View.VISIBLE);
            } else {
                collapseWaitingIndicator();
            }
            mDubbingVideoView.stopDubbing();
            mAction.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dubbing_btn_record));
            isRecording = false;
        }
        showTryListenBtn();
    }

    private void showTryListenBtn() {
        mTryListenBtn.setVisibility(isDubbing ? View.GONE : View.VISIBLE);
        mWithdrawContainer.setVisibility(isDubbing ? View.GONE : View.VISIBLE);
        if (!isDubbing) {
            refreshWithdrawText(SRTUtil.getIndexByTime(mSrtEntityList, (int) mPlayTime));
        }
    }

    /**
     * THIS SRT SUBTITLE SHOULD FETCH FROM SDCARD BY PRE-ACTIVITY DOWNLOADED
     */
    private void checkRoles(File material) {
        primaryRole = "";
        secondRole = "";
        mSrtEntityList = SRTUtil.processSrtFromFile(this, SUBTITLE[MATERIAL]);
        if (mSrtEntityList == null || mSrtEntityList.size() == 0) return;
        mDubbingSubtitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording || isReviewing) return;
                if (mDubbingVideoView.isPlaying()) {
                    mDubbingVideoView.pause(DubbingVideoView.MODE_PREVIEW);
                }
                SubtitleEditActivity.launch(DubbingActivity.this, (ArrayList) mSrtEntityList, 0);
            }
        });
        for (SRTEntity entity : mSrtEntityList) {
            if (TextUtils.isEmpty(primaryRole)) {
                primaryRole = entity.getRole();
            } else if (!primaryRole.equals(entity.getRole()) && TextUtils.isEmpty(secondRole)) {
                secondRole = entity.getRole();
            }
        }

        if (!TextUtils.isEmpty(primaryRole) && !TextUtils.isEmpty(secondRole)) {
            initRoleView();
        }
        mDubbingSubtitleView.init(mSrtEntityList);
    }

    private void initRoleView() {
        mDubbingSubtitleView.setRolesPaint(primaryRole, secondRole);
    }

    /**
     * REFRESH TIME >> INCLUDE: PROGRESSBAR TIME-INDICATOR SRT-SUBTITLE
     */
    private void refreshTime(long playTime, long totalTime, int videoMode) {
        String str = MediaUtil.generateTime(playTime, totalTime);
        mVideoTime.setText(str);
        if (mDubbingSubtitleView != null) {
            if (videoMode == DubbingVideoView.MODE_IDEL) {
                mDubbingSubtitleView.refresh((int) playTime);
            } else {
                mDubbingSubtitleView.processTime((int) playTime);
            }
        }
        int i = (int) (100L * playTime / totalTime);
        if (videoMode == MODE_DUBBING || videoMode == MODE_IDEL) {
            mProgressBar.setProgress(i);
            mProgressBar.setSecondaryProgress(i);
            return;
        }
        mProgressBar.setSecondaryProgress(i);
    }

    private void resetPreviewUI() {
        mProgressBar.setProgress(0);
        mProgressBar.setSecondaryProgress(0);
        if (mVideoTime != null) {
            mVideoTime.setText(MediaUtil.generateTime(0, mDuration));
        }
        mDubbingSubtitleView.reset();
        mAction.setEnabled(true);
    }

    private void resetLastUiState(long time) {
        int progress = (int) (100L * time / mDuration);
        mProgressBar.setProgress(progress);
        mProgressBar.setSecondaryProgress(progress);
        if (mVideoTime != null) {
            mVideoTime.setText(MediaUtil.generateTime(time, mDuration));
        }
        mDubbingSubtitleView.refresh((int) time);
        mAction.setEnabled(true);
        mDubbingVideoView.setStackThumb(time);
    }

    /**
     * TOGGLE WAITING INDICATOR
     */
    private void toggleWaitingIndicator() {
        mAction.setImageDrawable(ContextCompat.getDrawable(DubbingActivity.this,
                R.drawable.dubbing_button_horizontal_stop));
        mWaitingNum.post(mWaitingTask);
    }

    private void collapseWaitingIndicator() {
        mWaitingNum.setVisibility(View.GONE);
    }

    private void record() {
        mPlayTime = mWaveformView.getCurrentTimeByIndicator();
        mAudioRecordHelper.startRecord(mPlayTime);
        mWaveIndex = mWaveformView.getLeftWaveLengthByIndicator();
    }

    private void playAudio() {
        mPlayTime = mWaveformView.getCurrentTimeByIndicator();
        mLastSeek = mPlayTime;
        if (mPlayTime >= mWaveformView.getCurrentTotalTime()) {
            mPlayTime = 0;
        }
        mAudioRecordHelper.play(mPlayTime);
    }

    private void resetUiWhenFinishArt() {
        mDubbingVideoView.reset(true);
        mWaveformView.refreshToStartPos();
        mLastSeek = 0;
        mPlayTime = 0;
    }

    /**
     * Withdraw click
     */
    private void onWithdraw(long mCurrentTime) {
        if (mCurrentTime == 0 || mSrtEntityList == null || mSrtEntityList.size() == 0) return;
        Log.e("ccc", "current time = " + mCurrentTime);
        int index = SRTUtil.getIndexByTime(mSrtEntityList, (int) mCurrentTime);
        Log.e("ccc", "index >. " + index);
        index = index < 0 ? 0 : index;
        long withdrawTime = SRTUtil.getTimeByIndex(mSrtEntityList, index);
        Log.e("ccc", "withdrawTime withdrawTime = " + withdrawTime);
        onWaveformScrolled(withdrawTime);
        mWaveformView.refreshByPos(withdrawTime);
    }

    private void refreshWithdrawText(int index) {
        mWithdrawCount.setText(String.valueOf(index));
    }

    private Runnable mWaitingTask = new Runnable() {
        @Override
        public void run() {
            if (isDubbing) {
                if (mWaitingNumber < 1) {
                    mWaitingNumber = 3;
                    mWaitingNum.setVisibility(View.GONE);
                    mWaveformView.setVisibility(View.INVISIBLE);
                    record();
                    mDubbingVideoView.startDubbing(mPlayTime);
                    //fixme start record audio here!!!
                    //todo: the variable 'mRecordedDuration' should change by wave bar
//                    mAudioHelper.startRecord(mWroteAccessFilePointer);
                    if (mLastSeek >= mDuration) {
                        mLastSeek = 0;
                        mWaveformView.refreshToStartPos();
                    }
                    isRecording = true;
                } else {
                    mWaitingNum.setVisibility(View.VISIBLE);
                    mDubbingVideoView.setDisabled(true);
                    mWaitingNum.setText(String.valueOf(mWaitingNumber));
                    mWaitingNum.postDelayed(mWaitingTask, 1000);
                    mWaitingNumber--;
                }
            }
        }
    };

    @Override
    public void onVideoPrepared(long duration) {
        mDuration = duration;
        if (mVideoTime != null) {
            mVideoTime.setText(MediaUtil.generateTime(0, mDuration));
        }

        mWaveformView.setDuration((int) mDuration / 1000);
        mWaveformView.setWaveformListener(this);
    }

    @Override
    public void onDoubleClick() {
        // no - op
    }

    @Override
    public void onError() {
        // no - op
    }

    @Override
    public void onLivingChanged() {
        // no - op
    }

    @Override
    public void onOverEightSeconds() {
        // no - op
    }

    @Override
    public boolean onPlayTimeChanged(long playTime, long totalTime, int videoMode) {
        mDuration = totalTime;
//        if (this.isReviewing) {
//            if (playTime >= this.reviewFlagTime) {
//                stopReview();
//                return false;
//            }
////            this.dubbingWaveform.seekToWithoutRecorder(playTime);
//            // TODO: 2017/4/27 WAVE RECORDER SHOULD UPDATE
//        }
        refreshTime(playTime, totalTime, videoMode);
        if (videoMode == MODE_DUBBING) {
            mHadRecordTime = playTime;
        }
        return true;
    }

    @Override
    public void onPreviewPlay() {
        mAction.setEnabled(false);
//        mDubbingVideoView.setDubbingLength(mWaveformView.getCurrentTotalTime());
        if (mWaveformView.getWaveHeights() != null) {
            isDubbing = true;
            showTryListenBtn();
        }
    }

    @Override
    public void onPreviewStop() {
        mDubbingSubtitleView.reset();
        resetPreviewUI();
    }

    @Override
    public void onSoundPreview() {

    }

    @Override
    public void onStarToPlay() {

    }

    @Override
    public void onStartTrackingTouch() {

    }

    @Override
    public void onStopTrackingTouch() {

    }

    @Override
    public void onVideoCompletion() {
        resetPreviewUI();
        final int mode = mDubbingVideoView.getMode();
        if (mode == MODE_DUBBING) {
            mCompleteBtn.setVisibility(View.VISIBLE);
            isDubbing = false;
            mAudioRecordHelper.stopRecord();
            dubbing();
            processCompleteArtInNewActivity();
        } else if (mode == DubbingVideoView.MODE_PREVIEW) {
            resetLastUiState(mHadRecordTime);
        } else {
            onTryListenClick();
        }
    }

    @Override
    public void onVideoPause() {

    }

    @Override
    public void onWhiteVideoPlay() {

    }

    @Override
    public void onWhiteVideoStop() {

    }

    @Override
    public void reset(boolean reset) {
//        updatePreviewUI();
    }

    private void startReview() {
        //todo play background audio
        playAudio();
        mDubbingVideoView.startReview(mPlayTime);
        mAction.setEnabled(false);
//        mProgressBar.setProgress(0);
//        mProgressBar.setSecondaryProgress(0);
//        mCountDownTimer = new CountDownTimer(mRecordedDuration, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//
//            }
//
//            @Override
//            public void onFinish() {
//                onTryListenClick();
//            }
//        };
//        mCountDownTimer.start();
        isReviewing = true;
    }

    private void stopReview() {
//        mAudioHelper.stopPlay();
        mAudioRecordHelper.stopMediaPlayer();
        mDubbingVideoView.stopReview((int) mLastSeek);
        mAction.setEnabled(true);
//        final int resetTimeByAccessFilePointer = (int) mRecordedDuration;
//        final int resetTimeByIjkVideoView = (int) mDubbingVideoView.getDubbingLength();
//        final int resetTime = resetTimeByIjkVideoView == 0 ? resetTimeByAccessFilePointer : resetTimeByIjkVideoView;
//        mProgressBar.setSecondaryProgress(0);

//        refreshTime(resetTime, mDuration, (mDuration - resetTime) > 1000 ?
//                MODE_DUBBING : DubbingVideoView.MODE_REVIEW);
        isReviewing = false;
    }

    @Override
    public void onPlayback(int time) {
        int i = (int) (100L * time / mDuration);
        mProgressBar.setSecondaryProgress(i);
        mDubbingSubtitleView.refresh(time);
        mAction.setEnabled(true);
//        mAction.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dubbing_btn_record));
        if (mWaveformView.getWaveHeights() != null) {
            isDubbing = false;
            showTryListenBtn();
        }
    }

    @Override
    public void onAudioDataReceived(long duration, long bytesRead) {
        mRecordedDuration = duration;
        mWroteAccessFilePointer = bytesRead;
    }

    @Override
    public void onProgress(int pos) {
    }

    @Override
    public void onCompletion() {
    }

    @Override
    public void onWaveformScrolled(long seek) {
        mPlayTime = seek;
        mLastSeek = seek;
        mWaveIndex = (int) (seek / mWaveformView.getPeriodPerFrame());
        refreshTime(seek, mDuration, DubbingVideoView.MODE_IDEL);
        mDubbingVideoView.seekTo((int) seek);
        if (seek < mDuration) {
            mAction.setEnabled(true);
        }
        refreshWithdrawText(SRTUtil.getIndexByTime(mSrtEntityList, (int) seek));
    }

    @Override
    public void onWaveformScrolling(long seek) {

    }

    @Override
    public void onWaveformOffset(long time) {
        mPlayTime = time;
    }

    @Override
    public void onWaveSize(int size) {
        if (mWaveIndex > mWaveHeights.length - 1) {
            mWaveHeights = Arrays.copyOf(mWaveHeights, mWaveHeights.length * 2);
        }
        mWaveHeights[mWaveIndex] = size;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWaveformView.refreshByFrame(Arrays.copyOfRange(mWaveHeights, 0, mWaveIndex));
            }
        });
        mWaveIndex++;
    }

    @Override
    public void onUpdateWaveFramePos() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWaveformView.refreshByPos(mPlayTime);
            }
        });

        mPlayTime += mWaveformView.getPeriodPerFrame();
    }

    @Override
    public void onMediaPlayerStart() {

    }

    @Override
    public void onMediaPlayerStop() {
        mPlayTime = mWaveformView.getCurrentTime();
        mWaveformView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWaveformView.refreshByPos(mLastSeek);
            }
        }, 100);

    }

    @Override
    public void onMediaPlayerComplete() {
        mWaveformView.refreshToEndPos();
        mPlayTime = mWaveformView.getCurrentTotalTime();
        if (mDubbingVideoView.isPlaying()) {
            onTryListenClick();
        }
        mWaveformView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWaveformView.refreshByPos(mLastSeek);
            }
        }, 100);

    }
}
