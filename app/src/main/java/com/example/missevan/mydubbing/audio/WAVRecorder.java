package com.example.missevan.mydubbing.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class WAVRecorder {
    private static final int RECORDER_SAMPLERATE = 8000;

    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;

    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    //输出mp3文件的路径
    private File outFile;

    private int minBuffer;
    private int inSamplerate = 16000;
    private AudioRecord audioRecord;
    private FileOutputStream outputStream;
    private volatile boolean isRecording = false;
    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format



    public WAVRecorder(File outFile) {
        this.outFile = outFile;
        if (this.outFile != null) {
            if (this.outFile.exists()) {
                this.outFile.delete();
            } else {
                try {
                    outFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public WAVRecorder(String outFilePath) {
        this(new File(outFilePath));
    }

    public void startRecord() {
        isRecording = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                record();
            }
        }, "RecordThread").start();
    }

    private void record() {
        minBuffer = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING);

        Log.i("recording", "Initialising audio recorder..");
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, /*minBuffer * 2*/BufferElements2Rec * BytesPerElement);
        audioRecord.startRecording();
        isRecording = true;

        try {
            outputStream = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        short sData[] = new short[BufferElements2Rec];

        while (isRecording) {
            // gets the voice output from microphone to byte format
            audioRecord.read(sData, 0, BufferElements2Rec);
            Log.e("ddd", "Short wirting to file " + sData.toString());
            try {
                // writes the data to file from buffer stores the voice buffer
                byte bData[] = short2byte(sData);
                outputStream.write(bData, 0, BufferElements2Rec * BytesPerElement);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    public void stopRecord() {
//        this.isRecording = false;
//    }

    public void stopRecord() {
        // stops the recording activity
        if (null != audioRecord) {
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    private void updateStatus(final String status) {

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
}
