package com.agenthun.eseal.activity;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.agenthun.eseal.R;
import com.agenthun.eseal.bean.base.DetailParcelable;
import com.agenthun.eseal.connectivity.ble.ACSUtility;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/9 下午7:22.
 */
public class DeviceOperationActivity extends AppCompatActivity {
    private static final String TAG = "DeviceOperationActivity";

    private static final int DEVICE_SETTING = 1;
    private static final long TIME_OUT = 30000;

    private ACSUtility.blePort mCurrentPort;
    private ACSUtility utility;
    private boolean utilEnable = false;
    private boolean isPortOpen = false;

    private AppCompatDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_operation);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        BluetoothDevice device = bundle.getParcelable(BluetoothDevice.EXTRA_DEVICE);
        Log.d(TAG, "onCreate() returned: " + device.getAddress());

        utility = new ACSUtility(this, callback);
        mCurrentPort = utility.new blePort(device);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(device.getAddress());
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getProgressDialog().show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (utilEnable) {
            utilEnable = false;
            utility.closePort();
            isPortOpen = false;
            utility.closeACSUtility();
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @OnClick(R.id.card_seting)
    public void onSettingBtnClick() {
        Intent intent = new Intent(DeviceOperationActivity.this, DeviceSettingActivity.class);
        startActivityForResult(intent, DEVICE_SETTING);
    }

    @OnClick(R.id.card_lock)
    public void onLockBtnClick() {
        Log.d(TAG, "onLockBtnClick() returned: ");
        if (isPortOpen) {
            sendData();
        }
    }

    @OnClick(R.id.card_unlock)
    public void onUnlockBtnClick() {
        Log.d(TAG, "onUnlockBtnClick() returned: ");
    }

    @OnClick(R.id.card_query)
    public void onQueryBtnClick() {
        Log.d(TAG, "onQueryBtnClick() returned: ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DEVICE_SETTING && resultCode == RESULT_OK) {
            DetailParcelable detail = data.getExtras().getParcelable(DetailParcelable.EXTRA_DEVICE);
            Log.d(TAG, "onActivityResult() returned: " + detail.toString());
        }
    }

    private ACSUtility.IACSUtilityCallback callback = new ACSUtility.IACSUtilityCallback() {
        @Override
        public void utilReadyForUse() {
            Log.d(TAG, "utilReadyForUse() returned:");
            utilEnable = true;
            utility.openPort(mCurrentPort);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isPortOpen) {
                        getProgressDialog().cancel();
                        new AlertDialog.Builder(DeviceOperationActivity.this)
                                .setTitle(mCurrentPort._device.getName())
                                .setMessage(R.string.time_out_device_connection)
                                .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        onBackPressed();
                                    }
                                }).show();
                    }
                }
            }, TIME_OUT);
        }

        @Override
        public void didFoundPort(final ACSUtility.blePort newPort, final int rssi) {

        }

        @Override
        public void didFinishedEnumPorts() {
        }

        @Override
        public void didOpenPort(final ACSUtility.blePort port, Boolean bSuccess) {
            Log.d(TAG, "didOpenPort() returned: " + bSuccess);
            AlertDialog.Builder builder = new AlertDialog.Builder(DeviceOperationActivity.this);
            isPortOpen = bSuccess;
            if (bSuccess) {
                getProgressDialog().cancel();
                builder.setTitle(port._device.getName())
                        .setMessage(R.string.success_device_connection)
                        .setPositiveButton(R.string.text_ok, null).show();
            } else {
                getProgressDialog().cancel();
                builder.setTitle(port._device.getName())
                        .setMessage(R.string.fail_device_connection)
                        .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onBackPressed();
                            }
                        }).show();
            }
        }

        @Override
        public void didClosePort(ACSUtility.blePort port) {
            Log.d(TAG, "didClosePort() returned: " + port._device.getAddress());
        }

        @Override
        public void didPackageSended(boolean succeed) {
            Log.d(TAG, "didPackageSended() returned: " + succeed);
        }

        @Override
        public void didPackageReceived(ACSUtility.blePort port, byte[] packageToSend) {
        }

        @Override
        public void heartbeatDebug() {

        }
    };

    private AppCompatDialog getProgressDialog() {
        if (mProgressDialog != null) {
            return mProgressDialog;
        }
        mProgressDialog = new AppCompatDialog(DeviceOperationActivity.this, AppCompatDelegate.MODE_NIGHT_AUTO);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setContentView(R.layout.dialog_device_connecting);
        mProgressDialog.setTitle(getString(R.string.device_connecting));
        return mProgressDialog;
    }

    private void sendData() {
        byte[] data = new byte[128];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
            Log.d(TAG, "bytes[" + i + "] = " + data[i]);
        }
        utility.writePort(data);
    }

    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        stringBuilder.append('\n');
        return stringBuilder.toString();
    }
}
