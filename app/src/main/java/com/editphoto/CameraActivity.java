package com.editphoto;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.View;

import com.editphoto.pack1.pack2.R;

import java.io.File;
import java.io.UnsupportedEncodingException;

import static com.editphoto.CameraHelper.cameraAvailable;
import static com.editphoto.CameraHelper.getCameraInstance;
import static com.editphoto.MediaHelper.getOutputMediaFile;
import static com.editphoto.MediaHelper.saveToFile;

public class CameraActivity extends Activity implements PictureCallback {

    protected static final String EXTRA_IMAGE_PATH = "response";

    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        setResult(RESULT_CANCELED);
        // Camera may be in use by another activity or the system or not available at all
        camera = getCameraInstance();
        if (cameraAvailable(camera)) {
            initCameraPreview();
        } else {
            finish();
        }
    }

    private void initCameraPreview() {
        CameraPreview cameraPreview = (CameraPreview) findViewById(R.id.camera_preview);
        cameraPreview.init(camera);
    }

    @FromXML
    public void onCaptureClick(View button) {

        camera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        MainActivity.Log.d("Picture taken");
        try {
            String s = new String(data, "UTF-8");


        String path = savePictureToFileSystem(data);
        setResult(path);
        finish();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private static String savePictureToFileSystem(byte[] data) {
        File file = getOutputMediaFile();
       saveToFile(data, file);
        return file.getAbsolutePath();
    }

    private void setResult(String path) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_IMAGE_PATH, path);
        setResult(RESULT_OK, intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
