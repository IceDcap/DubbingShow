package com.example.missevan.mydubbing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by missevan on 2017/4/28.
 */

public class SubtitleEditActivity extends AppCompatActivity {

    public static void launch(Context who) {
        who.startActivity(new Intent(who, SubtitleEditActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtitle_edit);
    }
}
