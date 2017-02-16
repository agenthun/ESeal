package com.agenthun.eseallite.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;


import com.agenthun.eseallite.R;
import com.agenthun.eseallite.fragment.TimePickerFragment;
import com.agenthun.eseallite.utils.ActivityUtils;
import com.agenthun.eseallite.utils.DeviceSearchSuggestion;

import butterknife.ButterKnife;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/15 10:53.
 */

public class TimePickerActivity extends AppCompatActivity {
    private static final String TAG = "TimePickerActivity";

    public static final int REQUEST_PICK_TIME = 0x13;
    public static final int RESULT_PICK_TIME = 0x14;

    public static final String PICK_TIME_FROM = "PICK_TIME_FROM";
    public static final String PICK_TIME_TO = "PICK_TIME_TO";

    private DeviceSearchSuggestion mFreight = null;

    public static Intent getStartIntent(Context context) {
        Intent starter = new Intent(context, TimePickerActivity.class);
        return starter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toolbar_frame);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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
        TimePickerFragment fragment = (TimePickerFragment) supportFragmentManager.findFragmentById(R.id.content_main);
        if (fragment == null) {
            fragment = TimePickerFragment.newInstance(mPickTimeListener);
            ActivityUtils.replaceFragmentToActivity(supportFragmentManager, fragment, R.id.content_main);
        }
    }

    private TimePickerFragment.PickTimeListener mPickTimeListener = new TimePickerFragment.PickTimeListener() {
        @Override
        public void onTimePicked(String from, String to) {
            setResultPickTime(from, to);
            ActivityCompat.finishAfterTransition(TimePickerActivity.this);
        }
    };

    private void setResultPickTime(String from, String to) {
        Intent timeIntent = new Intent();
        timeIntent.putExtra(PICK_TIME_FROM, from);
        timeIntent.putExtra(PICK_TIME_TO, to);
        setResult(RESULT_PICK_TIME, timeIntent);
    }
}
