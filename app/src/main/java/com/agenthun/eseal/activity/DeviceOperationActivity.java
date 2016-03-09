package com.agenthun.eseal.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.agenthun.eseal.R;
import com.agenthun.eseal.bean.base.DetailParcelable;
import com.agenthun.eseal.connectivity.ble.ACSUtility;
import com.agenthun.eseal.connectivity.manager.RetrofitManager;

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
    private ACSUtility.blePort mCurrentPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_operation);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        BluetoothDevice device = bundle.getParcelable(BluetoothDevice.EXTRA_DEVICE);
//        mCurrentPort = util.new blePort(device);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(device.getName());
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @OnClick(R.id.card_seting)
    public void onSettingBtnClick() {
        Intent intent = new Intent(DeviceOperationActivity.this, DeviceSettingActivity.class);
        startActivityForResult(intent, DEVICE_SETTING);
    }

    @OnClick(R.id.card_lock)
    public void onLockBtnClick() {
        Log.d(TAG, "onLockBtnClick() returned: ");
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
}
