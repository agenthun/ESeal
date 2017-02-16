package com.agenthun.eseallite.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.agenthun.eseallite.R;
import com.agenthun.eseallite.fragment.ScanNfcDeviceFragment;
import com.agenthun.eseallite.utils.ActivityUtils;
import com.agenthun.eseallite.utils.PreferencesHelper;

import butterknife.ButterKnife;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/14 16:49.
 */

public class ScanNfcDeviceActivity extends AppCompatActivity {

    private static final String TAG = "ScanNfcDeviceActivity";

    public static void start(Context context) {
        Intent starter = new Intent(context, ScanNfcDeviceActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toolbar_frame);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        attachDeviceFragment();

        supportPostponeEnterTransition();
    }

    private void attachDeviceFragment() {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        ScanNfcDeviceFragment fragment = (ScanNfcDeviceFragment) supportFragmentManager.findFragmentById(R.id.content_main);
        if (fragment == null) {
            fragment = ScanNfcDeviceFragment.newInstance();
            ActivityUtils.replaceFragmentToActivity(supportFragmentManager, fragment, R.id.content_main);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sign_out) {
            signOut(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOut(boolean isSave) {
        PreferencesHelper.signOut(this, isSave);
        LoginActivity.start(this, isSave);
        ActivityCompat.finishAfterTransition(this);
    }
}
