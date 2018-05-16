package org.tensorflow.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

public class PermissionsUtil {

    private static PermissionsUtil instance = null;

    private PermissionsUtil() {
    }

    public static PermissionsUtil getInstance() {
        if (instance == null) {
            instance = new PermissionsUtil();
        }
        return instance;
    }

    public void requestCameraPermission(Activity activity, int requestCode) {
        String manifestPermission = Manifest.permission.CAMERA;
        if (checkSelfPermission(activity, manifestPermission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{manifestPermission}, requestCode);
        }
    }


    public void requestStoragePermission(Activity activity, int requestCode) {
        String manifestPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (checkSelfPermission(activity, manifestPermission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{manifestPermission}, requestCode);
        }
    }

    public boolean hasCameraPermission(Context mContext) {
        return checkHasPermission(mContext, Manifest.permission.CAMERA);
    }

    private boolean checkHasPermission(Context mContext, String permission) {
        return checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED;
    }

}
