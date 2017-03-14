package com.agenthun.eseallite.pageabout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.agenthun.eseallite.R;
import com.agenthun.eseallite.connectivity.manager.DownloadService;
import com.agenthun.eseallite.utils.ApiLevelHelper;
import com.agenthun.eseallite.utils.VersionHelper;

import java.io.File;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/22 11:58.
 */

public class AboutFragment extends Fragment implements AboutContract.View {
    private static final String TAG = "AboutFragment";

    private static final String MY_HOME_PAGE_URL = "https://agenthun.github.io";

    Toolbar toolbar;

    View aboutContent;
    View webContent;
    WebView webView;
    ContentLoadingProgressBar progressBar;
    View webErrorContent;

    AppCompatTextView appVersionName;

    AppCompatTextView appNewVersionHint;
    AppCompatTextView appNewVersionName;

    private AboutContract.Presenter mPresenter;

    public static AboutFragment newInstance() {

        Bundle args = new Bundle();

        AboutFragment fragment = new AboutFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        aboutContent = view.findViewById(R.id.about_content);

        webContent = view.findViewById(R.id.web_content);
        webView = (WebView) view.findViewById(R.id.webView);
        progressBar = (ContentLoadingProgressBar) view.findViewById(R.id.progress);
        webErrorContent = view.findViewById(R.id.web_error_content);

        appVersionName = (AppCompatTextView) view.findViewById(R.id.app_version_name);
        appNewVersionHint = (AppCompatTextView) view.findViewById(R.id.app_new_version_hint);
        appNewVersionName = (AppCompatTextView) view.findViewById(R.id.app_new_version_name);

        String versionName = VersionHelper.getVersionName(getContext());
        appVersionName.setText(versionName);

        view.findViewById(R.id.update_version_area).setOnClickListener(__ -> mPresenter.checkUpdateVersion(false));

        view.findViewById(R.id.app_introduction).setOnClickListener(__ -> showIntroduction());

        view.findViewById(R.id.app_thanks).setOnClickListener(__ -> showThanks());

        view.findViewById(R.id.app_about_me).setOnClickListener(__ -> showAboutMe());

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(__ -> {
            if (webContent.getVisibility() == View.VISIBLE) {
                webContent.setVisibility(View.GONE);
                aboutContent.setVisibility(View.VISIBLE);
                toolbar.setTitle(R.string.about);
            } else if (webContent.getVisibility() == View.GONE) {
                getActivity().onBackPressed();
            }
        });

        setupWebView();

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
    public void setPresenter(AboutContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        WebSettings webSettings = webView.getSettings();
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                    progressBar.hide();
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                //处理404错误码
                if (ApiLevelHelper.isLowerThan(Build.VERSION_CODES.M)) {
                    if (title.contains("404") || title.contains("网页无法打开") || title.contains("找不到网页")) {
                        showWebViewLoadError();
                    }
                }
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                webView.loadUrl(request.toString());
                return true;
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                if (ApiLevelHelper.isAtLeast(Build.VERSION_CODES.LOLLIPOP)) {
                    int statusCode = errorResponse.getStatusCode();
                    //处理404错误码
                    if (404 == statusCode || 500 == statusCode) {
                        showWebViewLoadError();
                    }
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (ApiLevelHelper.isAtLeast(Build.VERSION_CODES.M)) {
                    int errorCode = error.getErrorCode();
                    //处理断网,超时
                    if (ERROR_CONNECT == errorCode || ERROR_HOST_LOOKUP == errorCode || ERROR_TIMEOUT == errorCode) {
                        showWebViewLoadError();
                    }
                }
            }
        });
    }

    @Override
    public void showWebView() {
        webView.loadUrl(MY_HOME_PAGE_URL);

        aboutContent.setVisibility(View.GONE);
        webContent.setVisibility(View.VISIBLE);

        webView.setVisibility(View.VISIBLE);

        progressBar.setVisibility(View.VISIBLE);
        progressBar.show();

        webErrorContent.setVisibility(View.GONE);
    }

    @Override
    public void showWebViewLoadError() {
        webView.setVisibility(View.GONE);

        progressBar.setVisibility(View.GONE);
        progressBar.hide();

        webErrorContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void showIntroduction() {
        showDialog(getString(R.string.text_app_introduction), R.layout.dialog_introduction,
                getString(R.string.text_ok), null);
    }

    @Override
    public void showThanks() {
        showDialog(getString(R.string.text_thanks), getString(R.string.text_thanks_name),
                null, null, null, null);
    }

    @Override
    public void showAboutMe() {
        toolbar.setTitle(R.string.text_app_about_me);
        showWebView();
    }

    @Override
    public void showNoNewVersion() {
        appNewVersionHint.setVisibility(View.GONE);
        appNewVersionName.setVisibility(View.GONE);
    }

    @Override
    public void showNewVersion(String newVersionName) {
        appNewVersionHint.setVisibility(View.VISIBLE);
        appNewVersionName.setVisibility(View.VISIBLE);
        appNewVersionName.setText(newVersionName);
    }

    @Override
    public void showCheckUpdateVersionError() {
        showMessage(getString(R.string.error_check_update_version));
    }

    @Override
    public void showNewVersionFound(String message, String url, String name) {
        showDialog(getString(R.string.text_found_new_app_version),
                message,
                getString(R.string.text_app_update_now),
                (dialog, which) -> DownloadService.start(getContext(), url, name), //下载显示方式Notification进度条
                getString(R.string.text_app_update_later),
                null
        );
    }

    @Override
    public void showNewVersionDownloaded(String message, String path) {
        showDialog(getString(R.string.text_downloaded_new_app_version),
                message,
                getString(R.string.text_app_install_now),
                (dialog, which) -> processInstallApk(getContext(), path),
                getString(R.string.text_app_update_later),
                null
        );
    }

    @Override
    public void showAlreadyLatestVersion() {
        showMessage(getString(R.string.text_app_already_the_latest_version));
    }

    private void showMessage(String message) {
        Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        ((TextView) (snackbar.getView().findViewById(R.id.snackbar_text)))
                .setTextColor(ContextCompat.getColor(getContext(), R.color.blue_grey_100));
        snackbar.show();
    }

    private void showMessage(String message, String textActionButton, @Nullable View.OnClickListener onClickListener) {
        Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setAction(textActionButton, onClickListener);
        ((TextView) (snackbar.getView().findViewById(R.id.snackbar_text)))
                .setTextColor(ContextCompat.getColor(getContext(), R.color.blue_grey_100));
        snackbar.show();
    }

    private void showDialog(String title,
                            @Nullable String message,
                            @Nullable String textPositiveButton,
                            @Nullable DialogInterface.OnClickListener onPositiveButtonClickListener,
                            @Nullable String textNegativeButton,
                            @Nullable DialogInterface.OnClickListener onNegativeButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(title)
                .setMessage(message);
        if (textPositiveButton != null) {
            builder.setPositiveButton(textPositiveButton, onPositiveButtonClickListener);
        }
        if (textNegativeButton != null) {
            builder.setNegativeButton(textNegativeButton, onNegativeButtonClickListener);
        }
        builder.show();
    }

    private void showDialog(String title, int layoutResId,
                            @Nullable String textPositiveButton,
                            @Nullable DialogInterface.OnClickListener onPositiveButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(title).setView(layoutResId);

        if (textPositiveButton != null) {
            builder.setPositiveButton(textPositiveButton, onPositiveButtonClickListener);
        }
        builder.show();
    }

    private void processInstallApk(Context context, String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(path)),
                "application/vnd.android.package-archive");
        ContextCompat.startActivity(context, intent, null);
    }
}
