package com.icedcap.dubbing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.icedcap.dubbing.audio.AudioPlayHelper;
import net.surina.soundtouch.SoundTouch;

import com.icedcap.dubbing.entity.SRTEntity;
import com.icedcap.dubbing.listener.DubbingVideoViewEventAdapter;
import com.icedcap.dubbing.utils.MediaUtil;
import com.icedcap.dubbing.view.CircleModifierView;
import com.icedcap.dubbing.view.DubbingVideoView;
import com.icedcap.dubbing.view.PreviewSubtitleView;
import com.icedcap.dubbing.view.UprightModifierView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.icedcap.dubbing.view.CircleModifierView.DEFAULT_MAX_PROGRESS;
import static com.icedcap.dubbing.view.DubbingVideoView.MODE_FINALLY_REVIEW;

/**
 * Created by dsq on 2017/5/5.
 *
 * The dubbing audio process page
 * The audio process function include:
 * 1. personal & background audio volume gain
 * 2. personal audio pitch
 * 3. personal & background audio mix process
 * 4. personal & background audio room size process
 * 5. personal & background audio echo process
 */
public class DubbingPreviewActivity extends Activity implements View.OnClickListener, AudioPlayHelper.OnAudioRecordPlaybackListener {
    private static final String LOG_TAG = "DubbingPreviewActivity";
    private static final String EXTRA_RECORD_FILE_PATH_KEY = "extra-record-file-path-key";
    private static final String EXTRA_PITCH_FILE_PATH_KEY = "extra-pitch-file-path-key";
    private static final String EXTRA_VIDEO_FILE_PATH_KEY = "extra-video-file-path-key";
    private static final String EXTRA_BACKGROUND_FILE_PATH_KEY = "extra-background-file-path-key";
    private static final String EXTRA_SRT_SUBTITLE_KEY = "extra-srt-subtitle-key";

    private String mRecordFilePath;
    private String mVideoFilePath;
    private String[] mBackgroundFilePath;
    private List<SRTEntity> mSRTEntities;
    private long mDuration;
    private float mPersonalVolume = 0.5f;
    private float mBackgroundVolume = 0.5f;
    private boolean isPitched;
    private float mPitchParam;

    private AudioPlayHelper mAudioHelper;

    // ui components
    private UprightModifierView mPersonalUprightView;
    private UprightModifierView mBackgroundUprightView;
    private CircleModifierView mPersonalCircleView;
    private CircleModifierView mPersonalPitchCircleView;
    private CircleModifierView mBackgroundCircleView;
    private RadioGroup mPersonalRG;
//    private RadioGroup mBackgroundRG;
    private ImageView mBackgroundRG;
    private DubbingVideoView mDubbingVideoView;
    private TextView mTime;
    private TextView mRbTime;
    private ProgressBar mProgressBar;
    private PreviewSubtitleView mSubtitleView;

    private static long start;

    public static void launch(Activity who, String recordFile, String videoFile,
                              String[] backgroundFiles, List<SRTEntity> entity) {
        start = System.currentTimeMillis();
        Log.e("ddd", "start time = " + start);
        Intent intent = new Intent(who, DubbingPreviewActivity.class);
        intent.putExtra(EXTRA_RECORD_FILE_PATH_KEY, recordFile);
        intent.putExtra(EXTRA_VIDEO_FILE_PATH_KEY, videoFile);
        intent.putExtra(EXTRA_BACKGROUND_FILE_PATH_KEY, backgroundFiles);
        intent.putParcelableArrayListExtra(EXTRA_SRT_SUBTITLE_KEY, (ArrayList) entity);
        who.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_dubbing_preview);
        init();
        initData();
        initVideoView();
        setModifierProgressListener();
    }

    private void init() {
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.complete).setOnClickListener(this);
        mPersonalUprightView = (UprightModifierView) findViewById(R.id.personal_control_voice_modifier);
        mBackgroundUprightView = (UprightModifierView) findViewById(R.id.background_control_voice_modifier);

        mPersonalCircleView = (CircleModifierView) findViewById(R.id.personal_volume_modifier);
        mPersonalPitchCircleView = (CircleModifierView) findViewById(R.id.personal_pitch_voice_modifier);
