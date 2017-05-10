package com.example.missevan.mydubbing.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.missevan.mydubbing.R;
import com.example.missevan.mydubbing.utils.MediaUtil;

import static com.example.missevan.mydubbing.DubbingUploadActivity.EXTRA_VIDEO_PATH_KEY;

/**
 * Created by dsq on 2017/5/10.
 */
public class DubbingUploadEditFragment extends Fragment {
    private String mVideoPath;

    private ImageView mCover;

    public static Fragment newInstance(String videoPath) {
        Fragment fragment = new DubbingUploadEditFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_VIDEO_PATH_KEY, videoPath);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mVideoPath = getArguments().getString(EXTRA_VIDEO_PATH_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dubbing_upload_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        if (!TextUtils.isEmpty(mVideoPath)) {
            Bitmap defaultCover = MediaUtil.getThumbnail(getActivity(), 0, mVideoPath);
            mCover.setImageBitmap(defaultCover);
        }
    }

    private void initView(View v) {
        mCover = (ImageView) v.findViewById(R.id.upload_dubbing_change_cover_btn_image_view);
    }
}
