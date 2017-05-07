package com.example.missevan.mydubbing;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.missevan.mydubbing.audio.AudioHelper;
import com.example.missevan.mydubbing.listener.DubbingVideoViewEventAdapter;
import com.example.missevan.mydubbing.utils.MediaUtil;
import com.example.missevan.mydubbing.view.CircleModifierView;
import com.example.missevan.mydubbing.view.DubbingVideoView;
import com.example.missevan.mydubbing.view.UprightModifierView;

import java.io.File;

import static com.example.missevan.mydubbing.view.DubbingVideoView.MODE_REVIEW;

/**
 * Created by dsq on 2017/5/5.
 */
public class DubbingPreviewActivity extends Activity implements View.OnClickListener, AudioHelper.OnAudioRecordPlaybackListener {
    private static final String LOG_TAG = "DubbingPreviewActivity";
    private static final String EXTRA_RECORD_FILE_PATH_KEY = "extra-record-file-path-key";
    private static final String EXTRA_VIDEO_FILE_PATH_KEY = "extra-video-file-path-key";
    private static final String EXTRA_BACKGROUND_FILE_PATH_KEY = "extra-background-file-path-key";

    private String mRecordFilePath;
    private String mVideoFilePath;
    private String[] mBackgroundFilePath;
    private long mDuration;

    private AudioHelper mAudioHelper;

    // ui components
    private UprightModifierView mPersonalUprightView;
    private UprightModifierView mBackgroundUprightView;
    private CircleModifierView mPersonalCircleView;
    private CircleModifierView mPersonalPitchCircleView;
    private CircleModifierView mBackgroundCircleView;
    private RadioGroup mPersonalRG;
    private RadioGroup mBackgroundRG;
    private DubbingVideoView mDubbingVideoView;
    private TextView mTime;
    private SeekBar mSeekBar;

