package com.example.missevan.mydubbing.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;


import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by dsq on 2017/5/2.
 */
public class AudioHelper {
    private static final String LOG_TAG = AudioHelper.class.getSimpleName();
    private static final String NAME_AUDIO_PERSONAL = "personal-audio";
    private static final String NAME_AUDIO_BACKGROUND = "background-audio";
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static final int EOF = -1;

    private Context mContext;
    private String mCachePath;
    //    private FileOutputStream mFileOutputStream;
    private RandomAccessFile mRandomAccessFile;
    private File mFile;
    private static long sRecordedDuration;
    private static long sWroteAccessFilePointer;

    private ShortBuffer mSamples; // the samples to play
    private int mNumSamples; // number of samples to play
    private int mRecordBufferSize;
    private Thread mAudioPlayback;
    private ThreadPoolExecutor mExecutorService;


    // config param
    private static final int SAMPLE_RATE = 44100;
    private boolean mShouldContinue;

    private OnAudioRecordPlaybackListener mListener;
    private MediaPlayer mMediaPlayer; // use for play background audio (mp3)
    private MediaPlayer mWaveMediaPlayer; // use for play background audio (wav)
    private AudioTrack mAudioTrack; // use for play personal audio (pcm)

    public AudioHelper(Context context) {
        mContext = context;
        File f = context.getExternalFilesDir(null);
        if (null != f) {
            mCachePath = f.getAbsolutePath();
        }
        mFile = new File(mCachePath,/* System.currentTimeMillis() +*/ "tmp.pcm");
        try {
            mRandomAccessFile = new RandomAccessFile(mFile, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mExecutorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

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

    public void setFile(File file) {
        mFile = file;
    }

    private void recordAudio(final long offset) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

                // buffer size
                mRecordBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                if (mRecordBufferSize == AudioRecord.ERROR || mRecordBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    mRecordBufferSize = SAMPLE_RATE * 2;
                }

                short[] audioBuffer = new short[mRecordBufferSize / 2];

                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, mRecordBufferSize);

                if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.e(LOG_TAG, "Audio Record can not initialized!");
                    return;
                }

                audioRecord.startRecording();
                final long startPoint = System.currentTimeMillis();

                Log.v(LOG_TAG, "Start recording...");

                try {
                    mRandomAccessFile.seek(offset);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                long bytesRead = 0;
                long shortsRead = 0;
                while (mShouldContinue) {
                    int numberOfShort = audioRecord.read(audioBuffer, 0, audioBuffer.length);
                    shortsRead += numberOfShort;

                    // write to storage
                    byte[] b = short2byte(audioBuffer);
                    bytesRead += b.length;
                    try {
                        mRandomAccessFile.write(b, 0, mRecordBufferSize);
                        sWroteAccessFilePointer = mRandomAccessFile.getFilePointer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                final long endPoint = System.currentTimeMillis();
                audioRecord.stop();
                sRecordedDuration += endPoint - startPoint;
                if (null != mListener) {
                    mListener.onAudioDataReceived(sRecordedDuration, sWroteAccessFilePointer);
                }
                audioRecord.release();
                Log.v(LOG_TAG, String.format("Recording stopped. Samples read: %d", shortsRead));
            }
        }).start();
    }


    // FIXME: 04/05/2017 HERE HAS BUGS >> WHEN A RECORD APPEND THE LAST RECORD AND PLAY THE TWO PARTS
    // FIXME: 04/05/2017 THE FIRST TIME PLAY IS MISS THE SECOND PART AUDIO, WHEN THE SECOND PLAY IS WORKED
    private void playPCMAudio(File file, float volume) {
        int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }

