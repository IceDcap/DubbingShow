package com.example.missevan.mydubbing.camera;

import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by missevan on 2017/4/26.
 */

public class CameraView2 extends TextureView implements CameraOperation {
    private static final String LOG_TAG = CameraView2.class.getSimpleName();
    public static final String TAG = "CameraView";
    private static MediaRecorder sMediaRecorder = new MediaRecorder();

    private Camera mCamera;
    private int mCameraDisplayOrientation;
    private int mCameraId;
    private ICameraStatusListener mCameraStatusListener;
    private Context mContext;
    private boolean mIsFaceCamera;
    private int mOrientation;
    private Camera.Parameters mParameters;
    private Camera.Size mPreviewSize;
    private Camera.Size mRecordedSize;
    private TextureView.SurfaceTextureListener mSTCallBack;
    private TextureView mSurfaceTexture;

    public CameraView2(Context context) {
        super(context);
    }

    public CameraView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    @Override
    public int getVideoAngle() {
        return 0;
    }

    @Override
    public int startPreview(Camera.Size size) {
        return 0;
    }

    @Override
    public boolean startRecord(Camera.Size size) {
        return false;
    }

    @Override
    public int stopPreview() {
        return 0;
    }

    @Override
    public void stopRecord() {

    }


    public void setContext(Context context) {
        mContext = context;
    }

    public void setCameraStatusListener(ICameraStatusListener listener) {
        mCameraStatusListener = listener;
    }

    public boolean isFaceCamera() {
        return mIsFaceCamera;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
    }

    public int getCameraDisplayOrientation() {
        return mCameraDisplayOrientation;
    }

    public void setCameraDisplayOrientation(int cameraDisplayOrientation) {
        mCameraDisplayOrientation = cameraDisplayOrientation;
    }

    public int getCameraId() {
        return mCameraId;
    }

    public void setCameraId(int cameraId) {
        mCameraId = cameraId;
    }

    public ICameraStatusListener getCameraStatusListener() {
        return mCameraStatusListener;
    }


    public void setFaceCamera(boolean faceCamera) {
        mIsFaceCamera = faceCamera;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public Camera.Parameters getParameters() {
        return mParameters;
    }

    public void setParameters(Camera.Parameters parameters) {
        mParameters = parameters;
    }

    public Camera.Size getPreviewSize() {
        return mPreviewSize;
    }

    public void setPreviewSize(Camera.Size previewSize) {
        mPreviewSize = previewSize;
    }

    public Camera.Size getRecordedSize() {
        return mRecordedSize;
    }

    public void setRecordedSize(Camera.Size recordedSize) {
        mRecordedSize = recordedSize;
    }

    public SurfaceTextureListener getSTCallBack() {
        return mSTCallBack;
    }

    public void setSTCallBack(SurfaceTextureListener STCallBack) {
        mSTCallBack = STCallBack;
    }


    public void setSurfaceTexture(TextureView surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
    }

    enum FlashMode {
        AUTO,
        OFF,
        ON,
        TORCH
    }

    public interface ICameraStatusListener {
        void onCameraStartPreview();

        void onCameraStopPreview();
    }
}
