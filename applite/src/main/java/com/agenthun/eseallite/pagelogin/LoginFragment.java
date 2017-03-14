package com.agenthun.eseallite.pagelogin;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.agenthun.eseallite.R;
import com.agenthun.eseallite.pagescannfcdevice.ScanNfcDeviceActivity;
import com.agenthun.eseallite.utils.PreferencesHelper;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/15 15:40.
 */

public class LoginFragment extends Fragment implements LoginContract.View {
    private static final String TAG = "LoginFragment";

    private static final String ARG_SAVE = "SAVE";
    private static final boolean ARG_SAVE_DEFAULT = false;

    AppCompatEditText loginName;
    AppCompatEditText loginPassword;

    private AppCompatDialog mProgressDialog;

    private boolean save;

    private LoginContract.Presenter mPresenter;

    public static LoginFragment newInstance(boolean save) {

        Bundle args = new Bundle();
        args.putBoolean(ARG_SAVE, save);
        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        loginName = (AppCompatEditText) view.findViewById(R.id.login_name);
        loginPassword = (AppCompatEditText) view.findViewById(R.id.login_password);

        AppCompatButton signInButton = (AppCompatButton) view.findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(__ -> {
            if (isInputDataValid()) {
                String name = loginName.getText().toString();
                String password = loginPassword.getText().toString();
                mPresenter.saveUser(name, password);
                mPresenter.attemptLogin(name, password, false);
            } else {
                showInputDataError();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }

    @Override
    public void setPresenter(LoginContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void initContents(String name, String password) {
        loginName.setText(name);
        loginName.setSelection(name.length());
        loginName.setFocusable(true);
        loginPassword.setText(password);
        loginPassword.setFocusable(true);
    }

    @Override
    public void setLoginingIndicator(boolean active) {
        if (active) {
            getProgressDialog().show();
        } else {
            getProgressDialog().cancel();
        }
    }

    @Override
    public boolean isInputDataValid() {
        return PreferencesHelper.isInputDataValid(loginName.getText(), loginPassword.getText());
    }

    @Override
    public void showInputDataError() {
        showMessage(getString(R.string.error_invalid_user));
    }

    @Override
    public void showNetworkError() {
        showMessage(getString(R.string.error_network_not_open),
                getString(R.string.text_hint_open_network),
                __ -> {
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivity(intent);
                });
    }

    @Override
    public void showInvalidTokenError() {
        showMessage(getString(R.string.error_invalid_null));
    }

    @Override
    public void showScanNfcDevicePage() {
        ScanNfcDeviceActivity.start(getContext());
        ActivityCompat.finishAfterTransition(getActivity());
    }

    private AppCompatDialog getProgressDialog() {
        if (mProgressDialog != null) {
            return mProgressDialog;
        }
        mProgressDialog = new AppCompatDialog(getContext(), AppCompatDelegate.MODE_NIGHT_AUTO);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setContentView(R.layout.dialog_logging_in);
        mProgressDialog.setTitle(getString(R.string.action_login));
        mProgressDialog.setCancelable(false);
        return mProgressDialog;
    }

    private void showMessage(String message) {
        showMessage(message, null, null);
    }

    private void showMessage(String message, @Nullable String textActionButton, @Nullable View.OnClickListener onClickListener) {
        Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG);
        if (textActionButton != null) {
            snackbar.setAction(textActionButton, onClickListener);
        }
        ((TextView) (snackbar.getView().findViewById(R.id.snackbar_text)))
                .setTextColor(ContextCompat.getColor(getContext(), R.color.blue_grey_100));
        snackbar.show();
    }

    private void checkIsInSaveMode() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            save = ARG_SAVE_DEFAULT;
        } else {
            save = arguments.getBoolean(ARG_SAVE, ARG_SAVE_DEFAULT);
        }
    }
}
