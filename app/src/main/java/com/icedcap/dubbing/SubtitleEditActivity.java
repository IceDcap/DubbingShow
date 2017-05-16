package com.icedcap.dubbing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.icedcap.dubbing.R;
import com.icedcap.dubbing.adapter.SubtitleEditAdapter;
import com.icedcap.dubbing.entity.SRTEntity;
import com.icedcap.dubbing.entity.SRTSubtitleEntity;

import java.util.ArrayList;

/**
 * Created by dsq on 2017/4/28.
 */

public class SubtitleEditActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String LOG_TAG = SubtitleEditActivity.class.getSimpleName();
    private static final String EXTRA_SUBTITLE_LIST_KEY = "extra-subtitle-list-key";
    public static final String EXTRA_RESULT_SUBTITLE_LIST_KEY = "extra-result-subtitle-list-key";
    // view component
    private RecyclerView mRecyclerView;
    private View mCancel;
    private View mDone;

    private SubtitleEditAdapter mAdapter;

    private ArrayList<SRTEntity> mEntities;

    public static void launch(Activity who, ArrayList<SRTEntity> subtitleEntityList, int requestCode) {
        Intent intent = new Intent(who, SubtitleEditActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_SUBTITLE_LIST_KEY, subtitleEntityList);
//        who.startActivity(intent);
        who.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtitle_edit);
        mEntities = getIntent().getParcelableArrayListExtra(EXTRA_SUBTITLE_LIST_KEY);
        init();
    }

    private void init() {
        mCancel = findViewById(R.id.back);
        mDone = findViewById(R.id.complete);
        mCancel.setOnClickListener(this);
        mDone.setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.subtitleList);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SubtitleEditAdapter(mEntities);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.complete:
                Intent i = new Intent();
                i.putExtra(EXTRA_RESULT_SUBTITLE_LIST_KEY, mAdapter.getList());
                setResult(Activity.RESULT_OK, i);
                onBackPressed();
                break;
        }
    }
}
