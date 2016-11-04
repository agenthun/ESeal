package com.agenthun.eseal.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.agenthun.eseal.R;
import com.commonsware.cwac.camera.CameraHostProvider;
import com.commonsware.cwac.camera.CameraView;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TakePictueActivity extends AppCompatActivity implements CameraHostProvider {

    private static final String TAG = "TakePictueActivity";
    private static final String EXTRA_START_LOCATION = "location";
    public static final String PICTURE_URI = "pictureUri";

    private static final int STATE_TAKE_PHOTO = 0;
    private static final int STATE_SETUP_PHOTO = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";

    private boolean pendingIntro = false;
    private File mFile;
    private int currentState;

    @Bind(R.id.cameraView)
    CameraView cameraView;
    @Bind(R.id.picturePreview)
    ImageView picturePreview;
    @Bind(R.id.control)
    ViewSwitcher controlPanel;
    @Bind(R.id.takePictureBtn)
    AppCompatButton takePictureBtn;

    public static void start(Activity activity, int[] startLocation, ActivityOptionsCompat options) {
        Intent starter = new Intent(activity, TakePictueActivity.class);
        starter.putExtra(EXTRA_START_LOCATION, startLocation);
        ActivityCompat.startActivity(activity, starter, options.toBundle());
    }

    public static void start(Context context, int[] startLocation) {
        Intent starter = new Intent(context, TakePictueActivity.class);
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

        requestCameraPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraView != null) {
            cameraView = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (currentState == STATE_SETUP_PHOTO) {
            takePictureBtn.setEnabled(true);
            controlPanel.showNext();
            updateState(STATE_TAKE_PHOTO);
        } else {
            super.onBackPressed();
            finish();
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.text_hint_request_take_picture_permission))
                        .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
//        Log.d(TAG, "onTakePictureBtnClick() returned: ");
        takePictureBtn.setEnabled(false);
        cameraView.takePicture(true, true);
    }

    @OnClick(R.id.acceptBtn)
    public void onAcceptBtnClick() {
//        Log.d(TAG, "onAcceptBtnClick() returned: ");
        Intent intent = new Intent(PICTURE_URI);
        intent.putExtra(PICTURE_URI, Uri.fromFile(mFile).toString());
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
        finish();
    }

    @OnClick(R.id.declineBtn)
    public void onDeclineBtnClick() {
//        Log.d(TAG, "onDeclineBtnClick() returned: ");
        onBackPressed();
    }

    @Override
    public CameraHost getCameraHost() {
        return new CameraHost(this);
    }

    class CameraHost extends SimpleCameraHost {
        private Camera.Size previewSize;

        public CameraHost(Context _ctxt) {
            super(_ctxt);
        }

        @Override
        public boolean useFullBleedPreview() {
            return true;
        }

        @Override
        public Camera.Size getPictureSize(PictureTransaction xact, Camera.Parameters parameters) {
            return previewSize;
        }

        @Override
        public Camera.Parameters adjustPreviewParameters(Camera.Parameters parameters) {
            Camera.Parameters parameters1 = super.adjustPreviewParameters(parameters);
            previewSize = parameters1.getPreviewSize();
            return parameters1;
        }

        @Override
        public void saveImage(PictureTransaction xact, final Bitmap bitmap) {
            super.saveImage(xact, bitmap);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showTakenPicture(bitmap);
                }
            });
        }

        @Override
        public void saveImage(PictureTransaction xact, byte[] image) {
            super.saveImage(xact, image);
            mFile = getPhotoPath();
        }

    }

    private void showTakenPicture(Bitmap bitmap) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(R.string.device_reply_read_setting_title)
//                .setMessage()
//                .setPositiveButton(R.string.text_ok, null).show();

        picturePreview.setImageBitmap(bitmap);
        controlPanel.showNext();
        updateState(STATE_SETUP_PHOTO);
    }

    private void updateState(int state) {
        currentState = state;
        if (currentState == STATE_TAKE_PHOTO) {
            controlPanel.setInAnimation(this, R.anim.slide_in_from_right);
            controlPanel.setOutAnimation(this, R.anim.slide_out_to_left);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    picturePreview.setVisibility(View.GONE);
                }
            }, 400);
        } else if (currentState == STATE_SETUP_PHOTO) {
            controlPanel.setInAnimation(this, R.anim.slide_in_from_left);
            controlPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            picturePreview.setVisibility(View.VISIBLE);
        }
    }

    public static class ErrorDialog extends DialogFragment {
        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.finish();
                        }
                    })
                    .create();
        }
    }


    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.text_hint_request_take_picture_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = getActivity();
                                    if (activity != null) {
                                        activity.finish();
                                    }
                                }
                            })
                    .create();
        }
    }

}
