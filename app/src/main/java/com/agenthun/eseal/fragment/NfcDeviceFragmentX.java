package com.agenthun.eseal.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.activity.TakePictueActivity;
import com.agenthun.eseal.connectivity.nfc.NfcUtility;
import com.agenthun.eseal.utils.ApiLevelHelper;
import com.agenthun.eseal.utils.baidumap.LocationService;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.ramotion.foldingcell.FoldingCell;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/7 上午5:47.
 */
public class NfcDeviceFragmentX extends Fragment {

    private static final String TAG = "NfcDeviceFragment";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private NfcUtility mNfcUtility;
    private Uri pictureUri = null;

    private LocationService locationService;

/*    @Bind(R.id.card_add_picture)
    View addPicture;

    @Bind(R.id.picturePreview)
    ImageView picturePreview;*/

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

    private AppCompatTextView lockTime;
    private AppCompatTextView lockLocation;
    private AppCompatTextView lockNfcId;

    private AppCompatTextView unlockTime;
    private AppCompatTextView unlockLocation;
    private AppCompatTextView unlockNfcId;

    //-1:初始化状态; 0:上封; 1:解封
    private int operationSealSwitch = -1;
    private boolean isLocationServiceStarting = false;

    public static NfcDeviceFragmentX newInstance() {
        NfcDeviceFragmentX fragment = new NfcDeviceFragmentX();
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
        lockTime = (AppCompatTextView) cellContentLockView.findViewById(R.id.time);
        lockLocation = (AppCompatTextView) cellContentLockView.findViewById(R.id.location);
        lockNfcId = (AppCompatTextView) cellContentLockView.findViewById(R.id.nfc_id);
        lockNfcId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableNfcReaderMode();
                Snackbar.make(lockNfcId, getString(R.string.text_hint_close_to_nfc_tag), Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
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
                operationSealSwitch = -1;
            }
        });

        ((AppCompatTextView) cellTitleUnlockView.findViewById(R.id.title)).setText(getString(R.string.card_title_unlock));
        ((ImageView) cellTitleUnlockView.findViewById(R.id.background)).setImageResource(R.drawable.cell_unlock);
        cellTitleUnlockView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green_mask));
        ((AppCompatTextView) cellContentUnlockView.findViewById(R.id.title)).setText(getString(R.string.text_hint_unlock_operation));
        unlockTime = (AppCompatTextView) cellContentUnlockView.findViewById(R.id.time);
        unlockLocation = (AppCompatTextView) cellContentUnlockView.findViewById(R.id.location);
        unlockNfcId = (AppCompatTextView) cellContentUnlockView.findViewById(R.id.nfc_id);
        unlockNfcId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableNfcReaderMode();
                Snackbar.make(unlockNfcId, getString(R.string.text_hint_close_to_nfc_tag), Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
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
                operationSealSwitch = -1;
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

/*    @OnClick(R.id.card_add_picture)
    public void onAddPictureBtnClick() {
//        Log.d(TAG, "onAddPictureBtnClick() returned: ");
        //设备拍照
        performTakePictureWithTransition(addPicture);
    }*/

    @OnClick(R.id.cell_title_lock)
    public void onFoldingCellLockBtnClick() {
        if (operationSealSwitch == 1) {
            foldingCellUnlock.toggle(true);
        }
        operationSealSwitch = 0; //上封

        foldingCellLock.toggle(false);
        String operateTime = DATE_FORMAT.format(Calendar.getInstance().getTime());
        lockTime.setText(operateTime);
        lockNfcId.setText(getString(R.string.text_hint_get_nfc_tag));

        if (isLocationServiceStarting) {
            locationService.stop();
        }
        locationService.start();// 定位SDK
        isLocationServiceStarting = true;
    }

    @OnClick(R.id.cell_title_unlock)
    public void onFoldingCellUnlockBtnClick() {
        if (operationSealSwitch == 0) {
            foldingCellLock.toggle(true);
        }
        operationSealSwitch = 1; //解封

        foldingCellUnlock.toggle(false);
        String operateTime = DATE_FORMAT.format(Calendar.getInstance().getTime());
        unlockTime.setText(operateTime);
        unlockNfcId.setText(getString(R.string.text_hint_get_nfc_tag));

        if (isLocationServiceStarting) {
            locationService.stop();
        }
        locationService.start();// 定位SDK
        isLocationServiceStarting = true;
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
                                        case 0: //上封
                                            lockNfcId.setText(tag);
                                            break;
                                        case 1: //解封
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
//                    picturePreview.setImageURI(pictureUri);
                }
            });
        }
    };

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

/*    private void showSnackbar(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(addPicture, msg, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
    }*/

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
                    case 0: //上封
                        lockTime.setText(time);
                        lockLocation.setText(address);
                        break;
                    case 1: //解封
                        unlockTime.setText(time);
                        unlockLocation.setText(address);
                        break;
                }
            } else if (null != location && location.getLocType() == BDLocation.TypeServerError) {
                lockLocation.setText(getString(R.string.fail_get_current_location));
            } else if (null != location && location.getLocType() == BDLocation.TypeNetWorkException) {
                lockLocation.setText(getString(R.string.fail_get_current_location));
            } else if (null != location && location.getLocType() == BDLocation.TypeCriteriaException) {
                lockLocation.setText(getString(R.string.fail_get_current_location));
            }

            locationService.stop();
            isLocationServiceStarting = false;
        }
    };
}
