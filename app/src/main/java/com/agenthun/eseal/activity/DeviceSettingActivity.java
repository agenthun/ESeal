package com.agenthun.eseal.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.connectivity.nfc.NfcUtility;
import com.agenthun.eseal.model.utils.SettingType;
import com.agenthun.eseal.utils.ApiLevelHelper;
import com.agenthun.eseal.view.CheckableFab;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/9 下午10:55.
 */
public class DeviceSettingActivity extends AppCompatActivity {
    private static final String TAG = "DeviceSettingActivity";
    public static final String RESULT_CONFIGURE = "result_configure";

    @Bind(R.id.addPicture)
    View addPicture;

    @Bind(R.id.picturePreview)
    ImageView picturePreview;

    @Bind(R.id.nfc_id)
    AppCompatEditText nfcId;

    @Bind(R.id.container_number)
    AppCompatEditText containerNumber;

    @Bind(R.id.owner)
    AppCompatEditText owner;

    @Bind(R.id.freight_name)
    AppCompatEditText freightName;

    @Bind(R.id.origin)
    AppCompatEditText origin;

    @Bind(R.id.destination)
    AppCompatEditText destination;

    @Bind(R.id.vessel)
    AppCompatEditText vessel;

    @Bind(R.id.voyage)
    AppCompatEditText voyage;

    @Bind(R.id.frequency)
    AppCompatEditText frequency;

    @Bind(R.id.fab)
    CheckableFab fab;

    private Handler mHandler = new Handler();
    private Runnable mHideFabRunnable;

    private boolean mEdited = false;

    private NfcUtility mNfcUtility;

    private Uri pictureUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_setting);
        ButterKnife.bind(this);

        mNfcUtility = new NfcUtility(tagCallback);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ismEdited()) {
                    Log.d(TAG, "ismEdited onClick() returned: ");
                }
                adjustFab(false);
            }
        });

        containerNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(containerNumber.getText())) {
                    allowSubmit(true);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.hide();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(TakePictueActivity.PICTURE_URI);

        localBroadcastManager.registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @OnClick(R.id.addPicture)
    public void onAddPictureBtnClick() {
        Log.d(TAG, "onAddPictureBtnClick() returned: ");
        performTakePictureWithTransition(addPicture);
    }

    @OnClick(R.id.nfc_id)
    public void onGetNfcIdBtnClick() {
        Log.d(TAG, "onGetNfcIdBtnClick() returned: ");
        enableNfcReaderMode();
    }

    private void performTakePictureWithTransition(View v) {
        Activity activity = this;

        final int[] startLocation = new int[2];
        v.getLocationOnScreen(startLocation);
        startLocation[0] += v.getWidth() / 2;

        if (v == null || ApiLevelHelper.isLowerThan(Build.VERSION_CODES.LOLLIPOP)) {
            TakePictueActivity.start(activity, startLocation);
            return;
        }
        if (ApiLevelHelper.isAtLeast(Build.VERSION_CODES.LOLLIPOP)) {
//            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeBasic();
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeScaleUpAnimation(v,
                    startLocation[0],
                    startLocation[1],
                    0,
                    0);
            TakePictueActivity.start(activity, startLocation, optionsCompat);
        }
    }

    private void enableNfcReaderMode() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            if (nfcAdapter.isEnabled()) {
                nfcAdapter.enableReaderMode(this, mNfcUtility, NfcUtility.NFC_TAG_FLAGS, null);
                Snackbar.make(nfcId, getString(R.string.text_hint_close_to_nfc_tag), Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            } else {
                Snackbar.make(nfcId, getString(R.string.error_nfc_not_open), Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.text_hint_open_nfc), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                                startActivity(intent);
                            }
                        }).show();
            }
        }
    }

    private void disableNfcReaderMode() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }

    private NfcUtility.TagCallback tagCallback = new NfcUtility.TagCallback() {
        @Override
        public void onTagReceived(final String tag) {
            App.setTagId(tag);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DeviceSettingActivity.this);
                    builder.setTitle(R.string.text_hint_nfc_id)
                            .setMessage(tag)
                            .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    nfcId.setText(tag);
                                }
                            }).show();
                }
            });
        }

        @Override
        public void onTagRemoved() {
            disableNfcReaderMode();
        }
    };

    private void adjustFab(final boolean settingCorrect) {
        fab.setChecked(settingCorrect);
        mHideFabRunnable = new Runnable() {
            @Override
            public void run() {
                fab.hide();
                if (!settingCorrect) {
                    getConfigure();
                    onBackPressed();
                }
            }
        };
        mHandler.postDelayed(mHideFabRunnable, 500);
    }

    protected void allowSubmit(boolean edited) {
        if (null != fab) {
            if (edited) {
                fab.show();
            } else {
                fab.hide();
            }
            mEdited = edited;
        }
    }

    public boolean ismEdited() {
        return mEdited;
    }

    private void getConfigure() {
        Intent data = new Intent();
        Bundle b = new Bundle();

        SettingType settingType = new SettingType();

        settingType.setContainerNumber(containerNumber.getText().toString().trim());
        settingType.setOwner(owner.getText().toString().trim());
        settingType.setFreightName(freightName.getText().toString().trim());
        settingType.setOrigin(origin.getText().toString().trim());
        settingType.setDestination(destination.getText().toString().trim());
        settingType.setVessel(vessel.getText().toString().trim());
        settingType.setVoyage(voyage.getText().toString().trim());
        settingType.setFrequency(frequency.getText().toString().trim());
        b.putParcelable(SettingType.EXTRA_DEVICE, settingType);
        if (pictureUri != null) {
            String imgUrl = getImageResourceBase64(picturePreview);
            b.putString(TakePictueActivity.PICTURE_URI, imgUrl);
        } else {
            b.putString(TakePictueActivity.PICTURE_URI, "");
        }
        data.putExtras(b);
        setResult(RESULT_OK, data);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String uriStr = intent.getStringExtra(TakePictueActivity.PICTURE_URI);
            pictureUri = Uri.parse(uriStr);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    picturePreview.setImageURI(pictureUri);
                    picturePreview.setVisibility(View.VISIBLE);
                }
            });
        }
    };

    private String getImageResourceBase64(ImageView imageView) {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        if (drawable == null) return "";
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream);
        String result = Base64.encodeToString(stream.toByteArray(), 0);
        try {
            stream.flush();
            stream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "flush failed or close failed");
            e.printStackTrace();
        }
        bitmap.recycle();
        return result;
    }
}
