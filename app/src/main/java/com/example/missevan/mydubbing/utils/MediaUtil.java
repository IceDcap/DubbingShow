package com.example.missevan.mydubbing.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.example.missevan.mydubbing.R;

import java.util.Locale;

/**
 * Created by dsq on 2017/4/26.
 */

public class MediaUtil {

    public static Bitmap getThumbnail(Context context,long time, String videopath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
           retriever.setDataSource(videopath);
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

    public static String generateTime(long curr, long total) {
        return generateTime(curr) + "/" + MediaUtil.generateTime(total);
    }

    public static String generateTime(long time) {
        int k = (int) Math.floor(time / 1000.0D);
        int i = k % 60;
        int j = k / 60 % 60;
        k /= 3600;
        if (k > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", k, j, i);
        }
        return String.format(Locale.US, "%02d:%02d", j, i);
    }
}
