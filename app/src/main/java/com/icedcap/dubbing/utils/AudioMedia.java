package com.icedcap.dubbing.utils;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by dsq on 2017/4/24.
 */

public class AudioMedia implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }

    private String mUrl;
    private MediaPlayer mediaPlayer;

    public AudioMedia(String url) {
        mUrl = url;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            return;
        } catch (IOException paramString) {
            paramString.printStackTrace();
        }
    }

    public AudioMedia(String filename, AssetManager assetManager, MediaPlayer.OnCompletionListener listener) {
        try {
            AssetFileDescriptor descriptor = assetManager.openFd(filename);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            this.mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(listener);
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    return false;
                }
            });
            mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getStartOffset());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public String getUrl() {
        return mUrl;
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void reset() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
    }

    public void seekTo(int msec) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(msec);
        }
    }

    public void setVolume(float leftVolume, float rightVolume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(leftVolume, rightVolume);
        }
    }

    public void setmUrl(String url) {
        mUrl = url;
    }

    public void start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        try {
            mediaPlayer.prepare();
            mediaPlayer.seekTo(0);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
