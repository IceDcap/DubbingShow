package com.icedcap.dubbing.utils;

import android.content.Context;
import android.support.annotation.RawRes;

import com.icedcap.dubbing.entity.SRTEntity;
import com.icedcap.dubbing.entity.SRTSubtitleEntity;
import com.icedcap.dubbing.view.DubbingSubtitleView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dsq on 2017/4/24.
 */

public class SRTUtil {

    public static List<SRTSubtitleEntity> processToSubtitleList(List<SRTEntity> list) {
        List<SRTSubtitleEntity> result = new ArrayList<>(list.size());

        for (int i = 0; i < list.size(); i++) {
            SRTEntity cur = list.get(i);
            boolean isShowAnim;
            int type;
            if (i == 0) {
                isShowAnim = cur.getStarttime() >= DubbingSubtitleView.LONG_BREAK_TIME;
                type = SRTSubtitleEntity.SHOWROLE_TYPE;
            } else {
                isShowAnim = cur.getStarttime() - list.get(i - 1).getEndtime() >= DubbingSubtitleView.LONG_BREAK_TIME;
                type = cur.getRole().equals(list.get(i - 1).getRole()) ? SRTSubtitleEntity.SHOWNORMAL_TYPE : SRTSubtitleEntity.SHOWROLE_TYPE;
            }

            result.add(new SRTSubtitleEntity(type, cur, isShowAnim));
        }
        return result;
    }

    public static List<SRTEntity> processSrtFromFile(Context context, @RawRes int id/*test*/) {
        List<SRTEntity> res = new ArrayList<>();

        JSONObject jsonObject = null;
        StringBuilder json = new StringBuilder();
        InputStream fakeData = context.getResources().openRawResource(id);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fakeData));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            jsonObject = json.length() > 0 ? new JSONObject(json.toString()) : null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (jsonObject == null) return null;
        try {
            if (jsonObject.isNull("status") || !jsonObject.getString("status").equals("success")) {
                return null;
            }
            JSONObject info = jsonObject.getJSONObject("info");
            if (info == null) return null;
            JSONArray jsonArray = info.getJSONArray("subtitle");
            if (jsonArray == null) return null;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject subtitle = jsonArray.getJSONObject(i);
                if (subtitle == null) continue;
                String content = subtitle.getString("context");
                int stime = subtitle.getInt("stime");
                int etime = subtitle.getInt("etime");
                String role = subtitle.getString("role");
                res.add(new SRTEntity(role, stime, etime, content));
            }
            return res;
        } catch (Exception e) {
            return null;
        }

    }


    public static int getIndexByTime(List<? extends SRTEntity> entity, int time) {
        int index = 0;

        for (; index < entity.size(); index++) {
            int st = entity.get(index).getStarttime();
            int et = entity.get(index).getStarttime();
            if (time < st || time < et) {
                return index > 0 ? index - 1 : 0;
            }
        }
        return index > 0 ? index - 1 : 0;
    }

    public static long getTimeByIndex(List<? extends SRTEntity> entity, int index) {
        int res = 0;
        if (entity == null || entity.size() == 0 || index < 0 || index > entity.size() - 1)
            return res;
        final int s = entity.get(index).getStarttime();
        final int i = index - 1;
        if (i >= 0) {
            final int e = entity.get(i).getEndtime();
            int halfInt = (s - e) / 2;
            halfInt = halfInt > 1000 ? 1000 : halfInt;
            res = s - halfInt;
        }
        return res;
    }
}
