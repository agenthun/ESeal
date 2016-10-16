package com.agenthun.eseal.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
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

    private static final int STATE_TAKE_PHOTO = 0;
    private static final int STATE_SETUP_PHOTO = 1;

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
        takePictureBtn.setEnabled(false);
        cameraView.takePicture(true, true);
    }

    @OnClick(R.id.acceptBtn)
    public void onAcceptBtnClick() {
        Log.d(TAG, "onAcceptBtnClick() returned: ");
        Uri.fromFile(mFile);
        finish();
    }

    @OnClick(R.id.declineBtn)
    public void onDeclineBtnClick() {
        Log.d(TAG, "onDeclineBtnClick() returned: ");
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

    public interface PictureCallback {
        public void onPictureAccepted(Uri uri);
    }
}
