package com.agenthun.eseal.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
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
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.bean.base.Result;
import com.agenthun.eseal.connectivity.ble.ACSUtility;
import com.agenthun.eseal.connectivity.manager.RetrofitManager;
import com.agenthun.eseal.connectivity.nfc.NfcUtility;
import com.agenthun.eseal.connectivity.service.PathType;
import com.agenthun.eseal.model.protocol.ESealOperation;
import com.agenthun.eseal.model.utils.Encrypt;
import com.agenthun.eseal.model.utils.PositionType;
import com.agenthun.eseal.model.utils.SensorType;
import com.agenthun.eseal.model.utils.SettingType;
import com.agenthun.eseal.model.utils.SocketPackage;
import com.agenthun.eseal.model.utils.StateType;
import com.agenthun.eseal.utils.ApiLevelHelper;
import com.agenthun.eseal.utils.baidumap.LocationService;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClientOption;
import com.ramotion.foldingcell.FoldingCell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/9 下午7:22.
 */
public class DeviceOperationActivity extends AppCompatActivity {
    private static final String TAG = "DeviceOperationActivity";

    private static final int DEVICE_SETTING = 1;
    private static final long TIME_OUT = 30000;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private ACSUtility.blePort mCurrentPort;
    private ACSUtility utility;
    private boolean utilEnable = false;

    private boolean isPortOpen = false;

    private AppCompatDialog mProgressDialog;

    //    private int id = 11001;
    private static final int rn = 0xABABABAB;
    private static final int key = 0x00000000; //0x87654321; //

    private static final int STATE_OPERATION_INITIAL = -1;
    private static final int STATE_OPERATION_LOCK = 0;
    private static final int STATE_OPERATION_UNLOCK = 1;
    private static final int STATE_OPERATION_SETTING = 2;
    private static final int STATE_DEVICE_PICTURE_ADD = 0;
    private static final int STATE_DEVICE_PICTURE_PREVIEW = 1;

    private NfcUtility mNfcUtility;
    private Uri pictureUri = null;

    private LocationService locationService;

    String coordinateSetting = "0.000000,0.000000";

    @Bind(R.id.card_seting)
    CardView cardSetting;

    @Bind(R.id.folding_cell_lock)
    FoldingCell foldingCellLock;

    @Bind(R.id.cell_content_lock)
    View cellContentLockView;

    @Bind(R.id.cell_title_lock)
    View cellTitleLockView;

    @Bind(R.id.folding_cell_unlock)
    FoldingCell foldingCellUnlock;

    @Bind(R.id.cell_content_unlock)
    View cellContentUnlockView;

    @Bind(R.id.cell_title_unlock)
    View cellTitleUnlockView;

    private View lockAddDevicePicture;
    private ImageView lockPicturePreview;
    private View lockAddPicture;
    private AppCompatTextView lockTime;
    private AppCompatTextView lockLocation;
    private AppCompatTextView lockNfcId;

    private View unlockAddDevicePicture;
    private ImageView unlockPicturePreview;
    private View unlockAddPicture;
    private AppCompatTextView unlockTime;
    private AppCompatTextView unlockLocation;
    private AppCompatTextView unlockNfcId;

    //-1:初始化状态; 0:上封; 1:解封
    private int operationSealSwitch = STATE_OPERATION_INITIAL;
    private boolean isLocationServiceStarting = false;

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


        ((AppCompatTextView) cellTitleLockView.findViewById(R.id.title)).setText(getString(R.string.card_title_lock));
        ((ImageView) cellTitleLockView.findViewById(R.id.background)).setImageResource(R.drawable.cell_lock);
        cellTitleLockView.setBackgroundColor(ContextCompat.getColor(this, R.color.amber_a100_mask));

