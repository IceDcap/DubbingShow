package com.icedcap.dubbing.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.icedcap.dubbing.DubbingUploadActivity;
import com.icedcap.dubbing.R;

/**
 * Created by dsq on 2017/5/10.
 */
public class DubbingUploadingFragment extends Fragment implements View.OnClickListener{


    public static Fragment newInstance() {
        return new DubbingUploadingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dubbing_uploading, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View v) {
        v.findViewById(R.id.cancel).setOnClickListener(this);
        v.findViewById(R.id.upload_dubbing_background).setOnClickListener(this);

        //// FIXME: 2017/5/11 should change the logic by rustfull api
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((DubbingUploadActivity)getActivity()).launchUploadSuccessPage();
            }
        }, 5000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                getActivity().onBackPressed();
                break;
        }
    }
}
