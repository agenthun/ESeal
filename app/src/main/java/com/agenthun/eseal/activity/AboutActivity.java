package com.agenthun.eseal.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.agenthun.eseal.R;
import com.agenthun.eseal.fragment.AboutFragment;
import com.agenthun.eseal.utils.ActivityUtils;

import butterknife.ButterKnife;

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

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(R.string.about);
        setSupportActionBar(toolbar);
/*        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });*/

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
    }
}
