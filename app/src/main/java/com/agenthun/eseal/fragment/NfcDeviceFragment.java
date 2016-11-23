package com.agenthun.eseal.fragment;

import android.annotation.TargetApi;
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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/7 上午5:47.
 */
public class NfcDeviceFragment extends Fragment {

    private static final String TAG = "NfcDeviceFragment";

    private NfcUtility mNfcUtility;
    private Uri pictureUri = null;

    private LocationService locationService;

    @Bind(R.id.card_add_picture)
    View addPicture;

    @Bind(R.id.picturePreview)
    ImageView picturePreview;

    @Bind(R.id.NfcId)
    AppCompatTextView NfcIdTextView;

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
        mOption.setCoorType("bd09ll");
        locationService.setLocationOption(mOption);
//        locationService.setLocationOption(locationService.getOption());
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

    @OnClick(R.id.card_lock)
    public void onLockBtnClick() {
//        Log.d(TAG, "onLockBtnClick() returned: ");
        //发送上封操作报文
        locationService.start();// 定位SDK
    }

    @OnClick(R.id.card_unlock)
    public void onUnlockBtnClick() {
//        Log.d(TAG, "onUnlockBtnClick() returned: ");
        //发送解封操作报文
//        ByteBuffer buffer = ByteBuffer.allocate(10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION);
//        buffer.putInt(id);
//        buffer.putInt(rn);
//        buffer.putShort(ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION);
//        buffer.put(ESealOperation.operationOperation(id, rn, key,
//                ESealOperation.POWER_ON,
//                ESealOperation.SAFE_UNLOCK)
//        );
//
//        SocketPackage socketPackage = new SocketPackage();
//        byte[] data = socketPackage.packageAddHeader(ESealOperation.ESEALBD_OPERATION_PORT,
//                10 + ESealOperation.ESEALBD_OPERATION_REQUEST_SIZE_OPERATION,
//                buffer.array()
//        );
//        sendData(data);
    }

    @OnClick(R.id.card_scan_nfc)
    public void onScanNfcBtnClick() {
//        Log.d(TAG, "onScanNfcBtnClick() returned: ");
        //扫描NFC封条,获取ID
        showAlertDialog(getString(R.string.text_title_hint),
                getString(R.string.text_hint_close_to_nfc_tag),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        enableNfcReaderMode();
                    }
                });
        ViewCompat.animate(NfcIdTextView).alpha(0)
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
    }

    @OnClick(R.id.card_add_picture)
    public void onAddPictureBtnClick() {
//        Log.d(TAG, "onAddPictureBtnClick() returned: ");
        //设备拍照
        performTakePictureWithTransition(addPicture);
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
                Snackbar.make(NfcIdTextView, getString(R.string.error_nfc_not_open), Snackbar.LENGTH_SHORT)
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
                                    NfcIdTextView.setText(tag);
                                    ViewCompat.animate(NfcIdTextView).alpha(1)
                                            .setDuration(100)
                                            .setStartDelay(200)
                                            .setInterpolator(new LinearOutSlowInInterpolator())
                                            .start();
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
                    picturePreview.setImageURI(pictureUri);
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

    private void showSnackbar(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(addPicture, msg, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
    }

    /*****
     * @see copy funtion to you project
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     */
    private BDLocationListener mListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                StringBuffer sb = new StringBuffer(256);
                sb.append("time : ");
                /**
                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */
                sb.append(location.getTime());
                sb.append("\nlocType : ");// 定位类型
                sb.append(location.getLocType());
                sb.append("\nlocType description : ");// *****对应的定位类型说明*****
                sb.append(location.getLocTypeDescription());
                sb.append("\nlatitude : ");// 纬度
                sb.append(location.getLatitude());
                sb.append("\nlontitude : ");// 经度
                sb.append(location.getLongitude());
                sb.append("\nradius : ");// 半径
                sb.append(location.getRadius());
                sb.append("\nCountryCode : ");// 国家码
                sb.append(location.getCountryCode());
                sb.append("\nCountry : ");// 国家名称
                sb.append(location.getCountry());
                sb.append("\ncitycode : ");// 城市编码
                sb.append(location.getCityCode());
                sb.append("\ncity : ");// 城市
                sb.append(location.getCity());
                sb.append("\nDistrict : ");// 区
                sb.append(location.getDistrict());
                sb.append("\nStreet : ");// 街道
                sb.append(location.getStreet());
                sb.append("\naddr : ");// 地址信息
                sb.append(location.getAddrStr());
                sb.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****
                sb.append(location.getUserIndoorState());
                sb.append("\nDirection(not all devices have value): ");
                sb.append(location.getDirection());// 方向
                sb.append("\nlocationdescribe: ");
                sb.append(location.getLocationDescribe());// 位置语义化信息
                sb.append("\nPoi: ");// POI信息
                if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                    for (int i = 0; i < location.getPoiList().size(); i++) {
                        Poi poi = (Poi) location.getPoiList().get(i);
                        sb.append(poi.getName() + ";");
                    }
                }
                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append("\nspeed : ");
                    sb.append(location.getSpeed());// 速度 单位：km/h
                    sb.append("\nsatellite : ");
                    sb.append(location.getSatelliteNumber());// 卫星数目
                    sb.append("\nheight : ");
                    sb.append(location.getAltitude());// 海拔高度 单位：米
                    sb.append("\ngps status : ");
                    sb.append(location.getGpsAccuracyStatus());// *****gps质量判断*****
                    sb.append("\ndescribe : ");
                    sb.append("gps定位成功");
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    // 运营商信息
                    if (location.hasAltitude()) {// *****如果有海拔高度*****
                        sb.append("\nheight : ");
                        sb.append(location.getAltitude());// 单位：米
                    }
                    sb.append("\noperationers : ");// 运营商信息
                    sb.append(location.getOperators());
                    sb.append("\ndescribe : ");
                    sb.append("网络定位成功");
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("\ndescribe : ");
                    sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    sb.append("\ndescribe : ");
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    sb.append("\ndescribe : ");
                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    sb.append("\ndescribe : ");
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
                showAlertDialog("定位", sb.toString(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        locationService.stop();
                    }
                });
            }
        }
    };
}
