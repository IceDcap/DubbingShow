package com.example.missevan.mydubbing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import com.example.missevan.mydubbing.view.CircleModifierView;
import com.example.missevan.mydubbing.view.UprightModifierView;

/**
 * Created by dsq on 2017/5/5.
 */
public class DubbingPreviewActivity extends Activity {
    private UprightModifierView mPersonalUprightView;
    private UprightModifierView mBackgroundUprightView;
    private CircleModifierView mPersonalCircleView;
    private CircleModifierView mPersonalPitchCircleView;
    private CircleModifierView mBackgroundCircleView;
    private RadioGroup mPersonalRG;
    private RadioGroup mBackgroundRG;

    public static void launch(Activity who) {
        who.startActivity(new Intent(who, DubbingPreviewActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dubbing_preview);
        init();
    }

    private void init() {
        mPersonalUprightView = (UprightModifierView) findViewById(R.id.personal_control_voice_modifier);
        mBackgroundUprightView = (UprightModifierView) findViewById(R.id.background_control_voice_modifier);

        mPersonalCircleView = (CircleModifierView) findViewById(R.id.personal_volume_modifier);
        mPersonalPitchCircleView = (CircleModifierView) findViewById(R.id.personal_pitch_voice_modifier);
        mBackgroundCircleView = (CircleModifierView) findViewById(R.id.background_volume_modifier);
        mPersonalRG = (RadioGroup) findViewById(R.id.personal_menu);
        mBackgroundRG = (RadioGroup) findViewById(R.id.background_menu);

        mPersonalRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.personal_volume_menu:
                        mPersonalUprightView.setVisibility(View.GONE);
                        mPersonalPitchCircleView.setVisibility(View.GONE);
                        mPersonalCircleView.setVisibility(View.VISIBLE);
                        break;
                    case R.id.personal_pitch_voice_menu:
                        mPersonalPitchCircleView.setModifierTitle("变声");
                        mPersonalUprightView.setVisibility(View.GONE);
                        mPersonalCircleView.setVisibility(View.GONE);
                        mPersonalPitchCircleView.setVisibility(View.VISIBLE);
                        break;
                    case R.id.personal_control_voice_menu:
                        mPersonalCircleView.setVisibility(View.GONE);
                        mPersonalPitchCircleView.setVisibility(View.GONE);
                        mPersonalUprightView.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        mBackgroundRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.background_volume_menu:
                        mBackgroundUprightView.setVisibility(View.GONE);
                        mBackgroundCircleView.setVisibility(View.VISIBLE);
                        break;
                    case R.id.background_control_voice_menu:
                        mBackgroundCircleView.setVisibility(View.GONE);
                        mBackgroundUprightView.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }


}
