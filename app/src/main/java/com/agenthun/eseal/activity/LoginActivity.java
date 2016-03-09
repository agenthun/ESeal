package com.agenthun.eseal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.bean.UserInfoByGetToken;
import com.agenthun.eseal.connectivity.manager.RetrofitManager;
import com.agenthun.eseal.connectivity.service.PathType;

import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private AppCompatEditText loginName;
    private AppCompatEditText loginPassword;

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        loginName = (AppCompatEditText) findViewById(R.id.login_name);
        loginPassword = (AppCompatEditText) findViewById(R.id.login_password);

        loginName.setText("demodemo");
        loginPassword.setText("123456");
/*        userData = UserData.getCurrentUser(this, UserData.class);
        if (userData != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }*/
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
        String name = loginName.getText().toString();
        String password = loginPassword.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(LoginActivity.this, R.string.error_invalid_account, Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, R.string.error_invalid_password, Toast.LENGTH_SHORT).show();
            return;
        }

        Call<UserInfoByGetToken> call = RetrofitManager.builder(PathType.BASE_WEB_SERVICE).getTokenObservable(name, password);
        call.enqueue(new Callback<UserInfoByGetToken>() {
            @Override
            public void onResponse(Call<UserInfoByGetToken> call, Response<UserInfoByGetToken> response) {
                token = response.body().getTOKEN();
                Log.d(TAG, "token: " + token);
                if (token != null) {
                    App.setToken(token);
//                    Intent intent = new Intent(LoginActivity.this, DeviceOperationActivity.class); //for debug
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra(RetrofitManager.TOKEN, token);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, R.string.error_invalid_null, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserInfoByGetToken> call, Throwable t) {
                Log.d(TAG, "Response onFailure: " + t.getLocalizedMessage());
            }
        });
    }
}



