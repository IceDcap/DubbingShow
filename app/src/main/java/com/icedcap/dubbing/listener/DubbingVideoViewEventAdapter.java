package com.icedcap.dubbing.listener;

import com.icedcap.dubbing.view.DubbingVideoView;

/**
 * Created by icedcap on 07/05/2017.
 */

public class DubbingVideoViewEventAdapter implements DubbingVideoView.OnEventListener {
    @Override
    public void onVideoPrepared(long duration) {

    }

    @Override
    public void onDoubleClick() {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onLivingChanged() {

    }

    @Override
    public void onOverEightSeconds() {

    }

    @Override
    public boolean onPlayTimeChanged(long playTime, long totalTime, int videoMode) {
        return false;
    }

    @Override
    public void onPreviewPlay() {

    }

    @Override
    public void onPreviewStop() {

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
    public void reset(boolean keepStatus) {

    }

    @Override
    public void onPlayback(int pos) {

    }
}