        if (mAudioTrack == null) {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize, AudioTrack.MODE_STREAM);
            if (volume >= 0 && volume <= 1) {
                mAudioTrack.setStereoVolume(volume, volume);
            }
        }

        mAudioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
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
        mAudioTrack.setPositionNotificationPeriod(SAMPLE_RATE / 30); // 30 times per second
        mAudioTrack.setNotificationMarkerPosition(mNumSamples);

        mAudioTrack.play();

        Log.v(LOG_TAG, "Audio file started");

        short[] buffer = new short[bufferSize];
        try {
            short[] samples = getSamples(file);
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
            mAudioTrack.write(buffer, 0, samplesToWrite);
        }

        if (!mShouldContinue || mSamples.position() >= limit) {
            mShouldContinue = false;
            if (mAudioTrack != null) {
                mAudioTrack.release();
            }
            mAudioTrack = null;
        }

        Log.v(LOG_TAG, "Audio streaming finished. Samples written: " + totalWritten);
    }

    private void playAudio(File file, float volume) {
        if (file.getAbsolutePath().endsWith("mp3")) {
            playMp3(file.getAbsolutePath(), volume);
        } else if (file.getAbsolutePath().endsWith("wav")){
            playWave(file.getAbsolutePath(), volume);
        } else {
            playPCMAudio(file, volume);
        }

    }

    private void playMp3(String path, final float volume) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        try {
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (volume >= 0 && volume <= 1) {
                        mp.setVolume(volume, volume);
                    }
                    mp.start();
                }
            });

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(LOG_TAG, "Media Player onError");
                    return false;
                }
            });

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    mMediaPlayer = null;
                }
            });

            FileDescriptor fd = null;
            FileInputStream fis = new FileInputStream(path);
            fd = fis.getFD();
            if (fd != null) {
                mMediaPlayer.setDataSource(fd);
                mMediaPlayer.prepare();
//                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playWave(String path, final float volume) {
        if (mWaveMediaPlayer == null) {
            mWaveMediaPlayer = new MediaPlayer();
        }
        try {
            mWaveMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (volume >= 0 && volume <= 1) {
                        mp.setVolume(volume, volume);
                    }
                    mp.start();
                }
            });

            mWaveMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(LOG_TAG, "Media Player onError");
                    return false;
                }
            });

            mWaveMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    mWaveMediaPlayer = null;
                }
            });

            FileDescriptor fd = null;
            FileInputStream fis = new FileInputStream(path);
            fd = fis.getFD();
            if (fd != null) {
                mWaveMediaPlayer.setDataSource(fd);
                mWaveMediaPlayer.prepare();
//                mediaPlayer.start();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMediaPlayer() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void stopWaveMediaPlayer() {
        if (mWaveMediaPlayer != null && mWaveMediaPlayer.isPlaying()) {
            mWaveMediaPlayer.stop();
            mWaveMediaPlayer.release();
            mWaveMediaPlayer = null;
        }
    }

    public void setVolume(float gain, MediaPlayer mediaPlayer, AudioTrack audioTrack) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.setVolume(gain, gain);
        }
        if (audioTrack != null && isPlaying()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                audioTrack.setVolume(gain);
            } else {
                audioTrack.setStereoVolume(gain, gain);
            }
        }
    }

    public void setBackgroundVolume(float gain) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(gain, gain);
        }
    }

    public void setPersonalVolume(float gain) {
        // personal audio play by mWaveMediaPlayer
        if (mWaveMediaPlayer != null) {
            mWaveMediaPlayer.setVolume(gain, gain);
        }
    }

    /**
     * Get had recorded time
     */
    public long getHadRecordTime() {
        long time = 0;
        try {
            time = mRandomAccessFile.getFilePointer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return time;
    }


    /**
     * Record audio
     */
    public void startRecord(long pointer) {
        if (mShouldContinue) return;
        mShouldContinue = true;
        recordAudio(pointer);
    }

    /**
     * Audio is recording or not
     */
    public boolean isRecording() {
        return mShouldContinue;
    }

    /**
     * Stop record audio
     */
    public void stopRecord() {
        mShouldContinue = false;
    }


    /**
     * Audio is playing or not
     */
    public boolean isPlaying() {
        return mAudioPlayback != null;
    }

    /**
     * Audio playback
     */
    public void startPlay() {
        if (mAudioPlayback != null) return;
        mShouldContinue = true;
        mAudioPlayback = new Thread(new Runnable() {
            @Override
            public void run() {
//                playAudio(mFile, -1);
                playPCMAudio(mFile, -1);
            }
        });
        mAudioPlayback.start();
    }

    /**
     * Audio stop play
     */
    public void stopPlay() {
        if (isPlaying()) {
            mShouldContinue = false;
            mAudioPlayback = null;
        }
    }

    public void playCombineAudio(String personalAudioPath, String backgroundAudioPath,
                                 float personalVolume, float backgroundVolume) {
        if (mExecutorService == null) {
            mExecutorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        }
        AudioPlayTask personalAudio = new AudioPlayTask(NAME_AUDIO_PERSONAL, personalAudioPath, personalVolume);
        AudioPlayTask backgroundAudio = new AudioPlayTask(NAME_AUDIO_BACKGROUND, backgroundAudioPath, backgroundVolume);
        mShouldContinue = true;
        mExecutorService.execute(personalAudio);
        mExecutorService.execute(backgroundAudio);
    }

    /** stop personal & background audio if playing */
    public void stopCombineAudio() {
        stopMediaPlayer();
        stopWaveMediaPlayer();
    }


    public String getRecordFilePath() {
        if (mFile != null) {
            return mFile.getAbsolutePath();
        }
        return null;
    }

    public File getRecordFile() {
        return mFile;
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

    private short[] getSamples(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file), 8 * 1024);
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

    public static long accessFilePointer2duration(long pointer) {
        if (sWroteAccessFilePointer == 0) return 0;
        if (sRecordedDuration > 0 && pointer < sWroteAccessFilePointer) {
            return sRecordedDuration * pointer / sWroteAccessFilePointer;
        }
        return 0;
    }

    public static long duration2accessFilePointer(long duration) {
        if (sRecordedDuration == 0) return 0;
        if (sWroteAccessFilePointer > 0 && duration <= sRecordedDuration) {
            return duration * sWroteAccessFilePointer / sRecordedDuration;
        }

        return 0;
    }

    /** Convert raw data to wave file */
    public void rawToWaveFile(final File rawFile, final File waveFile) throws IOException {
        DataOutputStream output = null;
        InputStream is  = null;
        byte[] bytes = new byte[(int)rawFile.length()];
        final short numChannels = 1;
        final short bitsPerSample = 16;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            WaveHeader waveHeader = new WaveHeader(WaveHeader.FORMAT_PCM, numChannels,
                    SAMPLE_RATE, bitsPerSample, bytes.length);
            waveHeader.write(output);

            is = new DataInputStream(new FileInputStream(rawFile));
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) > 0) {
                output.write(buffer, 0, len);
                output.flush();
            }
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    public interface OnAudioRecordPlaybackListener {

        void onAudioDataReceived(long duration, long bytesRead);

        void onProgress(int pos);

        void onCompletion();
    }

    private class AudioPlayTask implements Runnable {
        private float volume;
        private String name;
        private String audioFilePath;

        public AudioPlayTask(String name, String audioFilePath,
                             float volume) {
            this.name = name;
            this.audioFilePath = audioFilePath;
            this.volume = volume;
        }

        @Override
        public void run() {
            File file = new File(audioFilePath);
            if (file.exists()) {
                playAudio(file, volume);
            }
        }
    }

    public void onPause() {
        stopRecord();
        stopPlay();
        stopCombineAudio();
        stopMediaPlayer();
        stopWaveMediaPlayer();
    }
}
