package com.icedcap.dubbing.camera;

import android.hardware.Camera;

/**
 * Created by dsq on 2017/4/26.
 */

public interface CameraOperation {

    public abstract int getVideoAngle();

    public abstract int startPreview(Camera.Size size);

    public abstract boolean startRecord(Camera.Size size);

    public abstract int stopPreview();

    public abstract void stopRecord();
}
