package com.agenthun.eseallite.pagelogin;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.agenthun.eseallite.R;
import com.agenthun.eseallite.utils.ActivityUtils;
import com.agenthun.eseallite.utils.scheduler.SchedulerProvider;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private static final String EXTRA_SAVE_PREFERENCES = "SAVE";
    private static final boolean ARG_SAVE_DEFAULT = false;

    private final int SDK_PERMISSION_REQUEST = 127;

    public static void start(Activity activity, Boolean isSavePreferences) {
        Intent starter = new Intent(activity, LoginActivity.class);
        starter.putExtra(EXTRA_SAVE_PREFERENCES, isSavePreferences);
        activity.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar_TextAppearance);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        boolean save = isInSaveMode();

        attachLoginFragment(save);

        supportPostponeEnterTransition();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //after andrioid m, must request Permission on runtime
        getPermissions(this);

        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void attachLoginFragment(boolean save) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        LoginFragment fragment = (LoginFragment) supportFragmentManager.findFragmentById(R.id.login_container);
        if (fragment == null) {
            fragment = LoginFragment.newInstance(save);
            ActivityUtils.replaceFragmentToActivity(supportFragmentManager, fragment, R.id.login_container);
        }

        new LoginPresenter(
                save,
                getApplicationContext(),
                fragment,
                SchedulerProvider.getInstance());
    }

    private void getPermissions(Context context) {
        List<String> permissions = new ArrayList<>();

        // 定位为必须权限，用户如果禁止，则每次进入都会申请
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        // 读写外设为必须权限，用户如果禁止，则每次进入都会申请
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        // 获取设备状态，友盟API
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (permissions.size() > 0) {
            ActivityCompat.requestPermissions((Activity) context, permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
        }
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(LoginActivity.this, permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permission)) {
                return true;
            } else {
                permissionsList.add(permission);
                return false;
            }

        } else {
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean isInSaveMode() {
        final Intent intent = getIntent();
        boolean save = ARG_SAVE_DEFAULT;
        if (null != intent) {
            save = intent.getBooleanExtra(EXTRA_SAVE_PREFERENCES, ARG_SAVE_DEFAULT);
        }
        return save;
    }
}
