package com.example.missevan.mydubbing.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;



import android.os.Process;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Created by missevan on 2017/5/2.
 */

public class AudioHelper {
    private static final String LOG_TAG = AudioHelper.class.getSimpleName();
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static final int EOF = -1;

    private Context mContext;
    private String mCachePath;
//    private FileOutputStream mFileOutputStream;
    private RandomAccessFile mRandomAccessFile;
    private File mFile;

    private ShortBuffer mSamples; // the samples to play
    private int mNumSamples; // number of samples to play
    private Thread mAudioPlayback;


    // config pram
    private final int SAMPLE_RATE = 44100;
    private boolean mShouldContinue;

    private OnAudioRecordPlaybackListener mListener;

    public AudioHelper(Context context) {
        mContext = context;
        File f = context.getExternalFilesDir(null);
        if (null != f) {
            mCachePath = f.getAbsolutePath();
        }
        mFile = new File(mCachePath, System.currentTimeMillis() + ".pcm");
        try {
            mRandomAccessFile = new RandomAccessFile(mFile, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public AudioHelper(Context context, OnAudioRecordPlaybackListener listener) {
        this(context);
        mListener = listener;
    }

    private void setListener(OnAudioRecordPlaybackListener listener) {
        mListener = listener;
    }


    public void setCachePath(String cachePath) {
        mCachePath = cachePath;
        File dir = new File(mCachePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        mFile = new File(mCachePath, System.currentTimeMillis() + ".pcm");
    }

    private void recordAudio(final long offset) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

                // buffer size
                int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    bufferSize = SAMPLE_RATE * 2;
                }

                short[] audioBuffer = new short[bufferSize / 2];

                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.e(LOG_TAG, "Audio Record can not initialized!");
                    return;
                }

                audioRecord.startRecording();

                Log.v(LOG_TAG, "Start recording...");
//                try {
//                    mFileOutputStream = new FileOutputStream(mFile);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
                try {
                    mRandomAccessFile.seek(offset);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                long shortsRead = 0;
                while (mShouldContinue) {
                    int numberOfShort = audioRecord.read(audioBuffer, 0, audioBuffer.length);
                    shortsRead += numberOfShort;


                    // write to storage
                    byte[] b = short2byte(audioBuffer);
                    try {
//                        mFileOutputStream.write(b, 0, bufferSize);
                        mRandomAccessFile.write(b, 0, bufferSize);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (null != mListener) {
                        mListener.onAudioDataReceived(audioBuffer);
                    }
                }

                audioRecord.stop();
                audioRecord.release();
                Log.v(LOG_TAG, String.format("Recording stopped. Samples read: %d", shortsRead));
            }
        }).start();
    }


    private void playAudio() {
        int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize, AudioTrack.MODE_STREAM);

        audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioTrack track) {
                Log.v(LOG_TAG, "Audio file end reached");
                track.release();
                if (mListener != null) {
                    mListener.onCompletion();
                }

            }

            @Override
            public void onPeriodicNotification(AudioTrack track) {
                if (mListener != null && track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    mListener.onProgress((track.getPlaybackHeadPosition() * 1000) / SAMPLE_RATE);
                }
            }
        });
        audioTrack.setPositionNotificationPeriod(SAMPLE_RATE / 30); // 30 times per second
        audioTrack.setNotificationMarkerPosition(mNumSamples);

        audioTrack.play();

        Log.v(LOG_TAG, "Audio file started");

        short[] buffer = new short[bufferSize];
        try {
            short[] samples = getSamples();
            mSamples = ShortBuffer.wrap(samples);
            mNumSamples = samples.length;
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSamples.rewind();
        int limit = mNumSamples;
        int totalWritten = 0;
        while (mSamples.position() < limit && mShouldContinue) {
            int numSamplesLeft = limit - mSamples.position();
            int samplesToWrite;
            if (numSamplesLeft >= buffer.length) {
                mSamples.get(buffer);
                samplesToWrite = buffer.length;
            } else {
                for (int i = numSamplesLeft; i < buffer.length; i++) {
                    buffer[i] = 0;
                }
                mSamples.get(buffer, 0, numSamplesLeft);
                samplesToWrite = numSamplesLeft;
            }
            totalWritten += samplesToWrite;
            audioTrack.write(buffer, 0, samplesToWrite);
        }

        if (!mShouldContinue) {
            audioTrack.release();
        }

        Log.v(LOG_TAG, "Audio streaming finished. Samples written: " + totalWritten);
    }

    /** Get had recorded time */
    public long getHadRecordTime() {
        long time = 0;
        try {
            time = mRandomAccessFile.getFilePointer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return time;
    }


    /** Record audio */
    public void startRecord(long time) {
        if (mShouldContinue) return;
        mShouldContinue = true;
        recordAudio(time);
    }

    /** Audio is recording or not */
    public boolean isRecording() {
        return mShouldContinue;
    }

    /** Stop record audio */
    public void stopRecord() {
        mShouldContinue = false;
    }


    /** Audio is playing or not */
    public boolean isPlaying() {
        return mAudioPlayback != null;
    }

    /** Audio playback */
    public void startPlay() {
        if (mAudioPlayback != null) return;
        mShouldContinue = true;
        mAudioPlayback = new Thread(new Runnable() {
            @Override
            public void run() {
                playAudio();
            }
        });
        mAudioPlayback.start();
    }

    /** Audio stop play */
    public void stopPlay() {
        if (isPlaying()) {
            mShouldContinue = false;
            mAudioPlayback = null;
        }
    }


    //Conversion of short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];

        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

    private short[] getSamples() throws IOException {
        InputStream is = new FileInputStream(mFile);
        byte[] data;
        try {
            data = toByteArray(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }

        ShortBuffer sb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] samples = new short[sb.limit()];
        sb.get(samples);
        return samples;
    }

    private byte[] toByteArray(final InputStream input) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        long count = 0;
        int n;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return output.toByteArray();
    }

    public interface OnAudioRecordPlaybackListener {
        void onAudioDataReceived(short[] data);

        void onProgress(int pos);

        void onCompletion();
    }
}
