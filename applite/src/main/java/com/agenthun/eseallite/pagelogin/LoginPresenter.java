package com.agenthun.eseallite.pagelogin;

import android.content.Context;
import android.util.Log;

import com.agenthun.eseallite.bean.User;
import com.agenthun.eseallite.connectivity.manager.RetrofitManager;
import com.agenthun.eseallite.connectivity.service.PathType;
import com.agenthun.eseallite.utils.NetUtil;
import com.agenthun.eseallite.utils.PreferencesHelper;
import com.agenthun.eseallite.utils.scheduler.BaseSchedulerProvider;

import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/28 20:07.
 */

public class LoginPresenter implements LoginContract.Presenter {
    private static final String TAG = "LoginPresenter";

    private boolean mSave;

    private Context mContext;

    private LoginContract.View mView;

    private BaseSchedulerProvider mSchedulerProvider;

    private CompositeSubscription mSubscriptions;

    public LoginPresenter(boolean save,
                          Context context,
                          LoginContract.View view,
                          BaseSchedulerProvider schedulerProvider) {
        mSave = save;
        mContext = context;
        mView = view;
        mSchedulerProvider = schedulerProvider;

        mSubscriptions = new CompositeSubscription();
        mView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        PreferencesHelper.User mUser = assureUserInit();
        if (mUser != null) {
            mView.initContents(mUser.getUsername(), mUser.getPassword());
            attemptLogin(mUser.getUsername(), mUser.getPassword(), mSave);
        }
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    @Override
    public PreferencesHelper.User assureUserInit() {
        return PreferencesHelper.getUser(mContext);
    }

    @Override
    public void attemptLogin(String name, String password, boolean save) {
        if (!save) {
            if (NetUtil.isConnected(mContext)) {
                mView.setLoginingIndicator(true);

                mSubscriptions.add(RetrofitManager
                        .builder(PathType.WEB_SERVICE_V2_TEST)
                        .getTokenObservable(name, password)
                        .subscribeOn(mSchedulerProvider.io())
                        .observeOn(mSchedulerProvider.ui())
                        .subscribe(new Subscriber<User>() {
                            @Override
                            public void onCompleted() {
                                mView.setLoginingIndicator(false);
                            }

                            @Override
                            public void onError(Throwable e) {
                                mView.setLoginingIndicator(false);
                                mView.showNetworkError();
                            }

                            @Override
                            public void onNext(User user) {
                                processToken(user);
                            }
                        }));
            } else {
                mView.showNetworkError();
            }
        }
    }

    @Override
    public void saveUser(String name, String password) {
        PreferencesHelper.clearUser(mContext);
        PreferencesHelper.writeUserInfoToPreferences(mContext,
                new PreferencesHelper.User(name, password));
    }

    private void processToken(User user) {
        if (user == null) {
            mView.showInvalidTokenError();
            return;
        }
        if (user.getEFFECTIVETOKEN() != 1) {
            mView.showInputDataError();
            return;
        }

        String token = user.getTOKEN();
        Log.d(TAG, "token: " + token);
        if (token != null) {
            PreferencesHelper.writeTokenToPreferences(mContext, token);
            mView.showScanNfcDevicePage();
        } else {
            mView.showInvalidTokenError();
        }
    }
}