//        mPersonalPitchCircleView.setIsAnimated(false);
        mBackgroundCircleView = (CircleModifierView) findViewById(R.id.background_volume_modifier);
        mPersonalRG = (RadioGroup) findViewById(R.id.personal_menu);
//        mBackgroundRG = (RadioGroup) findViewById(R.id.background_menu);

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
//                    case R.id.personal_control_voice_menu:
//                        mPersonalCircleView.setVisibility(View.GONE);
//                        mPersonalPitchCircleView.setVisibility(View.GONE);
//                        mPersonalUprightView.setVisibility(View.VISIBLE);
//                        break;
                }
            }
        });

       /* mBackgroundRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
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
        });*/
        mBackgroundRG = (ImageView) findViewById(R.id.background_volume_menu_control);
        mBackgroundRG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int level = mBackgroundRG.getDrawable().getLevel();
                mBackgroundRG.setImageLevel((level + 1) % 2);
                mBackgroundCircleView.setEnabled(level == 1);
                if (level == 0) {
                    mAudioHelper.setBackgroundVolume(0);
                } else {
                    mAudioHelper.setBackgroundVolume(mBackgroundVolume);
                }
                mBackgroundVolume = level == 0 ? 0 : ((float) mBackgroundCircleView.getModifierProgress()) / 200f;
                mAudioHelper.setBackgroundVolume(mBackgroundVolume);
            }
        });


        mDubbingVideoView = (DubbingVideoView) findViewById(R.id.videoView);
        mTime = (TextView) findViewById(R.id.video_time);
        mRbTime = (TextView) findViewById(R.id.rb_video_time);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mSubtitleView = (PreviewSubtitleView) findViewById(R.id.preview_subtitle_text_view);

    }

    private void initData() {
        Intent extraData = getIntent();
        mRecordFilePath = extraData.getStringExtra(EXTRA_RECORD_FILE_PATH_KEY);
        mVideoFilePath = extraData.getStringExtra(EXTRA_VIDEO_FILE_PATH_KEY);
        mBackgroundFilePath = extraData.getStringArrayExtra(EXTRA_BACKGROUND_FILE_PATH_KEY);
        mSRTEntities = extraData.getParcelableArrayListExtra(EXTRA_SRT_SUBTITLE_KEY);
        mSubtitleView.setSRTEntities(mSRTEntities);
        mAudioHelper = new AudioPlayHelper(this);
        mAudioHelper.setFile(new File(mRecordFilePath));
    }

    @Override
    protected void onResume() {
        super.onResume();
//        initVideoView();
        /*getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);*/
        mDubbingVideoView.onResume();
        long end = System.currentTimeMillis();
        Log.e("ddd", "spend time = " + (end - start));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDubbingVideoView.onPause();
        mAudioHelper.onPause();
    }

    private void initVideoView() {
        mDubbingVideoView.setPara(mVideoFilePath, mBackgroundFilePath[0]);
        mDubbingVideoView.findViewById(R.id.preview_text_view).setVisibility(View.GONE);
        mDubbingVideoView.setMode(MODE_FINALLY_REVIEW);
        mDubbingVideoView.onResume();
        mDubbingVideoView.setOnEventListener(new DubbingVideoViewEventAdapter() {
            @Override
            public void onVideoPrepared(long duration) {
                mDuration = duration;
                mTime.setText(MediaUtil.generateTime(0, duration));
                mRbTime.setText(MediaUtil.generateTime(0, duration));
            }

            @Override
            public boolean onPlayTimeChanged(long playTime, long totalTime, int videoMode) {
                refreshTime(playTime, totalTime);
                return true;
            }

//            @Override
//            public void onVideoCompletion() {
//                resetTime();
//            }

            @Override
            public void onFinalReviewComplete() {
                resetTime();
            }

            @Override
            public void onWhiteVideoPlay() {
                mAudioHelper.playCombineAudio(isPitched ? getPitchOutPath() : mRecordFilePath,
                        mBackgroundFilePath[0],
                        mPersonalVolume,
                        mBackgroundVolume);
            }

            @Override
            public void onWhiteVideoStop() {
                mAudioHelper.stopCombineAudio();
                resetTime();
            }

            @Override
            public void reset(boolean keepStatus) {
                mProgressBar.setProgress(0);
            }

            @Override
            public int fixThePlayMode() {
                return MODE_FINALLY_REVIEW;
            }
        });
    }

    private void refreshTime(long playTime, long totalTime) {
        String str = MediaUtil.generateTime(playTime, totalTime);
        mTime.setText(str);
        mRbTime.setText(str);
        int i = (int) (100L * playTime / totalTime);
        mProgressBar.setProgress(i);
        mSubtitleView.processTime((int) playTime);
    }

    private void resetTime() {
        mTime.setText(MediaUtil.generateTime(0, mDuration));
        mRbTime.setText(MediaUtil.generateTime(0, mDuration));
        mProgressBar.setProgress(0);
        mSubtitleView.reset();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.complete:
                DubbingUploadActivity.launch(this,
                        isPitched ? getPitchOutPath() : mRecordFilePath,
                        mVideoFilePath);
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


    /**
     * modifier ue control
     */
    private void setModifierProgressListener() {
        mPersonalCircleView.setOnModifierListener(new CircleModifierView.OnModifierListener() {
            @Override
            public void onModified(float progress) {
                // fix record audio volume
                mPersonalVolume = progress / DEFAULT_MAX_PROGRESS;
                mAudioHelper.setPersonalVolume(mPersonalVolume);
            }

            @Override
            public void onModifying(float progress) {
                mPersonalVolume = progress / DEFAULT_MAX_PROGRESS;
                mAudioHelper.setPersonalVolume(mPersonalVolume);
            }
        });

        mPersonalPitchCircleView.setOnModifierListener(new CircleModifierView.OnModifierListener() {
            @Override
            public void onModified(float progress) {
                if (progress < 0 || progress > DEFAULT_MAX_PROGRESS || mPitchParam == progress) return;
                mPitchParam = progress;
                mAudioHelper.onPause();
                mDubbingVideoView.stop();
                isPitched = true;
                process(mRecordFilePath, getPitchOutPath(), (progress - 100) * 0.12f);
            }

            @Override
            public void onModifying(float progress) {

            }
        });

        mBackgroundCircleView.setOnModifierListener(new CircleModifierView.OnModifierListener() {
            @Override
            public void onModified(float progress) {
                // fix background audio volume
                mBackgroundVolume = progress / DEFAULT_MAX_PROGRESS;
                mAudioHelper.setBackgroundVolume(mBackgroundVolume);
            }

            @Override
            public void onModifying(float progress) {
                mBackgroundVolume = progress / DEFAULT_MAX_PROGRESS;
                mAudioHelper.setBackgroundVolume(mBackgroundVolume);
            }
        });

        mPersonalUprightView.setOnModifierListener(new UprightModifierView.OnModifierListener() {
            @Override
            public void onModified(View view, float progress) {
                switch (view.getId()) {
                    case R.id.mix_voice_progress_bar:
                        break;
                    case R.id.space_progress_bar:
                        break;
                    case R.id.echo_progress_bar:
                        break;
                }
            }

            @Override
            public void onModifying(View view, float progress) {
                switch (view.getId()) {
                    case R.id.mix_voice_progress_bar:
                        break;
                    case R.id.space_progress_bar:
                        break;
                    case R.id.echo_progress_bar:
                        break;
                }
            }
        });

        mBackgroundUprightView.setOnModifierListener(new UprightModifierView.OnModifierListener() {
            @Override
            public void onModified(View view, float progress) {
                switch (view.getId()) {
                    case R.id.mix_voice_progress_bar:
                        break;
                    case R.id.space_progress_bar:
                        break;
                    case R.id.echo_progress_bar:
                        break;
                }
            }

            @Override
            public void onModifying(View view, float progress) {
                switch (view.getId()) {
                    case R.id.mix_voice_progress_bar:
                        break;
                    case R.id.space_progress_bar:
                        break;
                    case R.id.echo_progress_bar:
                        break;
                }
            }
        });
    }


    private String getPitchOutPath() {
        return getExternalFilesDir("temp").getAbsolutePath() + "/pitchFile.wav";
    }

    /// Helper class that will execute the SoundTouch processing. As the processing may take
    /// some time, run it in background thread to avoid hanging of the UI.
    protected class ProcessTask extends AsyncTask<DubbingPreviewActivity.ProcessTask.Parameters, String, Long> {
        /// Helper class to store the SoundTouch file processing parameters
        public final class Parameters {
            String inFileName;
            String outFileName;
            float tempo;
            float pitch;
        }


        /// Function that does the SoundTouch processing
        public final long doSoundTouchProcessing(DubbingPreviewActivity.ProcessTask.Parameters params) {

            SoundTouch st = new SoundTouch();
            st.setTempo(params.tempo);
            st.setPitchSemiTones(params.pitch);
            Log.i("SoundTouch", "process file " + params.inFileName);
            long startTime = System.currentTimeMillis();
            int res = st.processFile(params.inFileName, params.outFileName);
            long endTime = System.currentTimeMillis();
            float duration = (endTime - startTime) * 0.001f;

            Log.i("SoundTouch", "process file done, duration = " + duration);
//            appendToConsole("Processing done, duration " + duration + " sec.");
            publishProgress("Processing done, duration " + duration + " sec.");

            if (res != 0) {
                String err = SoundTouch.getErrorString();
//                appendToConsole("Failure: " + err);
                publishProgress("Failure: " + err);
                return -1L;
            }
            return 0L;
        }


        /// Overloaded function that get called by the system to perform the background processing
        @Override
        protected Long doInBackground(DubbingPreviewActivity.ProcessTask.Parameters... aparams) {
            return doSoundTouchProcessing(aparams[0]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mAlertDialog == null) {
                mAlertDialog = new AlertDialog.Builder(DubbingPreviewActivity.this)
                        .setMessage("正在处理...")
                        .create();
            }
            mAlertDialog.show();
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            mAlertDialog.dismiss();
//            mAlertDialog.cancel();
            mAudioHelper.playCombineAudio(getPitchOutPath(), mBackgroundFilePath[0],
                    mPersonalVolume, mBackgroundVolume);
            mDubbingVideoView.play();
        }
    }


    /**
     * Process a file with SoundTouch. Do the processing using a background processing
     * task to avoid hanging of the UI
     *
     * @param in input file
     * @param out output file
     * @param pitch range [-12, 12]
     */
    protected void process(String in, String out, float pitch) {
        try {
            DubbingPreviewActivity.ProcessTask task = new DubbingPreviewActivity.ProcessTask();
            DubbingPreviewActivity.ProcessTask.Parameters params = task.new Parameters();
            // parse processing parameters
            params.inFileName = in;
            params.outFileName = out;
            params.tempo = 1;
            params.pitch = pitch;

            // update UI about status
            appendToConsole("Process audio file :" + params.inFileName + " => " + params.outFileName);
            appendToConsole("Tempo = " + params.tempo);
            appendToConsole("Pitch adjust = " + params.pitch);

            // start SoundTouch processing in a background thread
            task.execute(params);
//			task.doSoundTouchProcessing(params);	// this would run processing in main thread

        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    private AlertDialog mAlertDialog;
    private StringBuilder mConsoleText = new StringBuilder();

    private void appendToConsole(final String message) {
        // run on UI thread to avoid conflicts
        mConsoleText.append(message);
        mConsoleText.append("\n");
        Log.e("ddd", mConsoleText.toString());

    }

}
