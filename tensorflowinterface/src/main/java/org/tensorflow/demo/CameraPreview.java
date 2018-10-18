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

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        boolean b = false;
        try{
           b = init();
       }catch (IOException e){}

        if(b && camera != null){
            Camera.Parameters parameters = camera.getParameters();
            parameters.setRotation(90);
            camera.setParameters(parameters);
            camera.setDisplayOrientation(90);
            camera.setPreviewCallback(mPreviewCallback);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try{
            init();
        }catch (IOException e){}
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (mHolder.getSurface() != null) {
            try{
                init();
            }catch (IOException e){}
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

    private boolean init() throws IOException{
        boolean rValue = false;
        if (PermissionsUtil.getInstance().hasCameraPermission(mContext)) {
                openCamera();
                camera.setPreviewDisplay(mHolder);
                camera.startPreview();
                rValue = true;
        }
        return rValue;
    }

    private void openCamera() {
        cameraMutex.lock();
        if (cameraIsNotOpen) {
            for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
                    camera = Camera.open(i);
                    cameraIsNotOpen = false;
                    if (camera != null) {
                        cameraMutex.unlock();
                        break;
                    }
            }
        }
        cameraMutex.unlock();
    }

    private void closeCamera() {
        cameraMutex.lock();
        if (!cameraIsNotOpen) {
            if (camera != null) {
                cameraIsNotOpen = true;
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        }
        cameraMutex.unlock();
    }

}