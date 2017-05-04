package com.example.missevan.mydubbing.soundfile;

import android.media.MediaMetadataRetriever;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by icedcap on 03/05/2017.
 */

public class CheapSoundFile {
    private static final char[] HEX_CHARS = null;
    static HashMap<String, Factory> sExtensionMap;
    static CheapSoundFile.Factory[] sSubclassFactories;
    static ArrayList sSupportedExtensions;
    protected File mInputFile;
    protected ProgressListener mProgressListener;
//todo
//    static {
//
//        HEX_CHARS = (char[]) localObject1;
//    }

    public static String bytesToHex(byte[] hash) {
        char[] buf = new char[hash.length * 2];
        int k = 0;
        for (int i = 0; i < hash.length; i++) {
            int m = k + 1;
            buf[k] = HEX_CHARS[hash[i] >>> 4 & 0xF];
            k = m + 1;
            buf[m] = HEX_CHARS[hash[i] & 0xF];
        }
        return new String(buf);
    }

    public static CheapSoundFile create(String fileName, ProgressListener progressListener) {
        CheapSoundFile localCheapSoundFile = null;
        File f = new File(fileName);
        if (!f.exists()) {
            try {
                throw new FileNotFoundException(fileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        String name = f.getName().toLowerCase();
        String[] components = name.split("\\.");

        for (int i = 0; i < components.length; i++) {
            CheapSoundFile.Factory factory = sExtensionMap.get(components[i]);
            if (factory != null) {
                localCheapSoundFile = factory.create();
                localCheapSoundFile.setProgressListener(progressListener);
                localCheapSoundFile.ReadFile(f);
            }
        }
        return localCheapSoundFile;
    }

    public static String[] getSupportedExtensions() {
        ArrayList localArrayList = sSupportedExtensions;
        String[] arrayOfString = new String[sSupportedExtensions.size()];
        return (String[]) localArrayList.toArray(arrayOfString);
    }

    public static boolean isFilenameSupported(String filename) {
        String lowerCase = filename.toLowerCase();
        String[] arrayOfString = lowerCase.split("\\.");
        for (int i = 0; i < arrayOfString.length; i++) {
            if (!sExtensionMap.containsKey(arrayOfString[i])) {
                return false;
            }
        }
        return true;
    }

    public void ReadFile(File inputFile) {
        mInputFile = inputFile;
    }

    public void WriteFile(File outputFile, int startFrame, int numFrames) {
    }

    public String computeMd5OfFirst10Frames() throws Exception {
        int[] frameOffsets = getFrameOffsets();
        int[] frameLens = getFrameLens();
        int numFrames = frameLens.length;

        if (numFrames > 10) {
            numFrames = 10;
        }
        MessageDigest digest = MessageDigest.getInstance("MD5");
        FileInputStream in = new FileInputStream(mInputFile);

        int pos = 0;
        int i = 0;
        while (i < numFrames) {
            int skip = frameOffsets[i] - pos;
            int len = frameLens[i];
            if (skip > 0) {
                in.skip(skip);
                pos += skip;
            }
            byte[] buffer = new byte[len];

            in.read(buffer, 0, len);
            digest.update(buffer);
            pos += len;
            i += 1;
        }
        in.close();
        return bytesToHex(digest.digest());
    }

    public int getAvgBitrateKbps() {
        return 0;
    }

    public int getChannels() {
        return 0;
    }

    public float getFileDuration() {
        MediaMetadataRetriever localMediaMetadataRetriever = new MediaMetadataRetriever();
        localMediaMetadataRetriever.setDataSource(mInputFile.getAbsolutePath());
        Integer duration = Integer.valueOf(localMediaMetadataRetriever.extractMetadata(9));
        return duration / 1000.0F;
    }

    public int getFileSizeBytes() {
        return 0;
    }

    public String getFiletype() {
        return "Unknown";
    }

    public int[] getFrameGains() {
        return null;
    }

    public int[] getFrameLens() {
        return null;
    }

    public int[] getFrameOffsets() {
        return null;
    }

    public int getNumFrames() {
        return 0;
    }

    public int getSampleRate() {
        return 0;
    }

    public int getSamplesPerFrame() {
        return 0;
    }

    public int getSeekableFrameOffset(int frame) {
        return -1;
    }

    public void setProgressListener(ProgressListener listener) {
        mProgressListener = listener;
    }

    interface ProgressListener {
        boolean reportProgress(double progress);
    }

    public interface Factory {
        CheapSoundFile create();

        String[] getSupportedExtensions();
    }


}
