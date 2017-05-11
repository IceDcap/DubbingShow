package com.example.missevan.mydubbing.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.missevan.mydubbing.DubbingUploadActivity;
import com.example.missevan.mydubbing.R;
import com.example.missevan.mydubbing.utils.MediaUtil;

import static com.example.missevan.mydubbing.DubbingUploadActivity.EXTRA_VIDEO_PATH_KEY;
import static com.example.missevan.mydubbing.DubbingUploadActivity.EXTRA_AUDIO_PATH_KEY;

/**
 * Created by dsq on 2017/5/10.
 */
public class DubbingUploadEditFragment extends Fragment implements View.OnClickListener{

    private String mVideoPath;
    private String mPendingPostTitle;
    private String mPendingPostContent;
    private String mPendingPostCorverPath;
    private String mPendingPostAudioPath;

    private ImageView mCover;
    private AlertDialog mAlertDialog;

    public static Fragment newInstance(String videoPath, String pendingPostAudioPath) {
        Fragment fragment = new DubbingUploadEditFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_VIDEO_PATH_KEY, videoPath);
        bundle.putString(EXTRA_AUDIO_PATH_KEY, pendingPostAudioPath);
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
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dubbing_upload_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        if (!TextUtils.isEmpty(mVideoPath)) {
            Bitmap defaultCover = MediaUtil.getThumbnail(getActivity(), 0, mVideoPath);
            mCover.setImageBitmap(defaultCover);
            String filename = ((DubbingUploadActivity)getActivity()).getDefaultCoverFilename();
            if (!TextUtils.isEmpty(filename)) {
                MediaUtil.writeBitmapToLocal(defaultCover, filename);
            }
        }
    }

    private void initView(View v) {
        mCover = (ImageView) v.findViewById(R.id.upload_dubbing_change_cover_btn_image_view);

        ((EditText)v.findViewById(R.id.upload_dubbing_title_edit_text)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no - op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // no - op
            }

            @Override
            public void afterTextChanged(Editable s) {
                mPendingPostTitle = s.toString();
            }
        });

        ((EditText)v.findViewById(R.id.upload_dubbing_content_edit_text)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no - op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // no - op
            }

            @Override
            public void afterTextChanged(Editable s) {
                mPendingPostContent = s.toString();
            }
        });

        v.findViewById(R.id.upload_dubbing_confirm_btn).setOnClickListener(this);
        v.findViewById(R.id.back).setOnClickListener(this);
        v.findViewById(R.id.upload_dubbing_change_cover_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upload_dubbing_confirm_btn:
                // todo upload
                ((DubbingUploadActivity) getActivity()).launchUploadingPage();
                break;
            case R.id.back:
                getActivity().onBackPressed();
                break;
            case R.id.upload_dubbing_change_cover_btn:
                selectCover();
                break;
            case R.id.select_from_camera:
                ((DubbingUploadActivity)getActivity()).selectCoverFromCamera();
                mAlertDialog.dismiss();
                break;
            case R.id.select_from_gallery:
                ((DubbingUploadActivity)getActivity()).selectCoverFromGallery();
                mAlertDialog.dismiss();
                break;
            case R.id.select_from_video:
                ((DubbingUploadActivity)getActivity()).selectCoverFromVideo();
                mAlertDialog.dismiss();
                break;
        }
    }

    private void selectCover() {
        final View dialog =  LayoutInflater.from(getActivity()).inflate(R.layout.item_cover_selector, null, false);
        dialog.findViewById(R.id.select_from_camera).setOnClickListener(this);
        dialog.findViewById(R.id.select_from_gallery).setOnClickListener(this);
        dialog.findViewById(R.id.select_from_video).setOnClickListener(this);
        toggleCoverSelector(dialog);
    }

    private void toggleCoverSelector(View contentView) {
        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(getActivity())
                    .setView(contentView)
                    .create();
        }
        mAlertDialog.show();
    }

    public void setCoverImageByUri(Uri uri) {
        mCover.setImageURI(uri);
    }

    public void setCoverImageByBitmap(Bitmap bitmap) {
        mCover.setImageBitmap(bitmap);
    }

}
