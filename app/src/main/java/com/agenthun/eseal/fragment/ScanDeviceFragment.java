package com.agenthun.eseal.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.agenthun.eseal.R;
import com.agenthun.eseal.activity.DeviceOperationActivity;
import com.agenthun.eseal.adapter.DeviceAdapter;
import com.agenthun.eseal.connectivity.ble.ACSUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.gmariotti.recyclerview.itemanimator.SlideScaleInOutRightItemAnimator;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/7 上午5:47.
 */
public class ScanDeviceFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ScanDeviceFragment";
    private static final String ARG_TYPE = "TYPE";
    private static final String ARG_CONTAINER_NO = "CONTAINER_NO";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQEEST_ENUM_PORTS = 10;
    private static final long SCAN_PERIOD = 10000;

    private String mType;
    private String mContainerNo;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private DeviceAdapter deviceAdapter;

    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    Map<String, Integer> deviceRssiValues = new HashMap<>();

    private ACSUtility.blePort mSelectedPort;
    private boolean utilAvaliable;
    private boolean utilIsScan = false;
    private ACSUtility utility;

    private AppCompatDialog mProgressDialog;

    public static ScanDeviceFragment newInstance(String type, String containerNo) {
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_CONTAINER_NO, containerNo);
        ScanDeviceFragment fragment = new ScanDeviceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getString(ARG_TYPE);
            mContainerNo = getArguments().getString(ARG_CONTAINER_NO);
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_device, container, false);

        utility = new ACSUtility(getContext(), callback);
        utilAvaliable = true;

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary, R.color.colorPrimaryDark,
                R.color.colorAccent, R.color.colorAccentDark);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        deviceAdapter = new DeviceAdapter(deviceList, deviceRssiValues);
        deviceAdapter.setOnItemClickListener(new DeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (utilIsScan) {
                    utilIsScan = false;
                    swipeRefreshLayout.setRefreshing(false);
                    utility.stopEnum();
                }

                BluetoothDevice device = deviceAdapter.getItem(position);
                Log.d(TAG, "onItemClick() returned: " + device.getName());
                mSelectedPort = utility.new blePort(device);
                if (mSelectedPort != null) {
                    swipeRefreshLayout.setRefreshing(false); //bug
                    getProgressDialog().show();
                    utility.openPort(mSelectedPort);
                }
//                startProductActivityWithTransition(ShoppingActivity.this, view.findViewById(R.id.pic), mBook);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(deviceAdapter);
        recyclerView.setItemAnimator(new SlideScaleInOutRightItemAnimator(recyclerView));
        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (utilAvaliable) {
            utilAvaliable = false;
            utility.closeACSUtility();
        }
    }

    @Override
    public void onRefresh() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanDevice();
        } else {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            BluetoothManager bluetoothManager = (BluetoothManager) getContext()
                    .getSystemService(getContext().BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                swipeRefreshLayout.setRefreshing(false);
                Snackbar.make(swipeRefreshLayout, getString(R.string.error_ble_not_supported), Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mBluetoothAdapter.isEnabled()) {
                        scanDevice();
                    }
                }
            }, 5000);
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            utilIsScan = false;
            swipeRefreshLayout.setRefreshing(false);
            utility.stopEnum();
        }/* else if (requestCode == REQEEST_ENUM_PORTS
                && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();

            BluetoothDevice device = bundle.getParcelable(BluetoothDevice.EXTRA_DEVICE);
            mSelectedPort = utility.new blePort(device);
        }*/
    }

    private void scanDevice() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                utilIsScan = false;
                swipeRefreshLayout.setRefreshing(false);
                utility.stopEnum();
            }
        }, SCAN_PERIOD);

        deviceAdapter.clear();
        deviceAdapter.notifyDataSetChanged();
        utilIsScan = true;
        utility.enumAllPorts(10);
    }

    private void addDevice(BluetoothDevice device, int rssi) {
        boolean isFound = false;
        for (BluetoothDevice deviceFound : deviceList) {
            if (deviceFound.getAddress().equals(device.getAddress())) {
                isFound = true;
                break;
            }
        }
        deviceRssiValues.put(device.getAddress(), rssi);
        if (!isFound) {
            deviceList.add(device);
        }
//        deviceAdapter.notifyDataSetChanged();
        deviceAdapter.notifyItemInserted(deviceList.size());
    }

    private ACSUtility.IACSUtilityCallback callback = new ACSUtility.IACSUtilityCallback() {
        @Override
        public void utilReadyForUse() {
            utilAvaliable = true;
            utility.enumAllPorts(10);
        }

        @Override
        public void didFoundPort(final ACSUtility.blePort newPort, final int rssi) {
            final ACSUtility.blePort mNewtPort = newPort;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "didFoundPort() returned: " + mNewtPort._device.getName() + ", " + mNewtPort._device + ", rssi=" + rssi);
                            addDevice(mNewtPort._device, rssi);
                        }
                    });
                }
            });
        }

        @Override
        public void didFinishedEnumPorts() {
            if (deviceList.size() == 0) {
                return;
            }
        }

        @Override
        public void didOpenPort(final ACSUtility.blePort port, Boolean bSuccess) {
            Log.d(TAG, "didOpenPort() returned: " + bSuccess);
            if (bSuccess) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getProgressDialog().cancel();

                        Bundle b = new Bundle();
                        b.putParcelable(BluetoothDevice.EXTRA_DEVICE, port._device);
                        Intent intent = new Intent(getContext(), DeviceOperationActivity.class);
                        intent.putExtras(b);
                        startActivity(intent);

                        //sendData();
                    }
                });
            } else {
                getProgressDialog().cancel();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(port._device.getName()).setMessage(R.string.fail_device_connection).setPositiveButton(R.string.text_ok, null).show();
            }
        }

        @Override
        public void didClosePort(ACSUtility.blePort port) {

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

    private void sendData() {
        StringBuffer stringBuffer = new StringBuffer("test12345678");
        byte[] bytes = hexStringToByte(stringBuffer.toString());
        for (int i = 0; i < bytes.length; i++) {
            Log.d(TAG, "bytes[" + i + "] = " + bytes[i]);
        }
        utility.writePort(bytes);
    }

    private byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    private AppCompatDialog getProgressDialog() {
        if (mProgressDialog != null) {
            return mProgressDialog;
        }
        mProgressDialog = new AppCompatDialog(getContext(), AppCompatDelegate.MODE_NIGHT_AUTO);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setContentView(R.layout.dialog_device_connecting);
        mProgressDialog.setTitle(getString(R.string.device_connecting));
        return mProgressDialog;
    }
}
