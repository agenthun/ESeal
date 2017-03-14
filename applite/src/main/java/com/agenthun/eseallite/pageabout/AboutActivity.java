package com.agenthun.eseallite.pageabout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.agenthun.eseallite.R;
import com.agenthun.eseallite.utils.ActivityUtils;
import com.agenthun.eseallite.utils.scheduler.SchedulerProvider;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/15 10:53.
 */

public class AboutActivity extends AppCompatActivity {
    private static final String TAG = "AboutActivity";

    public static void start(Context context) {
        Intent starter = new Intent(context, AboutActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toolbar_frame);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(R.string.about);
        setSupportActionBar(toolbar);

        attachDeviceFragment();

        supportPostponeEnterTransition();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityCompat.finishAfterTransition(this);
    }

    private void attachDeviceFragment() {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        AboutFragment fragment = (AboutFragment) supportFragmentManager.findFragmentById(R.id.content_main);
        if (fragment == null) {
            fragment = AboutFragment.newInstance();
            ActivityUtils.replaceFragmentToActivity(supportFragmentManager, fragment, R.id.content_main);
        }

        new AboutPresenter(getApplicationContext(),
                fragment,
                SchedulerProvider.getInstance());
    }
}
