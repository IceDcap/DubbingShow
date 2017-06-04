package com.icedcap.dubbing;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.drawable.LevelListDrawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.icedcap.dubbing.audio.AudioRecordHelper;
import com.icedcap.dubbing.entity.SRTEntity;
import com.icedcap.dubbing.listener.DubbingVideoViewEventAdapter;
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
import static com.icedcap.dubbing.view.DubbingVideoView.MODE_IDLE;

public class DubbingActivity extends AppCompatActivity implements
        WaveformView.WaveformListener,
        AudioRecordHelper.OnAudioRecordListener {
    private static final int PERMISSION_REQUEST_CODE = 0x520;
    private static final int MATERIAL = 0;
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

    boolean isReviewing = false;

    // data set
    List<SRTEntity> mSrtEntityList;
    private boolean isDubbing;
    private List<String> mPermissions = new ArrayList<>();

    // audio record relevant
    private AudioRecordHelper mAudioRecordHelper;
    private boolean isRecording;
    private long mHadRecordTime;
    private File mFile;
    private long mLastSeek;
    private long mPlayTime;
    private int mWaveIndex;
    private int[] mWaveHeights = new int[2400];
    private boolean isFinishArt;
    private int mRollbackPos;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                mPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            mPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            mPermissions.add(Manifest.permission.RECORD_AUDIO);
        }

        if (mPermissions.size() > 0) {
            String[] permissions = new String[mPermissions.size()];
            ActivityCompat.requestPermissions(this, mPermissions.toArray(permissions), PERMISSION_REQUEST_CODE);
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
        if (isReviewing) {
            onTryListenClick();
        }
        if (isRecording) {
            isDubbing = false;
            dubbing();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "您拒绝了相应的权限，无法完成配音", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                initView();
                checkRoles(null);
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

    @Override
    public void onBackPressed() {
        if (isRecording) {
            isDubbing = false;
            dubbing();
            new AlertDialog.Builder(this)
                    .setMessage("正在录音，真的退出吗？喵~")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            isDubbing = true;
                            dubbing();
                        }
                    })
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DubbingActivity.this.finish();
                        }
                    })
                    .show();
        } else {
            super.onBackPressed();
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
                mDubbingVideoView.setPara(video, "", false, 0, "", new VideoViewListener(), DubbingActivity.this);
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
                if (mWaveformView.getCurrentTotalTime() < mDuration) {
                    if (isRecording) {
                        isDubbing = !isDubbing;
                        dubbing();
                    }
                    final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(DubbingActivity.this)
                            .setMessage("当前视频还没有结束，确认要完成录制吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    processCompleteArtInNewActivity();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    dialog.show();
                }else {
                    processCompleteArtInNewActivity();
                }
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
            if (videoMode == MODE_IDLE) {
                mDubbingSubtitleView.refresh((int) playTime);
            } else {
                mDubbingSubtitleView.processTime((int) playTime);
            }
        }
        int i = (int) (100L * playTime / totalTime);
        if (videoMode == MODE_DUBBING || videoMode == MODE_IDLE) {
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

    private void resetUiWhenFinishArt() {
        mDubbingVideoView.reset(true);
        mWaveformView.refreshToStartPos();
        mLastSeek = 0;
        mPlayTime = 0;
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


    //////////////////////------video view listener----/////////////////////////////
    final class VideoViewListener extends DubbingVideoViewEventAdapter{
        @Override
        public void onVideoPrepared(long duration) {
            mDuration = duration;
            if (mVideoTime != null) {
                mVideoTime.setText(MediaUtil.generateTime(0, mDuration));
            }

            mWaveformView.setDuration((int) mDuration / 1000);
            mWaveformView.setWaveformListener(DubbingActivity.this);
        }


        @Override
        public void onDubbingComplete() {
            mCompleteBtn.setVisibility(View.VISIBLE);
            isDubbing = false;
            mAudioRecordHelper.stopRecord();
            dubbing();
            processCompleteArtInNewActivity();
        }


        @Override
        public int onPreviewPrepared() {
            return (int) mPlayTime;
        }

        @Override
        public void onPreviewPlay() {
            mAction.setEnabled(false);
            // should hide waveform if has dubbed
            if (mWaveformView.getWaveHeights() != null) {
                controlComponentVisible(mWaveformView, false);
                isDubbing = true;
                showTryListenBtn();
            }
        }

        @Override
        public void onPreviewStop(int resetPos) {
            mDubbingSubtitleView.reset();

            int i = (int) (100L * resetPos / mDuration);
            mProgressBar.setSecondaryProgress(i);
            mDubbingSubtitleView.refresh(resetPos);
            mAction.setEnabled(true);
            if (mWaveformView.getWaveHeights() != null) {
                controlComponentVisible(mWaveformView, true);
                isDubbing = false;
                showTryListenBtn();
            }
        }

        @Override
        public boolean onPlayTimeChanged(long playTime, long totalTime, int videoMode) {
            mDuration = totalTime;
            refreshTime(playTime, totalTime, videoMode);
            if (videoMode == MODE_DUBBING) {
                mHadRecordTime = playTime;
            }
            return true;
        }

    }

    //////////////////////------wave listener----/////////////////////////////
    @Override
    public void onWaveformScrolled(long seek) {
        mPlayTime = seek;
        mLastSeek = seek;
        mWaveIndex = (int) (seek / mWaveformView.getPeriodPerFrame());
        refreshTime(seek, mDuration, MODE_IDLE);
        mDubbingVideoView.seekTo((int) seek);
        mAction.setEnabled(seek < mDuration);
        int num = SRTUtil.getSubtitleNumByTime(mSrtEntityList, (int) seek);
        refreshWithdrawText(num);
    }

    @Override
    public void onWaveformScrolling(long seek) {

    }

    @Override
    public void onWaveformOffset(long time) {
        mPlayTime = time;
        if (mPlayTime > mDuration / 4) {
            mCompleteBtn.setVisibility(View.VISIBLE);
        }
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

    //////////////////////------audio record or play----/////////////////////////////

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
//        if (mDubbingVideoView.isPlaying()) {
        onTryListenClick();
//        }
        mWaveformView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWaveformView.refreshByPos(mLastSeek);
            }
        }, 100);

    }


    private void controlComponentVisible(View v, boolean isShow) {
        if (v == null) return;
        v.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    /**
     * Withdraw click
     */
    private void onWithdraw(long mCurrentTime) {
        if (mCurrentTime == 0 || mSrtEntityList == null || mSrtEntityList.size() == 0) {
            resetHaveNotDubbedState();
            return;
        }
        final int num = SRTUtil.getSubtitleNumByTime(mSrtEntityList, (int) mCurrentTime);
        if (num <= 1) {
            resetHaveNotDubbedState();
        }
        final int lastIndex = num - 2;
        long withdrawTime = SRTUtil.getTimeByIndex(mSrtEntityList, lastIndex);

        onWaveformScrolled(withdrawTime);
        mWaveformView.refreshByPos(withdrawTime);
    }

    private void resetHaveNotDubbedState() {
        mPlayTime = 0;
        mLastSeek = 0;
        mRollbackPos = 0;
        mWaveformView.reset();
        mDubbingSubtitleView.reset();
        controlComponentVisible(mWithdrawContainer, false);
        controlComponentVisible(mWaveformView, false);
        controlComponentVisible(mTryListenBtn, false);
    }

    private void refreshWithdrawText(int index) {
        controlComponentVisible(mWithdrawCount, index > 0);
        mWithdrawCount.setText(String.valueOf(index));
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
            mRollbackPos = (int) mWaveformView.getCurrentTimeByIndicator();
        }
        showTryListenBtn();
    }

    private void showTryListenBtn() {
        controlComponentVisible(mTryListenBtn, !isDubbing);
        if (mRollbackPos > 0) {
            controlComponentVisible(mWithdrawContainer, !isDubbing && !isReviewing);
        }
        if (!isDubbing) {
            int pos = SRTUtil.getSubtitleNumByTime(mSrtEntityList, (int) mPlayTime);
            refreshWithdrawText(pos);
        }
    }


    /**
     * REVIEW BTN PERFORM CLICK
     */
    public void onTryListenClick() {
        final LevelListDrawable tryListenDrawable = (LevelListDrawable) mTryListenBtn.getDrawable();
        final int level = tryListenDrawable.getLevel();
        final int i = (level + 1) % 2;
        mTryListenBtn.setImageLevel(i);
        //show or hide withdraw
        controlComponentVisible(mWithdrawContainer, i != 1);
        mWaveformView.setWaveformPlayMask(level == 0);
        switch (level) {
            case 0:
                startReview();
                break;
            case 1:
                stopReview();
                break;
        }
    }


    private void startReview() {
        isReviewing = true;
        //todo play background audio

        // play the recorded audio from indicator pos
        mPlayTime = mWaveformView.getCurrentTimeByIndicator();
        mLastSeek = mPlayTime;
        if (mPlayTime >= mWaveformView.getCurrentTotalTime()) {
            mPlayTime = 0;
        }
        mDubbingSubtitleView.refresh((int) mPlayTime);
        mAudioRecordHelper.play(mPlayTime);

        // play video view
        mDubbingVideoView.startReview(mPlayTime);
        mAction.setEnabled(false);
        mWaveformView.setMaskStartPos((int) mPlayTime);
    }

    private void stopReview() {
        //stop audio play
        mAudioRecordHelper.stopMediaPlayer();
        //stop video view
        mDubbingVideoView.stopReview((int) mLastSeek);

        mAction.setEnabled(mLastSeek < mDuration);
        isReviewing = false;
    }

}
