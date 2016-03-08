package com.agenthun.eseal.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.adapter.DeviceAdapter;
import com.agenthun.eseal.bean.FreightInfosByToken;
import com.agenthun.eseal.bean.base.Detail;
import com.agenthun.eseal.connectivity.ble.ACSUtility;
import com.agenthun.eseal.connectivity.manager.RetrofitManager;
import com.agenthun.eseal.connectivity.service.PathType;
import com.agenthun.eseal.utils.ContainerNoSuggestion;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.view.BodyTextView;
import com.arlib.floatingsearchview.util.view.IconImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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

    private ACSUtility.blePort mNewtPort, mSelectedPort;
    private boolean utilAvaliable;
    private ACSUtility utility;

    private ACSUtility.IACSUtilityCallback callback = new ACSUtility.IACSUtilityCallback() {
        @Override
        public void utilReadyForUse() {
            utilAvaliable = true;
            utility.enumAllPorts(10);
        }

        @Override
        public void didFoundPort(final ACSUtility.blePort newPort, final int rssi) {
            mNewtPort = newPort;
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
        public void didOpenPort(ACSUtility.blePort port, Boolean bSuccess) {

        }

        @Override
        public void didClosePort(ACSUtility.blePort port) {

        }

        @Override
        public void didPackageSended(boolean succeed) {

        }

        @Override
        public void didPackageReceived(ACSUtility.blePort port, byte[] packageToSend) {

        }

        @Override
        public void heartbeatDebug() {

        }
    };

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
        deviceAdapter.notifyDataSetChanged();
    }

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

        final BluetoothManager bluetoothManager = (BluetoothManager) getContext()
                .getSystemService(getContext().BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null | !mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getContext(), "该设备不支持蓝牙功能", Toast.LENGTH_SHORT).show();
        }

        utility = new ACSUtility(getContext(), callback);


        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary, R.color.colorPrimaryDark,
                R.color.colorAccent, R.color.colorAccentDark);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        deviceAdapter = new DeviceAdapter(deviceList, deviceRssiValues);
/*        deviceAdapter.setOnItemClickListener(new ShoppingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final View view, int position) {
                Log.d(TAG, "onItemClick() returned: " + position);
*//*                final Book mBook = shoppingAdapter.getItem(position);
                if (TextUtils.isEmpty(mBook.getContent()) && TextUtils.isEmpty(mBook.getSummary())) {
                    getSearchByIdRequestDataAndStartActivity(position, mBook.getId(), mBook);
                    Log.d(TAG, "onItemClick() returned: getSearchByIdRequestDataAndStartActivity");
                } else {
                    startProductActivityWithTransition(ShoppingActivity.this, view.findViewById(R.id.pic), mBook);
                }*//*
            }
        });*/

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(deviceAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView()");
        if (utilAvaliable) {
            utility.stopEnum();
            utility.closeACSUtility();
        }
    }

    @Override
    public void onRefresh() {
//        utilAvaliable = false;
/*        Intent intent = new Intent();
        startActivityForResult(intent, REQEEST_ENUM_PORTS);*/

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                utilAvaliable = false;
                swipeRefreshLayout.setRefreshing(false);
                utility.stopEnum();
            }
        }, SCAN_PERIOD);

        deviceAdapter.clear();
        deviceAdapter.notifyDataSetChanged();
        utilAvaliable = true;
        utility.enumAllPorts(10);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT
                && resultCode == Activity.RESULT_CANCELED) {
            return;
        } else if (requestCode == REQEEST_ENUM_PORTS
                && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();

            BluetoothDevice device = bundle.getParcelable(BluetoothDevice.EXTRA_DEVICE);
            mSelectedPort = utility.new blePort(device);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
