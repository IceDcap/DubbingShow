package com.example.missevan.mydubbing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.widget.FrameLayout;

import com.example.missevan.mydubbing.fragment.DubbingUploadEditFragment;
import com.example.missevan.mydubbing.fragment.DubbingUploadSuccessFragment;
import com.example.missevan.mydubbing.fragment.DubbingUploadingFragment;
import com.example.missevan.mydubbing.utils.MediaUtil;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


/**
 * Created by dsq on 2017/5/10.
 * <p>
 * Dubbing upload page
 */
public class DubbingUploadActivity extends FragmentActivity {
    private static final String LOG_TAG = "DubbingUploadActivity";
    public static final String EXTRA_VIDEO_PATH_KEY = "extra-video-path-key";
    public static final String EXTRA_AUDIO_PATH_KEY = "extra-audio-path-key";
    private static final int REQUEST_CODE_TAKE_PHOTO = 0;
    private static final int REQUEST_CODE_FROM_GALLERY = 1;
    private static final int REQUEST_CODE_FROM_VIDEO = 2;

    private FrameLayout mRootView;
    private FragmentManager mFragmentManager;

    private String mDefaultCoverFilename;
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
        initData();
        mFragmentManager = getSupportFragmentManager();
        initFirstFragment();
    }

    private void initData() {
        mDefaultCoverFilename = getExternalCacheDir() + "/default_cover.png";
        mVideoPath = getIntent().getStringExtra(EXTRA_VIDEO_PATH_KEY);
        mAudioPath = getIntent().getStringExtra(EXTRA_AUDIO_PATH_KEY);
    }

    public String getDefaultCoverFilename() {
        return mDefaultCoverFilename;
    }

    private void initFirstFragment() {
        if (TextUtils.isEmpty(mAudioPath) || TextUtils.isEmpty(mVideoPath)) return;
        mFragmentManager.beginTransaction()
                .add(R.id.upload_dubbing_root_view,
                        DubbingUploadEditFragment.newInstance(mVideoPath, mAudioPath))
                .addToBackStack(LOG_TAG)
                .commit();
    }

    public void launchUploadingPage() {
        mFragmentManager.beginTransaction()
                .add(R.id.upload_dubbing_root_view,
                        DubbingUploadingFragment.newInstance())
                .addToBackStack(LOG_TAG)
                .commit();
    }

    public void launchUploadSuccessPage() {
        mFragmentManager.beginTransaction()
                .add(R.id.upload_dubbing_root_view,
                        DubbingUploadSuccessFragment.newInstance())
                .addToBackStack(LOG_TAG)
                .commit();
    }


    public void onFragmentBackPress() {
        mFragmentManager.popBackStack();
    }

    public void selectCoverFromCamera() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, REQUEST_CODE_TAKE_PHOTO);
    }

    public void selectCoverFromGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_CODE_FROM_GALLERY);
    }

    public void selectCoverFromVideo() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case REQUEST_CODE_TAKE_PHOTO:
                final Uri uri = data.getData();
                final Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (uri != null) {
                    launchCropPage(uri);
                } else if (bitmap != null) {
                    String filename = getExternalCacheDir() + "/cover.png";
                    Uri fileUri = Uri.parse("file://" + filename);
                    MediaUtil.writeBitmapToLocal(bitmap, filename);
                    launchCropPage(fileUri);
                }
                break;
            case REQUEST_CODE_FROM_GALLERY:
                final Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    launchCropPage(selectedImage);
                }
                break;
            case REQUEST_CODE_FROM_VIDEO:

                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri resultUri = result.getUri();
                if (resultUri != null) {
                    updateImageView(resultUri);
                }
                break;
        }


    }

    private void launchCropPage(Uri uri) {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setAspectRatio(1, 1) //square
                .start(this);
    }

    private void updateImageView(Uri uri) {
        Fragment fragment = mFragmentManager.findFragmentById(R.id.upload_dubbing_root_view);
        if (fragment instanceof DubbingUploadEditFragment) {
            ((DubbingUploadEditFragment) fragment).setCoverImageByUri(uri);

        }
    }

    private void updateImageView(Bitmap bitmap) {
        Fragment fragment = mFragmentManager.findFragmentById(R.id.upload_dubbing_root_view);
        if (fragment instanceof DubbingUploadEditFragment) {
            ((DubbingUploadEditFragment) fragment).setCoverImageByBitmap(bitmap);

        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = mFragmentManager.findFragmentById(R.id.upload_dubbing_root_view);
        if (fragment instanceof DubbingUploadEditFragment) {
            finish();
        } else if (fragment instanceof DubbingUploadingFragment) {
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage("正在上传，确认取消？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onFragmentBackPress();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            dialog.show();
        } else if (fragment instanceof DubbingUploadSuccessFragment) {
            //todo transform to first page of dubbing
            finish();
        } else {
            super.onBackPressed();
        }

    }

}
