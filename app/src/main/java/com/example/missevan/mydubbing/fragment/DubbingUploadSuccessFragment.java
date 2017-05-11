package com.example.missevan.mydubbing.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.missevan.mydubbing.R;

/**
 * Created by dsq on 2017/5/10.
 */
public class DubbingUploadSuccessFragment extends Fragment implements View.OnClickListener {

    public static Fragment newInstance() {
        Fragment fragment = new DubbingUploadSuccessFragment();
        return fragment;
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
        return inflater.inflate(R.layout.fragment_dubbing_upload_success, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

    }

    private void initView(View v) {
        v.findViewById(R.id.upload_dubbing_share_to_wechat).setOnClickListener(this);
        v.findViewById(R.id.upload_dubbing_share_to_wechat_comment).setOnClickListener(this);
        v.findViewById(R.id.upload_dubbing_share_to_qq).setOnClickListener(this);
        v.findViewById(R.id.upload_dubbing_share_to_sina_weibo).setOnClickListener(this);
        v.findViewById(R.id.upload_dubbing_share_to_qzone).setOnClickListener(this);
        v.findViewById(R.id.upload_dubbing_review_text_view).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                getActivity().onBackPressed();
                break;
            case R.id.upload_dubbing_share_to_wechat:

                break;
            case R.id.upload_dubbing_share_to_wechat_comment:

                break;
            case R.id.upload_dubbing_share_to_qq:

                break;
            case R.id.upload_dubbing_share_to_sina_weibo:

                break;
            case R.id.upload_dubbing_share_to_qzone:

                break;
            case R.id.upload_dubbing_review_text_view:

                break;
        }
    }
}
