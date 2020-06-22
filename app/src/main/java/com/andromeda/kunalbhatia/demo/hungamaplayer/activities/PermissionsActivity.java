package com.andromeda.kunalbhatia.demo.hungamaplayer.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.andromeda.kunalbhatia.demo.hungamaplayer.PermissionsChecker;
import com.andromeda.kunalbhatia.demo.hungamaplayer.R;


public class PermissionsActivity extends AppCompatActivity {
    public static final int PERMISSIONS_GRANTED = 0;
    public static final int PERMISSIONS_DENIED = 1;

    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 10;
    private static final String EXTRA_PERMISSIONS = "com.permissions.EXTRA_PERMISSIONS";
    private static final String PACKAGE_URL_SCHEME = "package:";

    private PermissionsChecker mChecker;
    private boolean requiresCheck;

    public static void startActivityForResult(Activity activity, int requestCode, String... permissions) {
        Intent intent = new Intent(activity, PermissionsActivity.class);
        intent.putExtra(EXTRA_PERMISSIONS, permissions);
        ActivityCompat.startActivityForResult(activity, intent, requestCode, null);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (requiresCheck) {
            String[] permissions = getPermissions();
            if (mChecker.lacksPermissions(permissions)) {
                if(permissions.length > 0 && permissions[0].equals(Manifest.permission.CAMERA)){
                    requestPermissions(CAMERA_PERMISSION_REQUEST_CODE, permissions);
                }else{
                    requestPermissions(PERMISSION_REQUEST_CODE, permissions);
                }
            } else {
                allPermissionsGranted();
            }
        } else {
            requiresCheck = true;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("This Activity needs to be launched using the static startActivityForResult() method.");
        }
        mChecker = new PermissionsChecker(this);
        requiresCheck = true;
    }


    private String[] getPermissions() {
        return getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
    }

    private void allPermissionsGranted() {
        setResult(PERMISSIONS_GRANTED);
        finish();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions
            , @NonNull int[] grantResults) {
        if ((requestCode == PERMISSION_REQUEST_CODE || requestCode == CAMERA_PERMISSION_REQUEST_CODE ) &&
                hasAllPermissionsGranted(grantResults)) {
            requiresCheck = true;
            allPermissionsGranted();
        } else {
            requiresCheck = false;
            setResult(PERMISSIONS_DENIED);
            finish();
        }
    }

    private boolean hasAllPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void showMissingPermissionDialog() {
        AlertDialog.Builder alertDialogBuilder;
        alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setTitle(getString(R.string.tips_permission_request_title))
                .setMessage(getString(R.string.tips_permission_request_content))
                .setPositiveButton(R.string.go_and_grant_permission, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startAppSettings();
                    }
                })
                .setNegativeButton(R.string.deny_and_quit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setResult(PERMISSIONS_DENIED);
                        finish();
                    }
                });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();

    }

    private void startAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivity(intent);
    }

    private void requestPermissions(Integer code, String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, code);
    }


}
