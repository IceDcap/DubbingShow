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
    private static final char[] HEX_CHARS;
    static HashMap<String, Factory> sExtensionMap;
    static CheapSoundFile.Factory[] sSubclassFactories;
    static ArrayList sSupportedExtensions;
    protected File mInputFile = null;
    protected CheapSoundFile.ProgressListener mProgressListener = null;

    static {
        int i = 4;
        Object localObject1 = new CheapSoundFile.Factory[i];
        CheapSoundFile.Factory localFactory = CheapAAC.getFactory();
        localObject1[0] = localFactory;
        Object localObject2 = CheapAMR.getFactory();
        localObject1[1] = localObject2;
        localObject2 = CheapMP3.getFactory();
        localObject1[2] = localObject2;
        localObject2 = CheapWAV.getFactory();
        localObject1[3] = localObject2;
        sSubclassFactories = (CheapSoundFile.Factory[]) localObject1;
        localObject1 = new java / util / ArrayList;
        ((ArrayList) localObject1).<init> ();
        sSupportedExtensions = (ArrayList) localObject1;
        localObject1 = new java / util / HashMap;
        ((HashMap) localObject1).<init> ();
        sExtensionMap = (HashMap) localObject1;
        localObject2 = sSubclassFactories;
        int j = localObject2.length;
        int k = 0;
        localFactory = null;
        while (k < j) {
            Object localObject3 = localObject2[k];
            String[] arrayOfString = ((CheapSoundFile.Factory) localObject3).getSupportedExtensions();
            int m = arrayOfString.length;
            i = 0;
            localObject1 = null;
            while (i < m) {
                String str = arrayOfString[i];
                sSupportedExtensions.add(str);
                HashMap localHashMap = sExtensionMap;
                localHashMap.put(str, localObject3);
                i += 1;
            }
            i = k + 1;
            k = i;
        }
        localObject1 = new char[16];
        Object tmp171_170 = localObject1;
        Object tmp172_171 = tmp171_170;
        Object tmp172_171 = tmp171_170;
        tmp172_171[0] = 48;
        tmp172_171[1] = 49;
        Object tmp181_172 = tmp172_171;
        Object tmp181_172 = tmp172_171;
        tmp181_172[2] = 50;
        tmp181_172[3] = 51;
        Object tmp190_181 = tmp181_172;
        Object tmp190_181 = tmp181_172;
        tmp190_181[4] = 52;
        tmp190_181[5] = 53;
        Object tmp199_190 = tmp190_181;
        Object tmp199_190 = tmp190_181;
        tmp199_190[6] = 54;
        tmp199_190[7] = 55;
        Object tmp210_199 = tmp199_190;
        Object tmp210_199 = tmp199_190;
        tmp210_199[8] = 56;
        tmp210_199[9] = 57;
        Object tmp221_210 = tmp210_199;
        Object tmp221_210 = tmp210_199;
        tmp221_210[10] = 97;
        tmp221_210[11] = 98;
        Object tmp232_221 = tmp221_210;
        Object tmp232_221 = tmp221_210;
        tmp232_221[12] = 99;
        tmp232_221[13] = 100;
        tmp232_221[14] = 101;
        tmp232_221[15] = 102;
        HEX_CHARS = (char[]) localObject1;
    }

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
        boolean reportProgress(double paramDouble);
    }

    public interface Factory {
        CheapSoundFile create();

        String[] getSupportedExtensions();
    }


}