    public static void launch(Activity who, String recordFile, String videoFile, String[] backgroundFiles) {
        Intent intent = new Intent(who, DubbingPreviewActivity.class);
        intent.putExtra(EXTRA_RECORD_FILE_PATH_KEY, recordFile);
        intent.putExtra(EXTRA_VIDEO_FILE_PATH_KEY, videoFile);
        intent.putExtra(EXTRA_BACKGROUND_FILE_PATH_KEY, backgroundFiles);
        who.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.layout_dubbing_preview);
        init();
        initData();
        setModifierProgressListener();
    }

    private void init() {
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.complete).setOnClickListener(this);
        mPersonalUprightView = (UprightModifierView) findViewById(R.id.personal_control_voice_modifier);
        mBackgroundUprightView = (UprightModifierView) findViewById(R.id.background_control_voice_modifier);

        mPersonalCircleView = (CircleModifierView) findViewById(R.id.personal_volume_modifier);
        mPersonalPitchCircleView = (CircleModifierView) findViewById(R.id.personal_pitch_voice_modifier);
        mBackgroundCircleView = (CircleModifierView) findViewById(R.id.background_volume_modifier);
        mPersonalRG = (RadioGroup) findViewById(R.id.personal_menu);
        mBackgroundRG = (RadioGroup) findViewById(R.id.background_menu);

        mPersonalRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.personal_volume_menu:
                        mPersonalUprightView.setVisibility(View.GONE);
                        mPersonalPitchCircleView.setVisibility(View.GONE);
                        mPersonalCircleView.setVisibility(View.VISIBLE);
                        break;
                    case R.id.personal_pitch_voice_menu:
                        mPersonalPitchCircleView.setModifierTitle("变声");
                        mPersonalUprightView.setVisibility(View.GONE);
                        mPersonalCircleView.setVisibility(View.GONE);
                        mPersonalPitchCircleView.setVisibility(View.VISIBLE);
                        break;
                    case R.id.personal_control_voice_menu:
                        mPersonalCircleView.setVisibility(View.GONE);
                        mPersonalPitchCircleView.setVisibility(View.GONE);
                        mPersonalUprightView.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        mBackgroundRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.background_volume_menu:
                        mBackgroundUprightView.setVisibility(View.GONE);
                        mBackgroundCircleView.setVisibility(View.VISIBLE);
                        break;
                    case R.id.background_control_voice_menu:
                        mBackgroundCircleView.setVisibility(View.GONE);
                        mBackgroundUprightView.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        mDubbingVideoView = (DubbingVideoView) findViewById(R.id.videoView);
        mTime = (TextView) findViewById(R.id.video_time);
        mSeekBar = (SeekBar) findViewById(R.id.progress);
    }

    private void initData() {
        Intent extraData = getIntent();
        mRecordFilePath = extraData.getStringExtra(EXTRA_RECORD_FILE_PATH_KEY);
        mVideoFilePath = extraData.getStringExtra(EXTRA_VIDEO_FILE_PATH_KEY);
        mBackgroundFilePath = extraData.getStringArrayExtra(EXTRA_BACKGROUND_FILE_PATH_KEY);
        mAudioHelper = new AudioHelper(this, this);
        mAudioHelper.setFile(new File(mRecordFilePath));
    }

    @Override
    protected void onResume() {
        super.onResume();
        initVideoView();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDubbingVideoView.onPause();
    }

    private void initVideoView() {
        mDubbingVideoView.setPara(mVideoFilePath, mBackgroundFilePath[0]);
        mDubbingVideoView.findViewById(R.id.preview_text_view).setVisibility(View.GONE);
        mDubbingVideoView.setMode(MODE_REVIEW);
        mDubbingVideoView.onResume();
        mDubbingVideoView.setOnEventListener(new DubbingVideoViewEventAdapter(){
            @Override
            public void onVideoPrepared(long duration) {
                mDuration = duration;
                mTime.setText(MediaUtil.generateTime(0, duration));
            }
            @Override
            public boolean onPlayTimeChanged(long playTime, long totalTime, int videoMode) {
                refreshTime(playTime, totalTime, videoMode);
                return true;
            }

            @Override
            public void onVideoCompletion() {
                resetTime();
            }

            @Override
            public void onWhiteVideoPlay() {
                mAudioHelper.startPlay();
            }
        });
    }

    private void refreshTime(long playTime, long totalTime, int videoMode) {
        String str = MediaUtil.generateTime(playTime, totalTime);
        mTime.setText(str);
        int i = (int) (100L * playTime / totalTime);
        mSeekBar.setProgress(i);
    }

    private void resetTime() {
        mTime.setText(MediaUtil.generateTime(0,mDuration));
        mSeekBar.setProgress(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.complete:

                break;
        }
    }

    @Override
    public void onAudioDataReceived(long duration, long bytesRead) {

    }

    @Override
    public void onProgress(int pos) {

    }

    @Override
    public void onCompletion() {

    }


    /** modifier ui control */
    private void setModifierProgressListener() {
        mPersonalCircleView.setOnModifierListener(new CircleModifierView.OnModifierListener() {
            @Override
            public void onModified(float progress) {
                // fix record audio volume
            }

            @Override
            public void onModifying(float progress) {

            }
        });

        mPersonalPitchCircleView.setOnModifierListener(new CircleModifierView.OnModifierListener() {
            @Override
            public void onModified(float progress) {

            }

            @Override
            public void onModifying(float progress) {

            }
        });

        mBackgroundCircleView.setOnModifierListener(new CircleModifierView.OnModifierListener() {
            @Override
            public void onModified(float progress) {
                // fix background audio volume
            }

            @Override
            public void onModifying(float progress) {

            }
        });

        mPersonalUprightView.setOnModifierListener(new UprightModifierView.OnModifierListener() {
            @Override
            public void onModified(float progress) {

            }

            @Override
            public void onModifying(float progress) {

            }
        });

        mBackgroundUprightView.setOnModifierListener(new UprightModifierView.OnModifierListener() {
            @Override
            public void onModified(float progress) {

            }

            @Override
            public void onModifying(float progress) {

            }
        });
    }

}
