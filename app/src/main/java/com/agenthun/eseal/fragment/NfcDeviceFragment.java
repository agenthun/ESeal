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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.activity.DeviceOperationActivity;
import com.agenthun.eseal.activity.TakePictueActivity;
import com.agenthun.eseal.connectivity.nfc.NfcUtility;
import com.agenthun.eseal.utils.ApiLevelHelper;
import com.agenthun.eseal.utils.LocationUtil;

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
//    private static final String ARG_CONTAINER_NO = "CONTAINER_NO";
//    private static final String ARG_TYPE = "TYPE";
//    private String mType;
//    private String mContainerNo;

    private NfcUtility mNfcUtility;
    private Uri pictureUri = null;

    @Bind(R.id.card_add_picture)
    View addPicture;

    @Bind(R.id.picturePreview)
    ImageView picturePreview;

    @Bind(R.id.NfcId)
    AppCompatTextView NfcIdTextView;

    public static NfcDeviceFragment newInstance() {
        Bundle args = new Bundle();
//        args.putString(ARG_TYPE, type);
//        args.putString(ARG_CONTAINER_NO, containerNo);
        NfcDeviceFragment fragment = new NfcDeviceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mType = getArguments().getString(ARG_TYPE);
//            mContainerNo = getArguments().getString(ARG_CONTAINER_NO);
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nfc_device_operation, container, false);
        ButterKnife.bind(this, view);

        mNfcUtility = new NfcUtility(tagCallback);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter filter = new IntentFilter();
        filter.addAction(TakePictueActivity.PICTURE_URI);

        localBroadcastManager.registerReceiver(broadcastReceiver, filter);
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
        double[] gps = LocationUtil.getLocation(getContext());
        showSnackbar(gps[0] + ", " + gps[1]);
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
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
}
