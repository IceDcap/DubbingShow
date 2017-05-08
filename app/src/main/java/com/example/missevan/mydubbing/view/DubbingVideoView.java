package com.example.missevan.mydubbing.view;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.missevan.mydubbing.DubbingPreviewActivity;
import com.example.missevan.mydubbing.MainActivity;
import com.example.missevan.mydubbing.R;
import com.example.missevan.mydubbing.audio.AudioHelper;
import com.example.missevan.mydubbing.camera.CameraContainer;
import com.example.missevan.mydubbing.camera.CameraView2;
import com.example.missevan.mydubbing.utils.AudioMedia;
import com.example.missevan.mydubbing.utils.Config;
import com.example.missevan.mydubbing.utils.MediaUtil;


import tv.danmaku.ijk.media.IjkVideoView;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.MediaInfo;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;

/**
 * Created by dsq on 2017/4/24.
 */

public class DubbingVideoView extends FrameLayout implements
        View.OnClickListener,
        IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnInfoListener {
    // static fields
    public static final int MODE_DUBBING = 0x2;
    public static final int MODE_PREVIEW = 0x1;
    public static final int MODE_REVIEW = 0x3;

    public static int POSITION_COOPERA_ACCEPTER = 0x2;
    public static int POSITION_COOPERA_INVITER = 0x1;
    public static int POSITION_SINGLE = 0x0;

    public static int STATUS_DUBBING = 0x1;
    public static int STATUS_NORMAL = 0x0;
    public static int STATUS_PAUSE_DUBBING = 0x2;
    public static int STATUS_STOP_DUBBING = 0x2;

    private static final int SHOW_PROGRESS = 0x1024;
    private static final int HIDE_STOP = 0x1025;
    private long mDubbingLength;

    // instance fields
    private AudioMedia audioMedia;
    private String audioPath;
    private View container;
    private boolean disabled;
    private int dubbing_status = POSITION_SINGLE;
    private FrameLayout flVideo;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_PROGRESS:
                    if (mIjkVideoView != null && mIsPlaying) {
                        int cur = mIjkVideoView.getCurrentPosition();
                        lasttime = cur;
//                        if (mode == MODE_REVIEW &&
//                                cur >= AudioHelper.accessFilePointer2duration(
//                                        ((MainActivity) mActivity).getRecordTime())) {
//                            ((MainActivity) mActivity).onTryListenClick();
//                            break;
//                        }
                        int total = mIjkVideoView.getDuration();
                        if (onEventListener != null) {
                            onEventListener.onPlayTimeChanged(cur, total, mode);
                        }
                        mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 100);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
    private boolean isLiving;
    private boolean isRecording;
    private int lasttime;
    private int livingPosition;
    private Activity mActivity;
    private CameraContainer mCameraContainer; //FIXME: THIS MAYBE COMING IN FUTURE
    private boolean mCameraRecordSuccess;
    private boolean mCameraRecording;
    private Context mContext;
    //custom audio class called DubbingshowAudioRecoder
    private boolean mIsPlaying;
    private boolean isPlaySourceAudio = true;
    private int mScreenHeight;
    private int mScreenWidth;
    private int mode = MODE_PREVIEW;
    private OnEventListener onEventListener;
    private boolean openBg;
    //private ImageView mPlaceholder; // placeholder
    private LinearLayout mPlay; //play
    private ImageView mThumb;
    private IjkVideoView mIjkVideoView;
    private String mVideoPath;


    public DubbingVideoView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public DubbingVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public DubbingVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }


    private void init() {
        mScreenWidth = Config.screen_width;
        mScreenHeight = Config.screen_height;//null value
        initView();
        setListener();
    }

    private void initView() {
        container = LayoutInflater.from(mContext).inflate(R.layout.dubbingliving_videoview, null, false);
        flVideo = (FrameLayout) container.findViewById(R.id.fl_video);
//        mCameraContainer = (CameraContainer) container.findViewById(R.id.cameraContainer);
        mPlay = (LinearLayout) container.findViewById(R.id.play);
        mThumb = (ImageView) container.findViewById(R.id.thumb);
        //danmuku init view
        mIjkVideoView = (IjkVideoView) container.findViewById(R.id.videoView);
        mIjkVideoView.setOnCompletionListener(this);
        mIjkVideoView.setOnErrorListener(this);
        mIjkVideoView.setOnInfoListener(this);
        mIjkVideoView.setOnPreparedListener(this);
        addView(container);
//        mCameraContainer.setContext(mContext);
//        mCameraContainer.setCameraStatusListener(new CameraStatusListener());


    }

    private void setListener() {
        mIjkVideoView.setOnClickListener(this);
        mPlay.setOnClickListener(this);
    }


    public void dubbingSeekTo(long time) {
        if (null != mIjkVideoView) {
            mIjkVideoView.seekTo((int) time);
        }
        if (null != audioMedia) {
            audioMedia.seekTo((int) time);
        }
    }

    public int getCurrentPosition() {
        if (mIjkVideoView != null) {
            return mIjkVideoView.getCurrentPosition();
        }
        return 0;
    }

    public int getDubbingStatus() {
        return dubbing_status;
    }

    public int getDuration() {
        if (null != mIjkVideoView) {
            return mIjkVideoView.getDuration();
        }
        return 0;
    }

    public int getMode() {
        return mode;
    }

    public String getVideoPath() {
        return mIjkVideoView != null ? mIjkVideoView.getVideoPath() : "";
    }

    public void pause() {
        pause(mode);
    }

    public void pause(int type) {
        switch (type) {
            case MODE_DUBBING:
                mIjkVideoView.pause();
                if (null != audioMedia) {
                    audioMedia.pause();
                }
                break;
            case MODE_PREVIEW:
                if (mDubbingLength == 0) {
                    mIjkVideoView.pause();
                    if (null != onEventListener) {
                        onEventListener.onPreviewStop();
                    }
                    // reset video view in preview mode
                    reset(true);
                } else {
                    stopPlayback((int) mDubbingLength);
                }
                break;
            case MODE_REVIEW:
                mIjkVideoView.pause();
                if (null != audioMedia) {
                    audioMedia.pause();
                }
                if (getContext() instanceof DubbingPreviewActivity && null != onEventListener) {
                    onEventListener.onWhiteVideoStop();
                }
                break;
        }
        mIsPlaying = false;
        mHandler.removeMessages(SHOW_PROGRESS);
    }

    public void pauseDubbing() {
        mode = MODE_DUBBING;
    }

    private void play() {
        play(mode);
    }

    private void play(int mode) {
        mPlay.setVisibility(GONE);
        mThumb.setVisibility(GONE);
        mIsPlaying = true;
        if (mode == MODE_DUBBING) {
            mIjkVideoView.deselectTrack(mIjkVideoView.getSelectedTrack(ITrackInfo.MEDIA_TRACK_TYPE_AUDIO));
            isPlaySourceAudio = false;
            mIjkVideoView.seekTo((int) mDubbingLength);
            mIjkVideoView.start();
            if (onEventListener != null) {
            }
        } else if (mode == MODE_PREVIEW) {
            if (!isPlaySourceAudio) {
                // this method not worked
//                mIjkVideoView.selectTrack(mIjkVideoView.getSelectedTrack(ITrackInfo.MEDIA_TRACK_TYPE_AUDIO));
                mIjkVideoView.setVideoPath(mVideoPath);
                isPlaySourceAudio = true;
            }
            mIjkVideoView.seekTo((int) mDubbingLength);
            mIjkVideoView.start();
            if (onEventListener != null) {
                onEventListener.onPreviewPlay();
            }
            mIjkVideoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                    return false;
                }
            });
        } else if (mode == MODE_REVIEW) {
            // TODO: 2017/4/27 play dubbinged audio and video
            mIjkVideoView.deselectTrack(mIjkVideoView.getSelectedTrack(ITrackInfo.MEDIA_TRACK_TYPE_AUDIO));
            isPlaySourceAudio = false;
            mIjkVideoView.seekTo(0);
            mIjkVideoView.start();
            if (onEventListener != null) {
                onEventListener.onPlayback(0);
                onEventListener.onWhiteVideoPlay();
            }
        }

        mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    public void onResume() {
        if (mIjkVideoView.getCurrentPosition() >0 ) return;
        // should show preview thumbnail on DubbingVideoView
        mThumb.setImageBitmap(MediaUtil.getThumbnail(mContext, 0/*maybe change*/, mVideoPath));
        mThumb.setVisibility(VISIBLE);
    }

    public void onPause() {
        if (isPlaying()) {
            pause(mode);
        }
    }



    public void reset(boolean keepStatus) {
        mIjkVideoView.pause();
        mIjkVideoView.seekTo(0);
        mPlay.setVisibility(VISIBLE);
        if (null != audioMedia) {
            audioMedia.seekTo(0);
        }
        if (!keepStatus) {
            dubbing_status = STATUS_NORMAL;
        }
        if (onEventListener != null) {
            onEventListener.reset(keepStatus);
        }
    }

    public void resetAV() {
        mIjkVideoView.pause();
        mIjkVideoView.seekTo(0);
        if (null != audioMedia) {
            audioMedia.seekTo(0);
        }
    }

    public void resetRecordFlag(boolean flag) {

    }

    public void resumeRecoder() {
//        mDubbingshowAudioRecoder
    }

    public void seekTo(int position) {
        if (null != mIjkVideoView) {
            mIjkVideoView.seekTo(position);
        }
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        mPlay.setVisibility(disabled ? GONE : VISIBLE);
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setPara(String videoPath, String audioPathUrl) {
        mVideoPath = videoPath;
        mIjkVideoView.setVideoPath(videoPath);
        mThumb.setImageBitmap(MediaUtil.getThumbnail(mContext, 0, videoPath));
        if (!TextUtils.isEmpty(audioPathUrl)) {
            audioMedia = new AudioMedia(audioPathUrl);
        } else {
            mIjkVideoView.pause();
        }
    }

    public void setPara(String videoPath,
                        String audioPathUrl,
                        boolean isLiving,
                        int livingPosition,
                        String sourceId,
                        OnEventListener onEventListener,
                        Activity activity) {

        this.onEventListener = onEventListener;
        mActivity = activity;
        setPara(videoPath, audioPathUrl);

    }

    public boolean isPlaying() {
        return mIjkVideoView.isPlaying();
    }

    public void setOnEventListener(OnEventListener onEventListener) {
        this.onEventListener = onEventListener;
    }

    /**
     * start dubbing
     */
    public void startDubbing() {
        mode = MODE_DUBBING;
        // TODO: 2017/4/26 START DUBBING
        play(mode);
    }

    /**
     * stop dubbing
     */
    public void stopDubbing() {
        mDubbingLength = mIjkVideoView.getCurrentPosition();
        mode = MODE_PREVIEW;
        //todo stop dubbing
        mIjkVideoView.pause();
        mPlay.setVisibility(VISIBLE);
        mHandler.removeMessages(SHOW_PROGRESS);
    }


    public void stopPlayback(int pos) {
        mIjkVideoView.pause();
        mIjkVideoView.seekTo(pos);
        mPlay.setVisibility(VISIBLE);
        if (onEventListener != null) {
            onEventListener.onPlayback(pos);
        }
        mHandler.removeMessages(SHOW_PROGRESS);
    }

    public void stopPreview(boolean isFastStop, int dubbingFlagTime) {
        if (onEventListener != null) {
            onEventListener.onVideoCompletion();
        }
        mPlay.setVisibility(VISIBLE);
        mHandler.removeMessages(SHOW_PROGRESS);
    }


    public void startReview() {
        mode = MODE_REVIEW;
        play(mode);
    }

    public void stopReview() {
        mIjkVideoView.pause();
        if (null != audioMedia) {
            audioMedia.pause();
        }
        mIsPlaying = false;
        mHandler.removeMessages(SHOW_PROGRESS);
        mPlay.setVisibility(VISIBLE);
    }

    public long getDubbingLength() {
        return mDubbingLength;
    }

    public void setDubbingLength(long dubbingLength) {
        mDubbingLength = dubbingLength;
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        stopPreview(false, 0);
        resetAV();
        lasttime = 0;
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {

        if (onEventListener != null) {
            onEventListener.onVideoPrepared(iMediaPlayer.getDuration());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.videoView:
                if (mode != MODE_DUBBING)
                    pause();
                break;
            case R.id.play:
                play();
                break;
        }
    }

    class CameraStatusListener implements CameraView2.ICameraStatusListener {
        @Override
        public void onCameraStartPreview() {
            // no - op
        }

        @Override
        public void onCameraStopPreview() {
            // no - op
        }
    }

    class MediaPlayerInfoListener implements IMediaPlayer.OnInfoListener {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
            return false;
        }
    }

    class MediaPlayerPreparedListener implements IMediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            // no - op
        }
    }


    public interface OnEventListener {

        void onVideoPrepared(long duration);

        void onDoubleClick();

        void onError();

        void onLivingChanged();

        void onOverEightSeconds();

        boolean onPlayTimeChanged(long playTime, long totalTime, int videoMode);

        void onPreviewPlay();

        void onPreviewStop();

        void onSoundPreview();

        void onStarToPlay();

        void onStartTrackingTouch();

        void onStopTrackingTouch();

        void onVideoCompletion();

        void onVideoPause();

        void onWhiteVideoPlay();
        void onWhiteVideoStop();

        void reset(boolean keepStatus);

        void onPlayback(int pos);
    }

}
