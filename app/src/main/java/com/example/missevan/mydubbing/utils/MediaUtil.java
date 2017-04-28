package com.example.missevan.mydubbing.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.example.missevan.mydubbing.R;

/**
 * Created by missevan on 2017/4/26.
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
}
