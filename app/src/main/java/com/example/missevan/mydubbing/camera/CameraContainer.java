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
//        int i = 0;
//        if ("H60-L01".equals(Build.MODEL)) {
//            i = 1;
//        }
//        Object localObject4 = new Camera.CameraInfo();
//        Object localObject3 = null;
//        float f2 = 1000.0F;
//        if ((mBestVideoSize4SingleLiving != null) && (paramBoolean)) {
//            return mBestVideoSize4SingleLiving;
//        }
//        if ((mBestVideoSize4MultiLiving != null) && (!paramBoolean)) {
//            return mBestVideoSize4MultiLiving;
//        }
//        if (Camera.getNumberOfCameras() == 0) {
//            return null;
//        }
//        int j = 0;
//        for (; ; ) {
//            Object localObject1 = localObject3;
//            if (j < Camera.getNumberOfCameras()) {
//                Camera.getCameraInfo(j, (Camera.CameraInfo) localObject4);
//                if (((Camera.CameraInfo) localObject4).facing != 1) {
//                }
//            } else {
//                try {
//                    localObject3 = Camera.open(j);
//                    localObject1 = ((Camera) localObject3).getParameters();
//                    if (i != 0) {
//                        localObject3.getClass();
//                        mBestVideoSize4SingleLiving = new Camera.Size((Camera) localObject3, 640, 480);
//                        localObject3.getClass();
//                        mBestVideoSize4MultiLiving = new Camera.Size((Camera) localObject3, 640, 480);
//                        ((Camera) localObject3).release();
//                        if (paramBoolean) {
//                            return mBestVideoSize4SingleLiving;
//                        }
//                        return mBestVideoSize4MultiLiving;
//                    }
//                    ((Camera) localObject3).release();
//                    localObject3 = localObject1;
//                    if (localObject1 == null) {
//                    }
//                    j += 1;
//                } catch (Exception localException1) {
//                    try {
//                        localObject1 = Camera.open(0);
//                        localObject3 = ((Camera) localObject1).getParameters();
//                        ((Camera) localObject1).release();
//                        if (localObject3 != null) {
//                            break;
//                        }
//                        return null;
//                    } catch (Exception localException2) {
//                        return null;
//                    }
//                    localException1 = localException1;
//                    return null;
//                }
//            }
//        }
//        localObject4 = ((Camera.Parameters) localObject3).getSupportedVideoSizes();
//        if ((localObject4 == null) && (((Camera.Parameters) localObject3).getPreferredPreviewSizeForVideo() != null)) {
//            mBestVideoSize4SingleLiving = ((Camera.Parameters) localObject3).getPreferredPreviewSizeForVideo();
//            mBestVideoSize4MultiLiving = ((Camera.Parameters) localObject3).getPreferredPreviewSizeForVideo();
//        }
//        Object localObject2 = localObject4;
//        if (localObject4 == null) {
//            localObject2 = localObject4;
//            if (((Camera.Parameters) localObject3).getPreferredPreviewSizeForVideo() == null) {
//                List localList = ((Camera.Parameters) localObject3).getSupportedPreviewSizes();
//                localObject2 = localObject4;
//                if (localList != null) {
//                    localObject2 = localObject4;
//                    if (localList.size() > 0) {
//                        localObject2 = ((Camera.Parameters) localObject3).getSupportedPreviewSizes();
//                    }
//                }
//            }
//        }
//        float f1 = f2;
//        if (mBestVideoSize4SingleLiving == null) {
//            f1 = f2;
//            if (localObject2 != null) {
//                f1 = f2;
//                if (((List) localObject2).size() > 0) {
//                    f2 = 1000.0F;
//                    localObject3 = ((List) localObject2).iterator();
//                    for (; ; ) {
//                        f1 = f2;
//                        if (!((Iterator) localObject3).hasNext()) {
//                            break;
//                        }
//                        localObject4 = (Camera.Size) ((Iterator) localObject3).next();
//                        if ((((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height <= 1292) && (((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height > 352)) {
//                            if (mBestVideoSize4SingleLiving == null) {
//                                mBestVideoSize4SingleLiving = (Camera.Size) localObject4;
//                            }
//                            f1 = Math.abs(((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height - 720 - 540 - 32);
//                            if (f1 < f2) {
//                                f2 = f1;
//                                mBestVideoSize4SingleLiving = (Camera.Size) localObject4;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        f2 = f1;
//        if (mBestVideoSize4SingleLiving == null) {
//            f2 = f1;
//            if (localObject2 != null) {
//                f2 = f1;
//                if (((List) localObject2).size() > 0) {
//                    f1 = 1000.0F;
//                    localObject3 = ((List) localObject2).iterator();
//                    for (; ; ) {
//                        f2 = f1;
//                        if (!((Iterator) localObject3).hasNext()) {
//                            break;
//                        }
//                        localObject4 = (Camera.Size) ((Iterator) localObject3).next();
//                        if ((((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height <= 1152) && (((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height > 352)) {
//                            if (mBestVideoSize4SingleLiving == null) {
//                                mBestVideoSize4SingleLiving = (Camera.Size) localObject4;
//                            }
//                            f2 = Math.abs(((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height - 640 - 480 - 32);
//                            if (f2 < f1) {
//                                f1 = f2;
//                                mBestVideoSize4SingleLiving = (Camera.Size) localObject4;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        f1 = f2;
//        if (mBestVideoSize4SingleLiving == null) {
//            f1 = f2;
//            if (localObject2 != null) {
//                f1 = f2;
//                if (((List) localObject2).size() > 0) {
//                    f2 = 1000.0F;
//                    localObject3 = ((List) localObject2).iterator();
//                    for (; ; ) {
//                        f1 = f2;
//                        if (!((Iterator) localObject3).hasNext()) {
//                            break;
//                        }
//                        localObject4 = (Camera.Size) ((Iterator) localObject3).next();
//                        if ((((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height <= 832) && (((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height > 352)) {
//                            if (mBestVideoSize4SingleLiving == null) {
//                                mBestVideoSize4SingleLiving = (Camera.Size) localObject4;
//                            }
//                            f1 = Math.abs(((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height - 480 - 320 - 32);
//                            if (f1 < f2) {
//                                f2 = f1;
//                                mBestVideoSize4SingleLiving = (Camera.Size) localObject4;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        f2 = f1;
//        if (mBestVideoSize4SingleLiving == null) {
//            f2 = f1;
//            if (localObject2 != null) {
//                f2 = f1;
//                if (((List) localObject2).size() > 0) {
//                    f1 = 1000.0F;
//                    localObject3 = ((List) localObject2).iterator();
//                    for (; ; ) {
//                        f2 = f1;
//                        if (!((Iterator) localObject3).hasNext()) {
//                            break;
//                        }
//                        localObject4 = (Camera.Size) ((Iterator) localObject3).next();
//                        if ((((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height <= 672) && (((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height > 352)) {
//                            if (mBestVideoSize4SingleLiving == null) {
//                                mBestVideoSize4SingleLiving = (Camera.Size) localObject4;
//                            }
//                            f2 = Math.abs(((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height - 352 - 288 - 32);
//                            if (f2 < f1) {
//                                f1 = f2;
//                                mBestVideoSize4SingleLiving = (Camera.Size) localObject4;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        float f3 = f2;
//        if (mBestVideoSize4SingleLiving == null) {
//            f3 = f2;
//            if (localObject2 != null) {
//                f3 = f2;
//                if (((List) localObject2).size() > 0) {
//                    f1 = 1000.0F;
//                    localObject3 = ((List) localObject2).iterator();
//                    for (; ; ) {
//                        f3 = f1;
//                        if (!((Iterator) localObject3).hasNext()) {
//                            break;
//                        }
//                        localObject4 = (Camera.Size) ((Iterator) localObject3).next();
//                        if ((((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height <= 592) && (((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height > 352)) {
//                            if (mBestVideoSize4SingleLiving == null) {
//                                mBestVideoSize4SingleLiving = (Camera.Size) localObject4;
//                            }
//                            f2 = Math.abs(((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height - 320 - 240 - 32);
//                            if (f2 < f1) {
//                                f1 = f2;
//                                mBestVideoSize4SingleLiving = (Camera.Size) localObject4;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        if ((mBestVideoSize4SingleLiving == null) && (localObject2 != null) && (((List) localObject2).size() > 0)) {
//            mBestVideoSize4SingleLiving = (Camera.Size) ((List) localObject2).get(((List) localObject2).size() / 2);
//        }
//        if ((localObject2 != null) && (((List) localObject2).size() > 0)) {
//            localObject3 = ((List) localObject2).iterator();
//            while (((Iterator) localObject3).hasNext()) {
//                localObject4 = (Camera.Size) ((Iterator) localObject3).next();
//                f1 = Math.abs(((Camera.Size) localObject4).height / ((Camera.Size) localObject4).width - 1.0F);
//                if ((((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height < 1440) && (((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height > 640)) {
//                    if (f1 < f3) {
//                        f3 = f1;
//                        mBestVideoSize4MultiLiving = (Camera.Size) localObject4;
//                    } else if ((f1 == f3) && (((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height < mBestVideoSize4MultiLiving.width + mBestVideoSize4MultiLiving.height) && (((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height >= 1120)) {
//                        mBestVideoSize4MultiLiving = (Camera.Size) localObject4;
//                    }
//                }
//            }
//            if ((mBestVideoSize4MultiLiving != null) && (mBestVideoSize4MultiLiving.height / mBestVideoSize4MultiLiving.width > 1.5D)) {
//                mBestVideoSize4MultiLiving = null;
//                localObject3 = ((List) localObject2).iterator();
//                while (((Iterator) localObject3).hasNext()) {
//                    localObject4 = (Camera.Size) ((Iterator) localObject3).next();
//                    f1 = Math.abs(((Camera.Size) localObject4).height / ((Camera.Size) localObject4).width - 1.0F);
//                    if ((((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height <= 1500) && (((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height > 320)) {
//                        if (f1 < f3) {
//                            f3 = f1;
//                            mBestVideoSize4MultiLiving = (Camera.Size) localObject4;
//                        } else if ((f1 == f3) && (((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height < mBestVideoSize4MultiLiving.width + mBestVideoSize4MultiLiving.height) && (((Camera.Size) localObject4).width + ((Camera.Size) localObject4).height >= 1120)) {
//                            mBestVideoSize4MultiLiving = (Camera.Size) localObject4;
//                        }
//                    }
//                }
//            }
//        }
//        if ((mBestVideoSize4MultiLiving == null) && (localObject2 != null) && (((List) localObject2).size() > 0)) {
//            mBestVideoSize4MultiLiving = (Camera.Size) ((List) localObject2).get(((List) localObject2).size() / 2);
//        }
//        if (paramBoolean) {
//            return mBestVideoSize4SingleLiving;
//        }
//        return mBestVideoSize4MultiLiving;
        return null;
    }

    public static boolean HasCameraPermission() {
//        boolean bool2 = true;
//        Camera.CameraInfo localCameraInfo = new Camera.CameraInfo();
//        int i = 0;
//        boolean bool1;
//        for (; ; ) {
//            bool1 = bool2;
//            if (i < Camera.getNumberOfCameras()) {
//                Camera.getCameraInfo(i, localCameraInfo);
//                if (localCameraInfo.facing == 1) {
//                }
//                try {
//                    Camera.open(i).release();
//                    i += 1;
//                } catch (Exception localException) {
//                    bool1 = false;
//                }
//            }
//        }
//        return bool1;
        return false;
    }
}
