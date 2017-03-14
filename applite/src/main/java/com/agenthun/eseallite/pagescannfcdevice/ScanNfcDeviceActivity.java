package com.agenthun.eseallite.pagescannfcdevice;

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
import com.agenthun.eseallite.pageabout.AboutActivity;
import com.agenthun.eseallite.pagelogin.LoginActivity;
import com.agenthun.eseallite.utils.ActivityUtils;
import com.agenthun.eseallite.utils.PreferencesHelper;
import com.agenthun.eseallite.utils.scheduler.SchedulerProvider;
import com.pekingopera.versionupdate.UpdateHelper;
import com.pekingopera.versionupdate.listener.ForceListener;

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

        checkUpdate();

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

        new ScanNfcDevicePresenter(
                getApplicationContext(),
                fragment,
                SchedulerProvider.getInstance());
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

        switch (id) {
            case R.id.action_sign_out:
                signOut(true);
                return true;
            case R.id.action_about:
                AboutActivity.start(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOut(boolean isSave) {
        PreferencesHelper.signOut(this, isSave);
        LoginActivity.start(this, isSave);
        ActivityCompat.finishAfterTransition(this);
    }

    private void checkUpdate() {
        UpdateHelper.getInstance().setForceListener(new ForceListener() {
            @Override
            public void onUserCancel(boolean force) {
                if (force) {
                    finish();
                }
            }
        }).check(this);
    }
}
