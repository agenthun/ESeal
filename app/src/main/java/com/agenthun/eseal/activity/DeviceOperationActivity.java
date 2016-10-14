package com.agenthun.eseal.activity;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;
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

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.connectivity.ble.ACSUtility;
import com.agenthun.eseal.connectivity.nfc.NfcUtility;
import com.agenthun.eseal.model.protocol.ESealOperation;
import com.agenthun.eseal.model.utils.Encrypt;
import com.agenthun.eseal.model.utils.PositionType;
import com.agenthun.eseal.model.utils.SensorType;
import com.agenthun.eseal.model.utils.SettingType;
import com.agenthun.eseal.model.utils.SocketPackage;
import com.agenthun.eseal.model.utils.StateType;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
    private NfcUtility mNfcUtility;

    private AppCompatDialog mProgressDialog;

    @Bind(R.id.card_seting)
    CardView cardSetting;

    private static final int id = 11001; //30000;//
    private static final int rn = 0xABABABAB;
    private static final int key = 0x00000000; //0x87654321; //

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

        mNfcUtility = new NfcUtility(tagCallback);

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
    protected void onPause() {
        super.onPause();
        disableNfcReaderMode();
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
//        Log.d(TAG, "onLockBtnClick() returned: ");
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
//        Log.d(TAG, "onUnlockBtnClick() returned: ");
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

    @OnClick(R.id.card_scan_nfc)
    public void onScanNfcBtnClick() {
//        Log.d(TAG, "onScanNfcBtnClick() returned: ");
        //扫描NFC封条,获取ID
        enableNfcReaderMode();
    }

    @OnClick(R.id.card_query_status)
    public void onQueryStatusBtnClick() {
//        Log.d(TAG, "onQueryStatusBtnClick() returned: ");
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
//        Log.d(TAG, "onQueryInfoBtnClick() returned: ");
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

    @OnClick(R.id.card_read_seting)
    public void onReadSettingBtnClick() {
//        Log.d(TAG, "onReadSettingBtnClick() returned: ");
        //发送读数据报文
        ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_READ_DATA);
        buffer.putInt(id);
        buffer.putInt(rn);
        buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_READ_DATA);
        buffer.put(ESealOperation.operationReadData(id, rn, key,
                ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_READ_DATA_WITHOUT_LIMIT)
        );

        SocketPackage socketPackage = new SocketPackage();
        byte[] data = socketPackage.packageAddHeader(ESealOperation.ESEALBD_OPERATION_PORT,
                10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_READ_DATA,
                buffer.array()
        );
        sendData(data);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DEVICE_SETTING && resultCode == RESULT_OK) {
//            DetailParcelable detail = data.getExtras().getParcelable(DetailParcelable.EXTRA_DEVICE);
            final SettingType settingType = data.getExtras().getParcelable(SettingType.EXTRA_DEVICE);
            Log.d(TAG, "onActivityResult() returned: " + settingType.toString());

            int period = ESealOperation.PERIOD_DEFAULT;
            if (settingType.getFrequency() != null && settingType.getFrequency().length() != 0) {
                period = Integer.parseInt(settingType.getFrequency());
            }
            //发送配置操作报文
            ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_CONFIG);
            buffer.putInt(id);
            buffer.putInt(rn);
            buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_CONFIG);
            buffer.put(ESealOperation.operationConfig(id, rn, key,
                    period,
                    ESealOperation.WINDOW_DEFAULT,
                    ESealOperation.CHANNEL_DEFAULT,
                    new SensorType())
            );

            SocketPackage socketPackage = new SocketPackage();
            final byte[] settingData = socketPackage.packageAddHeader(ESealOperation.ESEALBD_OPERATION_PORT,
                    10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_CONFIG,
                    buffer.array()
            );
            sendData(settingData);

            //发送写数据报文
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    settingType.setNfcTagId(App.getTagId());

                    byte[] writeData = settingType.getSettingTypeString().getBytes();
                    ByteBuffer buffer = ByteBuffer.allocate(10 +
                            ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_WRITE_DATA_WITHOUT_DLEN +
                            writeData.length);
                    buffer.putInt(id);
                    buffer.putInt(rn);
                    buffer.putShort(
                            (short) (ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_WRITE_DATA_WITHOUT_DLEN + writeData.length));
                    buffer.put(ESealOperation.operationWriteData(id, rn, key,
                            writeData,
                            (short) writeData.length
                    ));

                    SocketPackage socketPackage = new SocketPackage();
                    byte[] settingData = socketPackage.packageAddHeader(ESealOperation.ESEALBD_OPERATION_PORT,
                            10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_WRITE_DATA_WITHOUT_DLEN + writeData.length,
                            buffer.array()
                    );
                    sendData(settingData);
                }
            }, 1000);
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
/*            StringBuffer sb = new StringBuffer();
            for (byte b : packageToSend) {
                if ((b & 0xff) <= 0x0f) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(b & 0xff) + " ");
            }
            Log.d(TAG, sb.toString());*/

            if (socketPackageReceived.packageReceive(socketPackageReceived, packageToSend) == 1) {
                Log.d(TAG, "didPackageReceived() returned: ok");
                socketPackageReceived.setFlag(0);
                socketPackageReceived.setCount(0);
                byte[] receiveData = socketPackageReceived.getData();
                int lenTotal = receiveData.length;
                Log.d(TAG, "getCount() returned: " + lenTotal);
                Encrypt.decrypt(id, rn, key, receiveData,
                        ESealOperation.ESEALBD_PROTOCOL_CMD_DATA_OFFSET,
                        lenTotal - ESealOperation.ESEALBD_PROTOCOL_CMD_DATA_OFFSET);

                ByteBuffer buffer = ByteBuffer.allocate(lenTotal);
                buffer.put(receiveData);
                short prococolPort = buffer.getShort(6);
                short type = buffer.getShort(ESealOperation.ESEALBD_PROTOCOL_CMD_DATA_OFFSET + 2);

                if ((prococolPort & 0xffff) == ESealOperation.ESEALBD_OPERATION_PORT) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DeviceOperationActivity.this);
                    switch (type) {
                        case ESealOperation.ESEALBD_OPERATION_TYPE_REPLAY_QUERY:
                            Log.d(TAG, "ESEALBD_OPERATION_TYPE_REPLAY_QUERY");
                            StateType stateType = new StateType();
                            ESealOperation.operationQueryReplay(buffer, stateType);

                            String safeStringQuery = (stateType.getSafe() == 0 ?
                                    getString(R.string.device_reply_safe_0) :
                                    (stateType.getSafe() == 1 ?
                                            getString(R.string.device_reply_safe_1) : getString(R.string.device_reply_safe_2)));
                            String isLockStringQuery = stateType.isLocked() ?
                                    getString(R.string.device_reply_lock) : getString(R.string.device_reply_unlock);
                            builder.setTitle(R.string.device_reply_query_title)
                                    .setMessage(getString(R.string.text_upload_period) + " " + stateType.getPeriod()
                                            + " s\r\n\r\n" + safeStringQuery
                                            + "\r\n\r\n" + getString(R.string.text_device_status) + " " + isLockStringQuery)
                                    .setPositiveButton(R.string.text_ok, null).show();
                            break;
                        case ESealOperation.ESEALBD_OPERATION_TYPE_REPLAY_INFO:
                            Log.d(TAG, "ESEALBD_OPERATION_TYPE_REPLAY_INFO");
                            PositionType positionType = new PositionType();
                            ESealOperation.operationInfoReplay(buffer, positionType);
                            Calendar calendar = positionType.getCalendar();
                            StringBuffer time = new StringBuffer();
                            if (positionType.getPosition() != null) {
                                time.append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                                        .format(calendar.getTime()));
                            } else {
                                time.append(getString(R.string.device_reply_info_time_error));
                            }
                            String safeStringInfo = (positionType.getSafe() == 0 ?
                                    getString(R.string.device_reply_safe_0) :
                                    (positionType.getSafe() == 1 ?
                                            getString(R.string.device_reply_safe_1) : getString(R.string.device_reply_safe_2)));
                            String isLockStringInfo = positionType.isLocked() ?
                                    getString(R.string.device_reply_lock) : getString(R.string.device_reply_unlock);
                            builder.setTitle(R.string.device_reply_info_title)
                                    .setMessage(time.toString()
                                            + "\r\n\r\n" + getString(R.string.text_current_position) + " " + positionType.getPosition()
                                            + "\r\n\r\n" + safeStringInfo
                                            + "\r\n\r\n" + getString(R.string.text_device_status) + " " + isLockStringInfo)
                                    .setPositiveButton(R.string.text_ok, null).show();
                            break;
                        case ESealOperation.ESEALBD_OPERATION_TYPE_REPLAY_READ_DATA:
                            Log.d(TAG, "ESEALBD_OPERATION_TYPE_REPLAY_READ_DATA");
                            SettingType settingType = new SettingType();
                            ESealOperation.operationReadSettingReplay(buffer, settingType);

                            builder.setTitle(R.string.device_reply_read_setting_title)
                                    .setMessage(
                                            getString(R.string.text_hint_freight_container_number) + " " + settingType.getContainerNumber() + "\r\n\r\n" +
                                                    getString(R.string.text_hint_freight_owner) + " " + settingType.getOwner() + "\r\n\r\n" +
                                                    getString(R.string.text_hint_freight_name) + " " + settingType.getFreightName() + "\r\n\r\n" +
                                                    getString(R.string.text_hint_freight_origin) + " " + settingType.getOrigin() + "\r\n\r\n" +
                                                    getString(R.string.text_hint_freight_destination) + " " + settingType.getDestination() + "\r\n\r\n" +
                                                    getString(R.string.text_hint_freight_vessel) + " " + settingType.getVessel() + "\r\n\r\n" +
                                                    getString(R.string.text_hint_freight_voyage) + " " + settingType.getVoyage() + "\r\n\r\n" +
                                                    getString(R.string.text_hint_freight_frequency) + " " + settingType.getFrequency() + "\r\n\r\n" +
                                                    getString(R.string.text_current_position) + " " + settingType.getNfcTagId())
                                    .setPositiveButton(R.string.text_ok, null).show();

                            break;
                    }
                }
            }
        }

        @Override
        public void heartbeatDebug() {

        }
    };

    private SocketPackage socketPackageReceived = new SocketPackage();


    private AppCompatDialog getProgressDialog() {
        if (mProgressDialog != null) {
            return mProgressDialog;
        }
        mProgressDialog = new AppCompatDialog(DeviceOperationActivity.this, AppCompatDelegate.MODE_NIGHT_AUTO);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setContentView(R.layout.dialog_device_connecting);
        mProgressDialog.setTitle(getString(R.string.device_connecting));
        mProgressDialog.setCancelable(false);
        return mProgressDialog;
    }

    private void sendData(byte[] data) {
        utility.writePort(data);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void enableNfcReaderMode() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            nfcAdapter.enableReaderMode(this, mNfcUtility, NfcUtility.NFC_TAG_FLAGS, null);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(DeviceOperationActivity.this);
                    builder.setTitle(R.string.text_hint_nfc_id)
                            .setMessage(tag)
                            .setPositiveButton(R.string.text_ok, null).show();
                }
            });
        }

        @Override
        public void onTagRemoved() {
            disableNfcReaderMode();
        }
    };
}
