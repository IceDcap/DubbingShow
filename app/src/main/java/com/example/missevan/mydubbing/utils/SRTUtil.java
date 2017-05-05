package com.example.missevan.mydubbing.utils;

import android.content.Context;
import android.support.annotation.RawRes;
import android.text.TextUtils;

import com.example.missevan.mydubbing.entity.SRTEntity;
import com.example.missevan.mydubbing.entity.SRTSubtitleEntity;
import com.example.missevan.mydubbing.view.DubbingSubtitleView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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


    public static int getIndexByTime(List<SRTSubtitleEntity> entity, int time) {
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
}
