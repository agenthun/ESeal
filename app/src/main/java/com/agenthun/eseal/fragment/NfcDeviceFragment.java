package com.agenthun.eseal.fragment;

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
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.activity.DeviceOperationActivity;
import com.agenthun.eseal.activity.DeviceSettingActivity;
import com.agenthun.eseal.activity.TakePictueActivity;
import com.agenthun.eseal.bean.base.Result;
import com.agenthun.eseal.connectivity.manager.RetrofitManager;
import com.agenthun.eseal.connectivity.nfc.NfcUtility;
import com.agenthun.eseal.connectivity.service.Api;
import com.agenthun.eseal.connectivity.service.PathType;
import com.agenthun.eseal.model.utils.SettingType;
import com.agenthun.eseal.utils.ApiLevelHelper;
import com.agenthun.eseal.utils.baidumap.LocationService;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClientOption;
import com.ramotion.foldingcell.FoldingCell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/7 上午5:47.
 */
public class NfcDeviceFragment extends Fragment {

    private static final String TAG = "NfcDeviceFragment";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final int DEVICE_SETTING = 1;

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

    public static NfcDeviceFragment newInstance() {
        NfcDeviceFragment fragment = new NfcDeviceFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nfc_device_operation, container, false);
        ButterKnife.bind(this, view);

        ((AppCompatTextView) cellTitleLockView.findViewById(R.id.title)).setText(getString(R.string.card_title_lock));
        ((ImageView) cellTitleLockView.findViewById(R.id.background)).setImageResource(R.drawable.cell_lock);
        cellTitleLockView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.amber_a100_mask));

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

                //服务器访问上封操作
                serviceOerationLock();

                operationSealSwitch = STATE_OPERATION_INITIAL;
            }
        });

        ((AppCompatTextView) cellTitleUnlockView.findViewById(R.id.title)).setText(getString(R.string.card_title_unlock));
        ((ImageView) cellTitleUnlockView.findViewById(R.id.background)).setImageResource(R.drawable.cell_unlock);
        cellTitleUnlockView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green_mask));

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

                //服务器访问解封操作
                serviceOerationUnlock();

                operationSealSwitch = STATE_OPERATION_INITIAL;
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter filter = new IntentFilter();
        filter.addAction(TakePictueActivity.PICTURE_URI);

        localBroadcastManager.registerReceiver(broadcastReceiver, filter);

        mNfcUtility = new NfcUtility(tagCallback);
    }

    @Override
    public void onStart() {
        super.onStart();
        locationService = ((App) (getActivity().getApplication())).locationService;
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
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
    }

    @OnClick(R.id.card_seting)
    public void onSettingBtnClick() {
//        getDeviceId(); //获取设备ID
        operationSealSwitch = STATE_OPERATION_SETTING; //配置

        locationService.requestLocation(getContext());// 定位SDK
        isLocationServiceStarting = true;

        //配置信息
        Intent intent = new Intent(getContext(), DeviceSettingActivity.class);
        intent.putExtra(DeviceSettingActivity.IS_CONFIG_BLE_DEVICE, false);
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
        locationService.requestLocation(getContext());// 定位SDK
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
        locationService.requestLocation(getContext());// 定位SDK
        isLocationServiceStarting = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DEVICE_SETTING && resultCode == Activity.RESULT_OK) {
            final SettingType settingType = data.getExtras().getParcelable(SettingType.EXTRA_DEVICE);
            Log.d(TAG, "onActivityResult() returned: settingType = " + settingType.toString());

            String imageResourceStr = data.getExtras().getString(TakePictueActivity.PICTURE_URI);

            imageResourceStr = "";
            serviceOerationSetting(settingType, coordinateSetting, imageResourceStr);

            operationSealSwitch = STATE_OPERATION_INITIAL;
        }
    }

    private void performTakePictureWithTransition(View v) {
        Activity activity = getActivity();

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
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (nfcAdapter != null) {
            if (nfcAdapter.isEnabled()) {
                nfcAdapter.enableReaderMode(getActivity(), mNfcUtility, NfcUtility.NFC_TAG_FLAGS, null);
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
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(getActivity());
        }
    }

    private NfcUtility.TagCallback tagCallback = new NfcUtility.TagCallback() {
        @Override
        public void onTagReceived(final String tag) {
            App.setTagId(tag);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

            getActivity().runOnUiThread(new Runnable() {
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
                    .configureDeviceObservable(token, Api.DEVICE_TYPE_BEIDOU_NFC, "",
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
            String tagId = "043B88F2994080";
            if (!TextUtils.equals(lockNfcId.getText(), getString(R.string.text_hint_get_nfc_tag))) {
                tagId = lockNfcId.getText().toString();
            }

            imgUrl = "";

            RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST)
                    .closeDeviceObservable(token, "", tagId, imgUrl, coordinate, operateTime)
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
            String tagId = "043B88F2994080";
            if (!TextUtils.equals(unlockNfcId.getText(), getString(R.string.text_hint_get_nfc_tag))) {
                tagId = unlockNfcId.getText().toString();
            }

            imgUrl = "";

            RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST)
                    .openDeviceObservable(token, "", tagId, imgUrl, coordinate, operateTime)
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

/*    private void setImageResourceBase64(ImageView imageView) {
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
    }*/

    private void showAlertDialog(final String title, final String msg, @Nullable final DialogInterface.OnClickListener listener) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getContext())
                        .setTitle(title)
                        .setMessage(msg)
                        .setPositiveButton(R.string.text_ok, listener).show();
            }
        });
    }

    private void showSnackbar(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar snackbar = Snackbar.make(foldingCellLock, msg, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null);
                ((TextView) (snackbar.getView().findViewById(R.id.snackbar_text))).setTextColor(ContextCompat.getColor(getContext(), R.color.blue_grey_100));
                snackbar.show();
            }
        });
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
