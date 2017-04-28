package com.example.missevan.mydubbing;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.LevelListDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.missevan.mydubbing.audio.ExtAudioRecorder;
import com.example.missevan.mydubbing.audio.Mp3Recorder;
import com.example.missevan.mydubbing.audio.WAVRecorder;
import com.example.missevan.mydubbing.entity.SRTEntity;
import com.example.missevan.mydubbing.utils.Config;
import com.example.missevan.mydubbing.utils.SRTUtil;
import com.example.missevan.mydubbing.view.DubbingVideoView;
import com.example.missevan.mydubbing.view.DubbingSubtitleView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements DubbingVideoView.OnEventListener {
    //    private static final String VIDEO = "/sdcard/dubbing/source/4625866548090916879/4625866548090916879.mp4";
    private static final String VIDEO = "/sdcard/dubbing/source/4803081086444687938/4803081086444687938.mp4";
    //    private static final String VIDEO = "/sdcard/test.mp4";
    private static final String AUDIO = "/sdcard/dubbing/source/4625866548090916879/4768294820698703403.mp3";
    private static final String BASE = "/sdcard/MyDubbing/audio_temp/";
//    private static String MP3_PATH;
    private static String WAV_PATH;


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
    List<SRTEntity> srtEntityList;
    private boolean isDubbing;

    // audio record relevant
//    private Mp3Recorder mMp3Recorder;
    private WAVRecorder mWAVRecorder;
    private boolean isRecording;
    private MediaPlayer mMediaPlayer;
    private CountDownTimer timer;
    private long MAX_DURATION = 0;
    private File mUpSoundFile;

    // view component
    private DubbingSubtitleView mDubbingSubtitleView;
    private TextView mVideoTime;
    private ProgressBar mProgressBar;
    private DubbingVideoView mDubbingVideoView;
    private ImageView mAction;
    private TextView mWaitingNum;
    private ImageView mTryListenBtn;
    private TextView mCompleteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenWidth();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.dubbing);

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

    private void initView() {
        mDubbingSubtitleView = (DubbingSubtitleView) findViewById(R.id.subtitleView);
        mVideoTime = (TextView) findViewById(R.id.video_time);
        mWaitingNum = (TextView) findViewById(R.id.waitingNum);
        mCompleteBtn = (TextView) findViewById(R.id.complete);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mDubbingVideoView = (DubbingVideoView) findViewById(R.id.videoView);
        mDubbingVideoView.setPara(VIDEO, "", false, 0, "", this, this);
        mAction = (ImageView) findViewById(R.id.action);
        mAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dubbing();
                isDubbing = !isDubbing;
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
                // TODO: 2017/4/28 COMPLETE DUBBING AND SEND SERVICES TO ENCODING
            }
        });
    }

    private void onTryListenClick() {
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
        if (!isDubbing) {
            mCompleteBtn.setVisibility(View.GONE);
            toggleWaitingIndicator();
        } else {
            mDubbingVideoView.stopDubbing();
            //fixme: pause record audio here!!!
            stopRecording();
            mAction.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dubbing_btn_record));
        }
        showTryListenBtn();
    }

    private void showTryListenBtn() {
        mTryListenBtn.setVisibility(!isDubbing ? View.GONE : View.VISIBLE);
    }


    /*******************************
     * AUDIO RECORD
     **********************************/
    void startRecording() {
//        if (TextUtils.isEmpty(WAV_PATH)) {
//            File f = new File(BASE);
//            if (!f.exists()) {
//                f.mkdirs();
//            }
//            WAV_PATH = BASE + System.currentTimeMillis() + ".wav";
//        }
//        if (mWAVRecorder == null) {
//            mWAVRecorder = new WAVRecorder(WAV_PATH);
//        }
//
//        if (!mWAVRecorder.isRecording()) {
//            mWAVRecorder.startRecord();
//        }
//        mUpSoundFile = new File(WAV_PATH);
//        isRecording = mWAVRecorder.isRecording();

        ExtAudioRecorder extAudioRecorder = new ExtAudioRecorder(true, MediaRecorder.AudioSource.MIC,
                AudioFormat.SAMPLE_RATE_UNSPECIFIED, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_8BIT);
    }

    void stopRecording() {
//        if (mWAVRecorder != null) {
//            mWAVRecorder.stopRecord();
//        }
//        complete();
//        isRecording = mWAVRecorder.isRecording();
    }

    void audition() {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(WAV_PATH);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            MAX_DURATION = mMediaPlayer.getDuration();
            mMediaPlayer.setLooping(true);
        } catch (IOException e) {
            Toast.makeText(this, "加载文件失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    void stopAudition() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * 重录
     */
    void retryRecording() {
        resetAudio();
    }


    void resetAudio() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }

        if (timer != null)
            timer.cancel();

        if (mUpSoundFile != null) {
            mUpSoundFile.delete();
            mUpSoundFile = null;
        }
        isRecording = false;
    }

    void complete() {
        if (timer != null)
            timer.cancel();
    }

    /*****************************************************************/


    /**
     * THIS SRT SUBTITLE SHOULD FETCH FROM SDCARD BY PRE-ACTIVITY DOWNLOADED
     */
    private void checkRoles(File material) {
        primaryRole = "";
        secondRole = "";
        srtEntityList = SRTUtil.processSrtFromFile(this, R.raw.subtitle2);
        if (srtEntityList == null || srtEntityList.size() == 0) return;
        for (SRTEntity entity : srtEntityList) {
            if (TextUtils.isEmpty(primaryRole)) {
                primaryRole = entity.getRole();
            } else if (!primaryRole.equals(entity.getRole()) && TextUtils.isEmpty(secondRole)) {
                secondRole = entity.getRole();
            }
        }

        if (!TextUtils.isEmpty(primaryRole) && !TextUtils.isEmpty(secondRole)) {
            initRoleView();
        }
        mDubbingSubtitleView.init(srtEntityList);
    }

    private void initRoleView() {
        mDubbingSubtitleView.setRolesPaint(primaryRole, secondRole);
    }

    /**
     * REFRESH TIME >> INCLUDE: PROGRESSBAR TIME-INDICATOR SRT-SUBTITLE
     */
    private void refreshTime(long playTime, long totalTime, int videoMode) {
        String str = generateTime(playTime) + "/" + generateTime(totalTime);
        mVideoTime.setText(str);
        if (mDubbingSubtitleView != null) {
            mDubbingSubtitleView.processTime((int) playTime);
        }
        int i = (int) (100L * playTime / totalTime);
        if (videoMode == DubbingVideoView.MODE_DUBBING) {
            mProgressBar.setProgress(i);
            return;
        }
        mProgressBar.setSecondaryProgress(i);
    }

    public static String generateTime(long time) {
        int k = (int) Math.floor(time / 1000.0D);
        int i = k % 60;
        int j = k / 60 % 60;
        k /= 3600;
        if (k > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", k, j, i);
        }
        return String.format(Locale.US, "%02d:%02d", j, i);
    }

    private void resetPreviewUI() {
        mProgressBar.setProgress(0);
        mProgressBar.setSecondaryProgress(0);
        if (mVideoTime != null) {
            mVideoTime.setText(generateTime(0) + "/" + generateTime(mDuration));
        }
        mDubbingSubtitleView.reset();
        mAction.setEnabled(true);
    }

    /**
     * TOGGLE WAITING INDICATOR
     */
    private void toggleWaitingIndicator() {
        mWaitingNum.post(mWaitingTask);
    }

    private Runnable mWaitingTask = new Runnable() {
        @Override
        public void run() {
            if (mWaitingNumber < 1) {
                mWaitingNumber = 3;
                mWaitingNum.setVisibility(View.GONE);
                mDubbingVideoView.startDubbing();
                //fixme start record audio here!!!
                startRecording();
                mAction.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,
                        R.drawable.dubbing_button_horizontal_stop));
            } else {
                mWaitingNum.setVisibility(View.VISIBLE);
                mDubbingVideoView.setDisabled(true);
                mWaitingNum.setText(String.valueOf(mWaitingNumber));
                mWaitingNum.postDelayed(mWaitingTask, 1000);
                mWaitingNumber--;
            }
        }
    };

    @Override
    public void onVideoPrepared(long duration) {
        mDuration = duration;
        if (mVideoTime != null) {
            mVideoTime.setText(generateTime(0) + "/" + generateTime(duration));
        }
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
        if (this.isReviewing) {
            if (playTime >= this.reviewFlagTime) {
                stopReview();
                return false;
            }
//            this.dubbingWaveform.seekToWithoutRecorder(playTime);
            // TODO: 2017/4/27 WAVE RECORDER SHOULD UPDATE
        }
        refreshTime(playTime, totalTime, videoMode);
        return true;
    }

    @Override
    public void onPreviewPlay() {
        mAction.setEnabled(false);
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
        if (mode == DubbingVideoView.MODE_DUBBING) {
            mCompleteBtn.setVisibility(View.VISIBLE);
            stopRecording();
            isDubbing = false;
            showTryListenBtn();
        } else if (mode == DubbingVideoView.MODE_PREVIEW) {

        } else {
            onTryListenClick();
        }
    }

    @Override
    public void onVideoPause() {

    }

    @Override
    public void onVideoPlay() {

    }

    @Override
    public void reset(boolean reset) {
//        updatePreviewUI();
    }

    private void startReview() {
        audition();
        mDubbingVideoView.startReview();
        mAction.setEnabled(false);
        mProgressBar.setProgress(0);
        mProgressBar.setSecondaryProgress(0);
    }

    private void stopReview() {
        stopAudition();
        mDubbingVideoView.stopReview();
        mAction.setEnabled(true);
        mProgressBar.setProgress(0);
        mProgressBar.setSecondaryProgress(0);
        mDubbingSubtitleView.refresh(0);
    }

    @Override
    public void onPlayback(int time) {
        int i = (int) (100L * time / mDuration);
        mProgressBar.setSecondaryProgress(i);
        mDubbingSubtitleView.refresh(time);
        mAction.setEnabled(true);
        mAction.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dubbing_btn_record));
    }

}
