package com.example.missevan.mydubbing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.widget.FrameLayout;

import com.example.missevan.mydubbing.fragment.DubbingUploadEditFragment;

/**
 * Created by dsq on 2017/5/10.
 *
 * Dubbing upload page
 */
public class DubbingUploadActivity extends FragmentActivity {
    private static final String LOG_TAG = "DubbingUploadActivity";
    public static final String EXTRA_VIDEO_PATH_KEY = "extra-video-path-key";
    public static final String EXTRA_AUDIO_PATH_KEY = "extra-audio-path-key";
    private FrameLayout mRootView;
    private FragmentManager mFragmentManager;

    private String mVideoPath;
    private String mAudioPath;


    public static void launch(Context who, String audioPath, String videoPath) {
        Intent intent = new Intent(who, DubbingUploadActivity.class);
        intent.putExtra(EXTRA_AUDIO_PATH_KEY, audioPath);
        intent.putExtra(EXTRA_VIDEO_PATH_KEY, videoPath);
        who.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRootView = new FrameLayout(this);
        mRootView.setId(R.id.upload_dubbing_root_view);
        setContentView(mRootView);
        mFragmentManager = getSupportFragmentManager();
        initFirstFragment();
    }


    private void initFirstFragment() {
        String path = getIntent().getStringExtra(EXTRA_VIDEO_PATH_KEY);
        if (TextUtils.isEmpty(path)) return;
        mFragmentManager.beginTransaction()
                .add(R.id.upload_dubbing_root_view, DubbingUploadEditFragment.newInstance(path))
                .commit();
    }

    private void changeFragment() {
    }
}
