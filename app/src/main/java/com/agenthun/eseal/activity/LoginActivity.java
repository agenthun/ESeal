package com.agenthun.eseal.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.bean.User;
import com.agenthun.eseal.connectivity.manager.RetrofitManager;
import com.agenthun.eseal.connectivity.service.PathType;
import com.agenthun.eseal.utils.NetUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private final int SDK_PERMISSION_REQUEST = 127;

    private AppCompatEditText loginName;

    private AppCompatEditText loginPassword;

    private String token;

    private AppCompatDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar_TextAppearance);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        loginName = (AppCompatEditText) findViewById(R.id.login_name);
        loginPassword = (AppCompatEditText) findViewById(R.id.login_password);

//        loginName.setText("demodemo");
        loginName.setText("henghu");
        loginPassword.setText("123456");
/*        userData = UserData.getCurrentUser(this, UserData.class);
        if (userData != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        //after andrioid m, must request Permission on runtime
        getPermissions(this);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @OnClick(R.id.sign_in_button)
    public void onSignInBtnClick() {
        attemptLogin();
    }

/*    @OnClick(R.id.forget_password_button)
    public void onForgetPasswordBtnClick() {
        startActivity(new Intent(this, ForgetPasswordActivity.class));
    }

    @OnClick(R.id.sign_up_button)
    public void onSignUpBtnClick() {
//        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        startActivity(new Intent(LoginActivity.this, SignUpGridActivity.class));
    }*/

    private void attemptLogin() {
        if (NetUtil.isConnected(this)) { //已连接网络
            String name = loginName.getText().toString();
            String password = loginPassword.getText().toString();

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(LoginActivity.this, R.string.error_invalid_account, Toast.LENGTH_SHORT).show();
                return;
            } else if (TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, R.string.error_invalid_password, Toast.LENGTH_SHORT).show();
                return;
            }

            getProgressDialog().show();

//            RetrofitManager.builder(PathType.BASE_WEB_SERVICE).getTokenObservable(name, password)
            RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST).getTokenObservable(name, password)
                    .subscribe(new Subscriber<User>() {
                        @Override
                        public void onCompleted() {
                            getProgressDialog().cancel();
                        }

                        @Override
                        public void onError(Throwable e) {
                            getProgressDialog().cancel();
                            Toast.makeText(LoginActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(User user) {
                            if (user == null) return;
                            if (user.getEFFECTIVETOKEN() != 1) return;
                            token = user.getTOKEN();
                            Log.d(TAG, "token: " + token);
                            if (token != null) {
                                App.setToken(token);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra(RetrofitManager.TOKEN, token);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, R.string.error_invalid_null, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Snackbar snackbar = Snackbar.make(loginName, getString(R.string.error_network_not_open), Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.text_hint_open_network), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            startActivity(intent);
                        }
                    });
            ((TextView) (snackbar.getView().findViewById(R.id.snackbar_text))).setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.blue_grey_100));
            snackbar.show();
        }
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

    private AppCompatDialog getProgressDialog() {
        if (mProgressDialog != null) {
            return mProgressDialog;
        }
        mProgressDialog = new AppCompatDialog(LoginActivity.this, AppCompatDelegate.MODE_NIGHT_AUTO);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setContentView(R.layout.dialog_logging_in);
        mProgressDialog.setTitle(getString(R.string.action_login));
        mProgressDialog.setCancelable(false);
        return mProgressDialog;
    }
}



