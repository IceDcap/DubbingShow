package com.example.missevan.mydubbing.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class Mp3Recorder {

    //输出mp3文件的路径
    private File outFile;

    private int minBuffer;
    private int inSamplerate = 16000;
    private AudioRecord audioRecord;
    private AndroidLame androidLame;
    private FileOutputStream outputStream;
    private volatile boolean isRecording = false;

    public Mp3Recorder(File outFile){

        this.outFile = outFile;
        if(this.outFile != null){
            if(this.outFile.exists()){
                this.outFile.delete();
            }else {
                try {
                    outFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Mp3Recorder(String outFilePath){

        this(new File(outFilePath));
    }

    public void startRecord() {

        isRecording = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                record();
            }
        }).start();
    }

    private void record(){
        minBuffer = AudioRecord.getMinBufferSize(inSamplerate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        Log.i("recording", "Initialising audio recorder..");
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, inSamplerate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBuffer * 2);

        //5 seconds data
        Log.i("recording", "creating short buffer array");
        short[] buffer = new short[inSamplerate * 2 * 5];

        // 'mp3buf' should be at least 7200 bytes long
        // to hold all possible emitted data.
        Log.i("recording", "creating mp3 buffer");
        byte[] mp3buffer = new byte[(int) (7200 + buffer.length * 2 * 1.25)];

        try {
            outputStream = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Log.i("recording", "Initialising Andorid Lame");
        androidLame = new LameBuilder()
                .setInSampleRate(inSamplerate)
                .setOutChannels(1)
                .setOutBitrate(32)
                .setOutSampleRate(inSamplerate)
                .build();

        Log.i("recording", "started audio recording");
        updateStatus("Recording...");
        try {
            audioRecord.startRecording();
        }catch (IllegalStateException e){}


        int bytesRead = 0;


        while (isRecording) {

            Log.i("recording", "reading to short array buffer, buffer sze- " + minBuffer);
            bytesRead = audioRecord.read(buffer, 0, minBuffer);
            Log.i("recording", "bytes read=" + bytesRead);

            if (bytesRead > 0) {

                Log.i("recording", "encoding bytes to mp3 buffer..");
                int bytesEncoded = androidLame.encode(buffer, buffer, bytesRead, mp3buffer);
                Log.i("recording", "bytes encoded=" + bytesEncoded);

                if (bytesEncoded > 0) {
                    try {
                        Log.i("recording", "writing mp3 buffer to outputstream with " + bytesEncoded + " bytes");
                        outputStream.write(mp3buffer, 0, bytesEncoded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        Log.i("recording", "stopped recording");
        updateStatus("Recording stopped");

        Log.i("recording", "flushing final mp3buffer");
        int outputMp3buf = androidLame.flush(mp3buffer);
        Log.i("recording", "flushed " + outputMp3buf + " bytes");

        if (outputMp3buf > 0) {
            try {
                Log.i("recording", "writing final mp3buffer to outputstream");
                outputStream.write(mp3buffer, 0, outputMp3buf);
                Log.i("recording", "closing output stream");
                outputStream.close();
                updateStatus("Output recording saved in " + outFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.i("recording", "releasing audio recorder");
        audioRecord.stop();
        audioRecord.release();

        Log.i("recording", "closing android lame");
        androidLame.close();
    }

    public void stopRecord(){
        this.isRecording = false;
    }

    public boolean isRecording(){
        return isRecording;
    }

    private void updateStatus(final String status) {

    }
}
