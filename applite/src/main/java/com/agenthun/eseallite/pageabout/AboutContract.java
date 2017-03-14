package com.agenthun.eseallite.pageabout;

import com.agenthun.eseallite.BasePresenter;
import com.agenthun.eseallite.BaseView;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/3/1 09:48.
 */

public interface AboutContract {
    interface View extends BaseView<Presenter> {
        void showWebView();

        void showWebViewLoadError();

        void showIntroduction();

        void showThanks();

        void showAboutMe();

        void showNoNewVersion();

        void showNewVersion(String newVersionName);

        void showCheckUpdateVersionError();

        void showNewVersionFound(String message, String url, String name);

        void showNewVersionDownloaded(String message, String path);

        void showAlreadyLatestVersion();
    }

    interface Presenter extends BasePresenter {
        void checkUpdateVersion(boolean auto);
    }
}
