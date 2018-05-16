package org.tensorflow.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PictureCallback {

    private Camera.PreviewCallback mPreviewCallback;
    private Lock cameraMutex = new ReentrantLock();
    private Lock initTerminateMutex = new ReentrantLock();
    private boolean cameraIsNotOpen = true;
    private SurfaceHolder mHolder;
    private static Camera camera;
    private Context mContext;

    public static Camera getCamera() {
        return camera;
    }

    public boolean isCameraConnected() {
        return !cameraIsNotOpen;
    }

    public CameraPreview(Context context, Camera.PreviewCallback previewCallback) {
        super(context);
        mPreviewCallback = previewCallback;
        mContext = context;

        Log.e("Tag", this.getResources().getConfiguration().orientation + "," + Configuration.ORIENTATION_LANDSCAPE );
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        init();
        boolean hasCameraPermission = PermissionsUtil.getInstance().hasCameraPermission(mContext);
        if(!hasCameraPermission)
        {
            PermissionsUtil.getInstance().requestCameraPermission((Activity) context,81);
        }
        if(camera != null){
            Camera.Parameters parameters = camera.getParameters();
            parameters.setRotation(90);
            camera.setParameters(parameters);
            camera.setDisplayOrientation(90);
            camera.setPreviewCallback(mPreviewCallback);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        init();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (mHolder.getSurface() != null) {
            init();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        terminate();
    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
    }

    private void terminate() {
        initTerminateMutex.lock();
        try {
            if (camera != null) {
                camera.setPreviewDisplay(null);
                camera.setOneShotPreviewCallback(null);
                camera.startPreview();
                closeCamera();
            }
        } catch (IOException e) {
        }
        initTerminateMutex.unlock();
    }

    private void init() {
        try {
               camera.setPreviewDisplay(mHolder);
               camera.startPreview();
            } catch (IOException e) {
        }
    }

    private void openCamera() {
        cameraMutex.lock();
        if (cameraIsNotOpen) {
            for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
                try {
                    camera = Camera.open(i);
                    cameraIsNotOpen = false;
                    if (camera != null) {
                        cameraMutex.unlock();
                    }
                } catch (RuntimeException e) {
                }
            }
        }
        cameraMutex.unlock();
    }

    private void closeCamera() {
        cameraMutex.lock();
        if (!cameraIsNotOpen) {
            if (camera != null) {
                try {
                    cameraIsNotOpen = true;
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                } catch (RuntimeException e) {
                }
            }
        }
        cameraMutex.unlock();
    }

}