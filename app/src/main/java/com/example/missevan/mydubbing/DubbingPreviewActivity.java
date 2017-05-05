package com.example.missevan.mydubbing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by dsq on 2017/5/5.
 */
public class DubbingPreviewActivity extends Activity{

    public static void launch(Activity who) {
        who.startActivity(new Intent(who, DubbingPreviewActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dubbing_preview);
    }
}
