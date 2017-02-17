package com.agenthun.eseallite.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.agenthun.eseallite.R;
import com.agenthun.eseallite.fragment.FreightTrackGoogleMapFragment;
import com.agenthun.eseallite.fragment.FreightTrackMapFragment;
import com.agenthun.eseallite.utils.ActivityUtils;
import com.agenthun.eseallite.utils.DeviceSearchSuggestion;
import com.baidu.mapapi.SDKInitializer;

import butterknife.ButterKnife;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/15 10:53.
 */

public class FreightTrackMapActivity extends AppCompatActivity {
    private static final String TAG = "FreightTrackMapActivity";

    private static final String EXTRA_FREIGHT = "freight";

    private DeviceSearchSuggestion mFreight = null;

    public static void start(Context context, DeviceSearchSuggestion deviceSearchSuggestion) {
        Intent starter = new Intent(context, FreightTrackMapActivity.class);
        starter.putExtra(EXTRA_FREIGHT, deviceSearchSuggestion);
        context.startActivity(starter);
    }

    public static void start(Context context, String id, Integer type, String name) {
        Intent starter = new Intent(context, FreightTrackMapActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化百度地图API
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_toolbar_frame_fab);

        ButterKnife.bind(this);

        mFreight = getIntent().getParcelableExtra(EXTRA_FREIGHT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mFreight.getName());
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

//        attachDeviceFragment();
        attachDeviceGoogleMapFragment(); //test GoogleMap

        supportPostponeEnterTransition();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ScanNfcDeviceActivity.start(this);
        ActivityCompat.finishAfterTransition(this);
    }

    private void attachDeviceFragment() {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FreightTrackMapFragment fragment = (FreightTrackMapFragment) supportFragmentManager.findFragmentById(R.id.content_main);
        if (fragment == null) {
            fragment = FreightTrackMapFragment.newInstance(mFreight.getId(), mFreight.getName());
            ActivityUtils.replaceFragmentToActivity(supportFragmentManager, fragment, R.id.content_main);
        }
    }

    private void attachDeviceGoogleMapFragment() {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FreightTrackGoogleMapFragment fragment = (FreightTrackGoogleMapFragment) supportFragmentManager.findFragmentById(R.id.content_main);
        if (fragment == null) {
            fragment = FreightTrackGoogleMapFragment.newInstance(mFreight.getId(), mFreight.getName());
            ActivityUtils.replaceFragmentToActivity(supportFragmentManager, fragment, R.id.content_main);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_freight_track_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_main);
        if (fragment != null) {
            return fragment.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_main);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
