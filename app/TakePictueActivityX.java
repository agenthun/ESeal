package com.agenthun.eseal.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.ViewTreeObserver;

import com.agenthun.eseal.R;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TakePictueActivityX extends AppCompatActivity {

    private static final String TAG = "TakePictueActivity";
    private static final String EXTRA_START_LOCATION = "location";

    private boolean pendingIntro = false;
    private ImageReader mImageReader;
    private File mFile;

    @Bind(R.id.texture)
    AutoFitTextureView mTextureView;
    @Bind(R.id.control)
    View controlPanel;

    public static void start(Activity activity, int[] startLocation, ActivityOptionsCompat options) {
        Intent starter = new Intent(activity, TakePictueActivityX.class);
        starter.putExtra(EXTRA_START_LOCATION, startLocation);
        ActivityCompat.startActivity(activity, starter, options.toBundle());
    }

    public static void start(Context context, int[] startLocation) {
        Intent starter = new Intent(context, TakePictueActivityX.class);
        starter.putExtra(EXTRA_START_LOCATION, startLocation);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_pictue);
        ButterKnife.bind(this);

        controlPanel.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                controlPanel.getViewTreeObserver().removeOnPreDrawListener(this);
                pendingIntro = true;
                controlPanel.setTranslationY(controlPanel.getHeight());
                return true;
            }
        });

        controlPanel.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (pendingIntro) {
                    startIntroAnimation();
                    pendingIntro = false;
                }
            }
        }, 100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    private void startIntroAnimation() {
        controlPanel.animate()
                .translationY(0)
                .setStartDelay(300)
                .setDuration(400)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .start();
    }

    @OnClick(R.id.takePictureBtn)
    public void onTakePictureBtnClick() {
        Log.d(TAG, "onTakePictureBtnClick() returned: ");
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera(i, i1);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            configureTransform(i, i1);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
        }

    };

    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }

        setUpCameraOutputs(width, height);
        configureTransform(width, height);
    }

    private void closeCamera() {

    }

    private void configureTransform(int width, int height) {

    }

    private void requestCameraPermission() {

//        if (FragmentCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
//            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
//        } else {
//            FragmentCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
//                    REQUEST_CAMERA_PERMISSION);
//        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] idList = manager.getCameraIdList();
            for (String cameraId :
                    idList) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());

                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, /*maxImages*/2);
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }
}