        ((AppCompatTextView) cellContentLockView.findViewById(R.id.title)).setText(getString(R.string.text_hint_lock_operation));
        lockAddDevicePicture = cellContentLockView.findViewById(R.id.addDevicePicture);
        lockAddDevicePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performTakePictureWithTransition(lockAddDevicePicture);
            }
        });
        lockPicturePreview = (ImageView) cellContentLockView.findViewById(R.id.picturePreview);
        lockAddPicture = cellContentLockView.findViewById(R.id.addPicture);
        lockTime = (AppCompatTextView) cellContentLockView.findViewById(R.id.time);
        lockLocation = (AppCompatTextView) cellContentLockView.findViewById(R.id.location);
        lockNfcId = (AppCompatTextView) cellContentLockView.findViewById(R.id.nfc_id);
        lockNfcId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableNfcReaderMode();
            }
        });
        AppCompatTextView lockConfirmBtn = (AppCompatTextView) cellContentLockView.findViewById(R.id.confirm_button);
        lockConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foldingCellLock.toggle(false);
                if (isLocationServiceStarting) {
                    locationService.stop();
                }

                //发送上封操作报文
                bleOerationLock();

                //服务器访问上封操作
                serviceOerationLock();

                operationSealSwitch = STATE_OPERATION_INITIAL;
            }
        });

        ((AppCompatTextView) cellTitleUnlockView.findViewById(R.id.title)).setText(getString(R.string.card_title_unlock));
        ((ImageView) cellTitleUnlockView.findViewById(R.id.background)).setImageResource(R.drawable.cell_unlock);
        cellTitleUnlockView.setBackgroundColor(ContextCompat.getColor(this, R.color.green_mask));

        ((AppCompatTextView) cellContentUnlockView.findViewById(R.id.title)).setText(getString(R.string.text_hint_unlock_operation));
        unlockAddDevicePicture = cellContentUnlockView.findViewById(R.id.addDevicePicture);
        unlockAddDevicePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performTakePictureWithTransition(unlockAddDevicePicture);
            }
        });
        unlockPicturePreview = (ImageView) cellContentUnlockView.findViewById(R.id.picturePreview);
        unlockAddPicture = cellContentUnlockView.findViewById(R.id.addPicture);
        unlockTime = (AppCompatTextView) cellContentUnlockView.findViewById(R.id.time);
        unlockLocation = (AppCompatTextView) cellContentUnlockView.findViewById(R.id.location);
        unlockNfcId = (AppCompatTextView) cellContentUnlockView.findViewById(R.id.nfc_id);
        unlockNfcId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableNfcReaderMode();
            }
        });
        AppCompatTextView unlockConfirmBtn = (AppCompatTextView) cellContentUnlockView.findViewById(R.id.confirm_button);
        unlockConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foldingCellUnlock.toggle(false);
                if (isLocationServiceStarting) {
                    locationService.stop();
                }

                //发送解封操作报文
                bleOerationUnlock();

                //服务器访问解封操作
                serviceOerationUnlock();

                operationSealSwitch = STATE_OPERATION_INITIAL;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(TakePictueActivity.PICTURE_URI);

        localBroadcastManager.registerReceiver(broadcastReceiver, filter);

        mNfcUtility = new NfcUtility(tagCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationService = ((App) (getApplication())).locationService;
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        //注册监听
        LocationClientOption mOption = locationService.getDefaultLocationClientOption();
        mOption.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        locationService.setLocationOption(mOption);
    }

    @Override
    public void onStop() {
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
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
        startActivity(new Intent(this, MainActivityX.class));
        finish();
    }

    @OnClick(R.id.card_seting)
    public void onSettingBtnClick() {
//        getDeviceId(); //获取设备ID
        operationSealSwitch = STATE_OPERATION_SETTING; //配置

        locationService.requestLocation(this);// 定位SDK
        isLocationServiceStarting = true;

        //配置信息
        Intent intent = new Intent(DeviceOperationActivity.this, DeviceSettingActivity.class);
        startActivityForResult(intent, DEVICE_SETTING);
    }

    @OnClick(R.id.cell_title_lock)
    public void onFoldingCellLockBtnClick() {
        if (operationSealSwitch == STATE_OPERATION_UNLOCK) {
            foldingCellUnlock.toggle(true);
        }
        operationSealSwitch = STATE_OPERATION_LOCK; //上封

        updateFoldingCellState(operationSealSwitch);
/*        if (isLocationServiceStarting) {
            locationService.stop();
        }*/
        locationService.requestLocation(this);// 定位SDK
        isLocationServiceStarting = true;
    }

    @OnClick(R.id.cell_title_unlock)
    public void onFoldingCellUnlockBtnClick() {
        if (operationSealSwitch == STATE_OPERATION_LOCK) {
            foldingCellLock.toggle(true);
        }
        operationSealSwitch = STATE_OPERATION_UNLOCK; //解封

        updateFoldingCellState(operationSealSwitch);

/*        if (isLocationServiceStarting) {
            locationService.stop();
        }*/
        locationService.requestLocation(this);// 定位SDK
        isLocationServiceStarting = true;
    }

    private void performTakePictureWithTransition(View v) {
        Activity activity = DeviceOperationActivity.this;

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
                Snackbar.make(foldingCellLock, getString(R.string.text_hint_close_to_nfc_tag), Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            } else {
                Snackbar.make(foldingCellLock, getString(R.string.error_nfc_not_open), Snackbar.LENGTH_SHORT)
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(DeviceOperationActivity.this);
                    builder.setTitle(R.string.text_hint_nfc_id)
                            .setMessage(tag)
                            .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (operationSealSwitch) {
                                        case STATE_OPERATION_LOCK: //上封
                                            lockNfcId.setText(tag);
                                            break;
                                        case STATE_OPERATION_UNLOCK: //解封
                                            unlockNfcId.setText(tag);
                                            break;
                                    }
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

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String uriStr = intent.getStringExtra(TakePictueActivity.PICTURE_URI);
            pictureUri = Uri.parse(uriStr);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateAddDevicePictureState(operationSealSwitch, STATE_DEVICE_PICTURE_PREVIEW);
                    switch (operationSealSwitch) {
                        case STATE_OPERATION_LOCK: //上封
                            Log.d(TAG, "onAddDevicePictureBtnClick() returned: 上封");
                            lockPicturePreview.setImageURI(pictureUri);
                            break;
                        case STATE_OPERATION_UNLOCK: //解封
                            Log.d(TAG, "onAddDevicePictureBtnClick() returned: 解封");
                            unlockPicturePreview.setImageURI(pictureUri);
                            break;
                    }
                }
            });
        }
    };

    private void updateAddDevicePictureState(int operationSealSwitch, int state) {
        switch (operationSealSwitch) {
            case STATE_OPERATION_LOCK: //上封
                if (state == STATE_DEVICE_PICTURE_ADD) {
                    lockAddPicture.setVisibility(View.VISIBLE);
                    lockPicturePreview.setVisibility(View.GONE);
                } else if (state == STATE_DEVICE_PICTURE_PREVIEW) {
                    lockAddPicture.setVisibility(View.GONE);
                    lockPicturePreview.setVisibility(View.VISIBLE);
                }
                break;
            case STATE_OPERATION_UNLOCK: //解封
                if (state == STATE_DEVICE_PICTURE_ADD) {
                    unlockAddPicture.setVisibility(View.VISIBLE);
                    unlockPicturePreview.setVisibility(View.GONE);
                } else if (state == STATE_DEVICE_PICTURE_PREVIEW) {
                    unlockAddPicture.setVisibility(View.GONE);
                    unlockPicturePreview.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void updateFoldingCellState(int operationSealSwitch) {
        updateAddDevicePictureState(operationSealSwitch, STATE_DEVICE_PICTURE_ADD);

        String operateTime = DATE_FORMAT.format(Calendar.getInstance().getTime());

        switch (operationSealSwitch) {
            case STATE_OPERATION_LOCK: //上封
                foldingCellLock.toggle(false);
                lockTime.setText(operateTime);
                lockNfcId.setText(getString(R.string.text_hint_get_nfc_tag));
                break;
            case STATE_OPERATION_UNLOCK: //解封
                foldingCellUnlock.toggle(false);
                unlockTime.setText(operateTime);
                unlockNfcId.setText(getString(R.string.text_hint_get_nfc_tag));
                break;
        }
    }

    private void serviceOerationSetting(SettingType settingType, String coordinate, String imageResourceStr) {
        String token = App.getToken();
        if (token != null) {
            String operateTime = DATE_FORMAT.format(Calendar.getInstance().getTime());

            RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST)
                    .configureDeviceObservable(token, App.getDeviceId(),
                            settingType.getContainerNumber(), settingType.getOwner(), settingType.getFreightName(),
                            settingType.getOrigin(), settingType.getDestination(), settingType.getVessel(), settingType.getVoyage(),
                            settingType.getFrequency(),
                            App.getTagId(),
                            imageResourceStr,
                            coordinate,
                            operateTime)
                    .subscribe(new Action1<Result>() {
                        @Override
                        public void call(Result result) {
                            int res = result.getRESULT();
                            if (res == 1) {
                                showSnackbar(getString(R.string.success_device_setting_upload));
                            } else {
                                if (result.getERRORINFO().equals("15")) {
                                    showSnackbar(getString(R.string.fail_device_nfc_tag));
                                } else {
                                    showSnackbar(getString(R.string.fail_device_setting_upload));
                                }
                            }
                        }
                    });
        }
    }

    private void serviceOerationLock() {
        String token = App.getToken();
        if (token != null) {
            String imgUrl = getImageResourceBase64(lockPicturePreview);
            String operateTime = lockTime.getText().toString();
            String coordinate = "0.000000,0.000000";
            if (!TextUtils.equals(lockLocation.getText(), getString(R.string.text_hint_get_current_location))) {
                coordinate = lockLocation.getText().toString().split(", ")[1];
            }
            String tagId = "00000000000000";
            if (!TextUtils.equals(lockNfcId.getText(), getString(R.string.text_hint_get_nfc_tag))) {
                tagId = lockNfcId.getText().toString();
            }

            imgUrl = "";

            RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST)
                    .closeDeviceObservable(token, App.getDeviceId(), tagId, imgUrl, coordinate, operateTime)
                    .subscribe(new Action1<Result>() {
                        @Override
                        public void call(Result result) {
                            int r = result.getRESULT() == null ? 0 : result.getRESULT();
                            if (r == 1) {
                                showSnackbar(getString(R.string.success_device_setting_upload));
                            } else {
                                if (result.getERRORINFO().equals("15")) {
                                    showSnackbar(getString(R.string.fail_device_nfc_tag));
                                } else {
                                    showSnackbar(getString(R.string.fail_device_setting_upload));
                                }
                            }
                        }
                    });
        }
    }

    private void serviceOerationUnlock() {
        String token = App.getToken();
        if (token != null) {
            String imgUrl = getImageResourceBase64(unlockPicturePreview);
            String operateTime = unlockTime.getText().toString();
            String coordinate = "0.000000,0.000000";
            if (!TextUtils.equals(unlockLocation.getText(), getString(R.string.text_hint_get_current_location))) {
                coordinate = unlockLocation.getText().toString().split(", ")[1];
            }
            String tagId = "00000000000000";
            if (!TextUtils.equals(unlockNfcId.getText(), getString(R.string.text_hint_get_nfc_tag))) {
                tagId = unlockNfcId.getText().toString();
            }

            imgUrl = "";

            RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST)
                    .openDeviceObservable(token, App.getDeviceId(), tagId, imgUrl, coordinate, operateTime)
                    .subscribe(new Action1<Result>() {
                        @Override
                        public void call(Result result) {
                            int r = result == null ? 0 : result.getRESULT();
                            if (r == 1) {
                                showSnackbar(getString(R.string.success_device_setting_upload));
                            } else {
                                if (result.getERRORINFO().equals("15")) {
                                    showSnackbar(getString(R.string.fail_device_nfc_tag));
                                } else {
                                    showSnackbar(getString(R.string.fail_device_setting_upload));
                                }
                            }
                        }
                    });
        }
    }

    private void bleOerationLock() {
        //发送上封操作报文
        ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION);
        buffer.putInt(Integer.parseInt(App.getDeviceId()));
        buffer.putInt(rn);
        buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION);
        buffer.put(ESealOperation.operationOperation(Integer.parseInt(App.getDeviceId()), rn, key,
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

    private void bleOerationUnlock() {
        //发送解封操作报文
        ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION);
        buffer.putInt(Integer.parseInt(App.getDeviceId()));
        buffer.putInt(rn);
        buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION);
        buffer.put(ESealOperation.operationOperation(Integer.parseInt(App.getDeviceId()), rn, key,
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

    @OnClick(R.id.card_query_status)
    public void onQueryStatusBtnClick() {
//        Log.d(TAG, "onQueryStatusBtnClick() returned: ");
        //发送查询操作报文
        ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_QUERY);
        buffer.putInt(Integer.parseInt(App.getDeviceId()));
        buffer.putInt(rn);
        buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_QUERY);
        buffer.put(ESealOperation.operationQuery(Integer.parseInt(App.getDeviceId()), rn, key));

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
        buffer.putInt(Integer.parseInt(App.getDeviceId()));
        buffer.putInt(rn);
        buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_INFO);
        buffer.put(ESealOperation.operationInfo(Integer.parseInt(App.getDeviceId()), rn, key));

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
        buffer.putInt(Integer.parseInt(App.getDeviceId()));
        buffer.putInt(rn);
        buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_READ_DATA);
        buffer.put(ESealOperation.operationReadData(Integer.parseInt(App.getDeviceId()), rn, key,
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
            final SettingType settingType = data.getExtras().getParcelable(SettingType.EXTRA_DEVICE);
            Log.d(TAG, "onActivityResult() returned: settingType = " + settingType.toString());

            String imageResourceStr = data.getExtras().getString(TakePictueActivity.PICTURE_URI);

            int period = ESealOperation.PERIOD_DEFAULT;
            if (settingType.getFrequency() != null && settingType.getFrequency().length() != 0) {
                period = Integer.parseInt(settingType.getFrequency());
            }
            //发送配置操作报文
            ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_CONFIG);
            buffer.putInt(Integer.parseInt(App.getDeviceId()));
            buffer.putInt(rn);
            buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_CONFIG);
            buffer.put(ESealOperation.operationConfig(Integer.parseInt(App.getDeviceId()), rn, key,
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
                    buffer.putInt(Integer.parseInt(App.getDeviceId()));
                    buffer.putInt(rn);
                    buffer.putShort(
                            (short) (ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_WRITE_DATA_WITHOUT_DLEN + writeData.length));
                    buffer.put(ESealOperation.operationWriteData(Integer.parseInt(App.getDeviceId()), rn, key,
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

            imageResourceStr = "";
            serviceOerationSetting(settingType, coordinateSetting, imageResourceStr);

            operationSealSwitch = STATE_OPERATION_INITIAL;
        }
    }

    //获取设备ID报文
    private void getDeviceId() {
        //发送获取设备ID报文
        SocketPackage socketPackage = new SocketPackage();
        byte[] data = socketPackage.packageAddHeader(ESealOperation.ESEALBD_OPERATION_PORT,
                ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_GET_DEVICE_ID,
                ESealOperation.operationGetDeviceId()
        );
        sendData(data);
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
                    if (!isPortOpen && utilEnable) {
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
                        .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getDeviceId(); //获取设备ID
                            }
                        }).show();
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

                //判断是否是获取设备ID应答报文
                if (lenTotal == 20
                        && receiveData[6] == (byte) 0xA0
                        && receiveData[7] == (byte) 0x02
                        && receiveData[12] == (byte) 0x1F
                        && receiveData[13] == (byte) 0x02
                        && receiveData[14] == (byte) 0x00
                        && receiveData[15] == (byte) 0x04) {
                    ByteBuffer buffer = ByteBuffer.allocate(4);
                    buffer.put(receiveData, 16, 4);
                    buffer.rewind();
                    int id = buffer.getInt();
                    App.setDeviceId(String.valueOf(id));
                    Log.d(TAG, "Get device id: " + App.getDeviceId());
                    return;
                }

                Encrypt.decrypt(Integer.parseInt(App.getDeviceId()), rn, key, receiveData,
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
//                                time.append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
//                                        .format(Calendar.getInstance().getTime()));
//                                positionType.setPosition("121.44755, 31.029331");
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
                                                    getString(R.string.text_hint_nfc_id) + " " + settingType.getNfcTagId())
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

    private void showAlertDialog(final String title, final String msg, @Nullable final DialogInterface.OnClickListener listener) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(DeviceOperationActivity.this)
                        .setTitle(title)
                        .setMessage(msg)
                        .setPositiveButton(R.string.text_ok, listener).show();
            }
        });
    }

    private void showSnackbar(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(cardSetting, msg, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
    }

    private void sendData(byte[] data) {
        utility.writePort(data);
    }

    /**
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     */
    private BDLocationListener mListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                String time = location.getTime();
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                String address = location.getAddrStr() + ", " + lat + "," + lng;
//                String address = lat + "," + lng;
                switch (operationSealSwitch) {
                    case STATE_OPERATION_LOCK: //上封
                        lockTime.setText(time);
                        lockLocation.setText(address);
                        break;
                    case STATE_OPERATION_UNLOCK: //解封
                        unlockTime.setText(time);
                        unlockLocation.setText(address);
                        break;
                    case STATE_OPERATION_SETTING: //配置
                        coordinateSetting = lat + "," + lng;
                        break;
                }
            } else if (null != location && location.getLocType() == BDLocation.TypeServerError) {
                switch (operationSealSwitch) {
                    case STATE_OPERATION_LOCK: //上封
                        lockLocation.setText(getString(R.string.fail_get_current_location));
                        break;
                    case STATE_OPERATION_UNLOCK: //解封
                        unlockLocation.setText(getString(R.string.fail_get_current_location));
                        break;
                }
            } else if (null != location && location.getLocType() == BDLocation.TypeNetWorkException) {
                switch (operationSealSwitch) {
                    case STATE_OPERATION_LOCK: //上封
                        lockLocation.setText(getString(R.string.fail_get_current_location));
                        break;
                    case STATE_OPERATION_UNLOCK: //解封
                        unlockLocation.setText(getString(R.string.fail_get_current_location));
                        break;
                }
            } else if (null != location && location.getLocType() == BDLocation.TypeCriteriaException) {
                switch (operationSealSwitch) {
                    case STATE_OPERATION_LOCK: //上封
                        lockLocation.setText(getString(R.string.fail_get_current_location));
                        break;
                    case STATE_OPERATION_UNLOCK: //解封
                        unlockLocation.setText(getString(R.string.fail_get_current_location));
                        break;
                }
            }

            isLocationServiceStarting = false;
        }
    };
}
