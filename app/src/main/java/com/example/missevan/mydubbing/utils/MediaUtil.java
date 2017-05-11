package com.example.missevan.mydubbing.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.example.missevan.mydubbing.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by dsq on 2017/4/26.
 * Utils of media process
 */
public class MediaUtil {

    /**
     * Get image from {@link MediaMetadataRetriever} by video path
     * @param context c
     * @param time video position
     * @param videoPath video path
     * @return {@link Bitmap} of image
     */
    public static Bitmap getThumbnail(Context context,long time, String videoPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
           retriever.setDataSource(videoPath);
            Bitmap bitmap = retriever.getFrameAtTime(time);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.home_bg_loading_recommend);
            }
            return bitmap;
        } catch (RuntimeException paramContext) {
            Log.d("SecVideoWidgetProvider", "getThumbnail localRuntimeException");
        }
        return null;
    }

    /**
     * Generate format text which header ui need
     * @param curr current time
     * @param total duration time
     * @return format text string
     */
    public static String generateTime(long curr, long total) {
        return generateTime(curr) + "/" + MediaUtil.generateTime(total);
    }

    private static String generateTime(long time) {
        int k = (int) Math.floor(time / 1000.0D);
        int i = k % 60;
        int j = k / 60 % 60;
        k /= 3600;
        if (k > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", k, j, i);
        }
        return String.format(Locale.US, "%02d:%02d", j, i);
    }

    /**
     * Judge current have enough space to record audio
     * 100M at less
     * @param available available space
     * @return true if have enough space or not
     */
    public static boolean isHasEnoughSdcardSpace(long available) {
        return available > 100 * 1024 * 1024;
    }

    /**
     * Get available internal memory size
     * @return memory size of bits
     */
    public static long getAvailableInternalMemorySize() {
        return getAvailableMemorySize(Environment.getDataDirectory());
    }

    /**
     * Get available external memory size
     * @return memory size of bits
     */
    public static long getAvailableExternalMemorySize() {
        return getAvailableMemorySize(Environment.getExternalStorageDirectory());
    }

    private static long getAvailableMemorySize(File file) {
        StatFs stat = new StatFs(file.getPath());
        long blockSize, availableBlocks;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            availableBlocks = stat.getAvailableBlocks();
        }
        return availableBlocks * blockSize;
    }

    /**
     * Convert a bitmap to png file
     * @param bitmap bitmap instance
     * @param filename filename which to write
     */
    public static void writeBitmapToLocal(Bitmap bitmap, String filename) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
