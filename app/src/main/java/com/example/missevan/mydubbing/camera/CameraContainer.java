package com.example.missevan.mydubbing.camera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.missevan.mydubbing.R;

import java.text.SimpleDateFormat;

/**
 * Created by missevan on 2017/4/26.
 */

public class CameraContainer extends RelativeLayout implements CameraOperation {
    public static final String TAG = "CameraContainer";
    private static Camera.Size mBestVideoSize4MultiLiving = null;
    private static Camera.Size mBestVideoSize4SingleLiving = null;
    private CameraView2 mCameraView;
    private long mRecordStartTime;
    private String mSavePath;
    private SimpleDateFormat mTimeFormat;

    public CameraContainer(Context context) {
        super(context);
        initView(context);
    }

    public CameraContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CameraContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @Override
    public int getVideoAngle() {
        if (mCameraView != null) {
            return mCameraView.getVideoAngle();
        }
        return 270;
    }

    @Override
    public int startPreview(Camera.Size size) {
        return mCameraView.startPreview(size);
    }

    @Override
    public boolean startRecord(Camera.Size size) {
        mRecordStartTime = SystemClock.uptimeMillis();
        if (mCameraView.startRecord(size)) {
            Log.d("CameraContainer", "startRecord ok");
            return true;
        }
        Log.d("CameraContainer", "startRecord fail");
        return false;
    }

    @Override
    public int stopPreview() {
        return mCameraView.stopPreview();
    }

    @Override
    public void stopRecord() {
        mCameraView.stopRecord();
    }


    private void initView(Context context) {
        inflate(context, R.layout.cameracontainer, this);
        mCameraView = ((CameraView2) findViewById(R.id.cameraView2));
    }

    public Camera.Size getPreviewSize() {
        if (mCameraView != null) {
            return mCameraView.getPreviewSize();
        }
        return null;
    }

    public Camera.Size getRecordedSize() {
        if (mCameraView != null) {
            return mCameraView.getRecordedSize();
        }
        return null;
    }


    public boolean isFaceCamera() {
        if (mCameraView != null) {
            return mCameraView.isFaceCamera();
        }
        return true;
    }

    public void setCameraStatusListener(CameraView2.ICameraStatusListener listener) {
        if (mCameraView != null) {
            mCameraView.setCameraStatusListener(listener);
        }
    }

    public void setContext(Context context) {
        if (mCameraView != null) {
            mCameraView.setContext(context);
        }
    }

    public void setZOrderMediaOverlay(boolean overlay) {
        if (overlay) {
            bringToFront();
            return;
        }
        ViewGroup localViewGroup = (ViewGroup) getParent();
        localViewGroup.removeView(this);
        localViewGroup.addView(this, 0);
    }

    public static Camera.Size GetBestVideoSize(boolean paramBoolean) {
        return null;
    }

    public static boolean HasCameraPermission() {
        return false;
    }
}
