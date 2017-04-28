package com.example.missevan.mydubbing.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by missevan on 2017/4/24.
 */

public class DimenUtil {
    public static int dip2px(Context paramContext, float paramFloat) {
        return (int) TypedValue.applyDimension(1, paramFloat, paramContext.getResources().getDisplayMetrics());
    }

    public static int getScreenWidth(Context paramContext) {
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        ((Activity) paramContext).getWindow().getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        return localDisplayMetrics.widthPixels;
    }

    public static float px2dip(Context paramContext, float paramFloat) {
        return TypedValue.applyDimension(0, paramFloat, paramContext.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context paramContext, float paramFloat) {
        return (int) TypedValue.applyDimension(2, paramFloat, paramContext.getResources().getDisplayMetrics());
    }
}
