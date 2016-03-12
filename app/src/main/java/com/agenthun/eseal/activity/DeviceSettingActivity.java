package com.agenthun.eseal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.agenthun.eseal.R;
import com.agenthun.eseal.bean.base.DetailParcelable;
import com.agenthun.eseal.view.CheckableFab;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/9 下午10:55.
 */
public class DeviceSettingActivity extends AppCompatActivity {
    private static final String TAG = "DeviceSettingActivity";
    public static final String RESULT_CONFIGURE = "result_configure";

    @Bind(R.id.container_number)
    AppCompatEditText containerNumber;

    @Bind(R.id.owner)
    AppCompatEditText owner;

    @Bind(R.id.freight_name)
    AppCompatEditText freightName;

    @Bind(R.id.origin)
    AppCompatEditText origin;

    @Bind(R.id.destination)
    AppCompatEditText destination;

    @Bind(R.id.vessel)
    AppCompatEditText vessel;

    @Bind(R.id.voyage)
    AppCompatEditText voyage;

    @Bind(R.id.frequency)
    AppCompatEditText frequency;

    @Bind(R.id.fab)
    CheckableFab fab;

    private Handler mHandler = new Handler();
    private Runnable mHideFabRunnable;

    private boolean mEdited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_setting);
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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ismEdited()) {
                    Log.d(TAG, "ismEdited onClick() returned: ");
                }
                adjustFab(false);
            }
        });

        containerNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(containerNumber.getText())) {
                    allowSubmit(true);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        fab.hide();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void adjustFab(final boolean settingCorrect) {
        fab.setChecked(settingCorrect);
        mHideFabRunnable = new Runnable() {
            @Override
            public void run() {
                fab.hide();
                if (!settingCorrect) {
                    getConfigure();
                    onBackPressed();
                }
            }
        };
        mHandler.postDelayed(mHideFabRunnable, 500);
    }

    protected void allowSubmit(boolean edited) {
        if (null != fab) {
            if (edited) {
                fab.show();
            } else {
                fab.hide();
            }
            mEdited = edited;
        }
    }

    public boolean ismEdited() {
        return mEdited;
    }

    private void getConfigure() {
        Intent data = new Intent();
        Bundle b = new Bundle();
        DetailParcelable detail = new DetailParcelable();
        detail.setContainerNo(containerNumber.getText().toString().trim());
        detail.setOperationer(owner.getText().toString().trim()); //xxxxxx
        detail.setFreightName(freightName.getText().toString().trim());
        detail.setOrigin(origin.getText().toString().trim());
//        detail.setPositionName(destination.getText().toString().trim());
//        detail.setXXX(vessel.getText().toString().trim());
//        detail.setContainerNo(voyage.getText().toString().trim());
        detail.setFrequency(frequency.getText().toString().trim());
        b.putParcelable(DetailParcelable.EXTRA_DEVICE, detail);

        data.putExtras(b);
        setResult(RESULT_OK, data);
    }
}
