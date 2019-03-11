package com.editphoto;

import android.hardware.Camera;
import android.util.Log;

public class CameraHelper {

    public static boolean cameraAvailable(Camera camera) {
        return camera != null;
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {

            Log.d("","getCamera failed");
        }
        return c;
    }

}
