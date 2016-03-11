package com.agenthun.eseal.activity;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.agenthun.eseal.R;
import com.agenthun.eseal.bean.base.DetailParcelable;
import com.agenthun.eseal.connectivity.ble.ACSUtility;
import com.agenthun.eseal.model.protocol.ESealOperation;
import com.agenthun.eseal.model.utils.SensorType;
import com.agenthun.eseal.model.utils.SocketPackage;

import java.nio.ByteBuffer;

import butterknife.Bind;
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

    @Bind(R.id.card_seting)
    CardView cardSetting;

    private int id = 0x12345678;
    private int rn = 0xABABABAB;
    private int key = 0x87654321;

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
        //配置信息
        Intent intent = new Intent(DeviceOperationActivity.this, DeviceSettingActivity.class);
        startActivityForResult(intent, DEVICE_SETTING);
    }

    @OnClick(R.id.card_lock)
    public void onLockBtnClick() {
        Log.d(TAG, "onLockBtnClick() returned: ");
        //发送上封操作报文
        ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION);
        buffer.putInt(id);
        buffer.putInt(rn);
        buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION);
        buffer.put(ESealOperation.operationOperation(id, rn, key,
                        ESealOperation.POWER_ON,
                        ESealOperation.SAFE_LOCK)
        );

        SocketPackage socketPackage = new SocketPackage();
        byte[] data = socketPackage.packageAddHeader(ESealOperation.ESEALBD_OPERATION_PORT,
                10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION,
                buffer.array()
        );
        sendData(data);
    }

    @OnClick(R.id.card_unlock)
    public void onUnlockBtnClick() {
        Log.d(TAG, "onUnlockBtnClick() returned: ");
        //发送解封操作报文
        ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION);
        buffer.putInt(id);
        buffer.putInt(rn);
        buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION);
        buffer.put(ESealOperation.operationOperation(id, rn, key,
                        ESealOperation.POWER_ON,
                        ESealOperation.SAFE_UNLOCK)
        );

        SocketPackage socketPackage = new SocketPackage();
        byte[] data = socketPackage.packageAddHeader(ESealOperation.ESEALBD_OPERATION_PORT,
                10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION,
                buffer.array()
        );
        sendData(data);
    }

    @OnClick(R.id.card_query_status)
    public void onQueryStatusBtnClick() {
        Log.d(TAG, "onQueryStatusBtnClick() returned: ");
        //发送查询操作报文
        ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_QUERY);
        buffer.putInt(id);
        buffer.putInt(rn);
        buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_QUERY);
        buffer.put(ESealOperation.operationQuery(id, rn, key));

        SocketPackage socketPackage = new SocketPackage();
        byte[] data = socketPackage.packageAddHeader(ESealOperation.ESEALBD_OPERATION_PORT,
                10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_QUERY,
                buffer.array()
        );
        sendData(data);
    }

    @OnClick(R.id.card_query_info)
    public void onQueryInfoBtnClick() {
        Log.d(TAG, "onQueryInfoBtnClick() returned: ");
        //发送位置请求信息操作报文
        ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_INFO);
        buffer.putInt(id);
        buffer.putInt(rn);
        buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_INFO);
        buffer.put(ESealOperation.operationInfo(id, rn, key));

        SocketPackage socketPackage = new SocketPackage();
        byte[] data = socketPackage.packageAddHeader(ESealOperation.ESEALBD_OPERATION_PORT,
                10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_INFO,
                buffer.array()
        );
        sendData(data);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DEVICE_SETTING && resultCode == RESULT_OK) {
            DetailParcelable detail = data.getExtras().getParcelable(DetailParcelable.EXTRA_DEVICE);
            Log.d(TAG, "onActivityResult() returned: " + detail.toString());

            //发送配置操作报文
            ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_CONFIG);
            buffer.putInt(id);
            buffer.putInt(rn);
            buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_CONFIG);
            buffer.put(ESealOperation.operationConfig(id, rn, key,
                            ESealOperation.PERIOD_DEFAULT,
                            ESealOperation.WINDOW_DEFAULT,
                            ESealOperation.CHANNEL_DEFAULT,
                            new SensorType())
            );

            SocketPackage socketPackage = new SocketPackage();
            byte[] settingData = socketPackage.packageAddHeader(ESealOperation.ESEALBD_OPERATION_PORT,
                    10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_CONFIG,
                    buffer.array()
            );
            sendData(settingData);
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
            if (succeed) {
                Snackbar.make(cardSetting, getString(R.string.success_device_send_data), Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            } else {
                Snackbar.make(cardSetting, getString(R.string.fail_device_send_data), Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        }

        @Override
        public void didPackageReceived(ACSUtility.blePort port, byte[] packageToSend) {
            StringBuffer sb = new StringBuffer();
            for (byte b : packageToSend) {
                if ((b & 0xff) <= 0x0f) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(b & 0xff) + " ");
            }
            Log.d(TAG, sb.toString());

/*            SocketPackageReceive((sSocketPackageTypeDef *)Comm2Data, &byte,1,
                    ESEALBD_OPERATION_CMD_MAX_SIZE, pipe, (FNCT_COMM) COM2TxFIFOIn);*/
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

    private void sendData(byte[] data) {
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
