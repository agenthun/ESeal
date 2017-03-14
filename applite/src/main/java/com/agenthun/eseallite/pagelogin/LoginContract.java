package com.agenthun.eseallite.pagelogin;

import com.agenthun.eseallite.BasePresenter;
import com.agenthun.eseallite.BaseView;
import com.agenthun.eseallite.utils.PreferencesHelper;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/28 16:07.
 */

public interface LoginContract {
    interface View extends BaseView<Presenter> {
        void initContents(String name, String password);

        void setLoginingIndicator(boolean active);

        boolean isInputDataValid();

        void showInputDataError();

        void showNetworkError();

        void showInvalidTokenError();

        void showScanNfcDevicePage();
    }

    interface Presenter extends BasePresenter {
        PreferencesHelper.User assureUserInit();

        void attemptLogin(String name, String password, boolean save);

        void saveUser(String name, String password);
    }
}
