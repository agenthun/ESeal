package com.agenthun.eseal.fragment;

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
import android.util.Log;
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

import com.agenthun.eseal.R;
import com.agenthun.eseal.bean.updateByRetrofit.UpdateResponse;
import com.agenthun.eseal.connectivity.manager.DownloadService;
import com.agenthun.eseal.connectivity.manager.RetrofitManager;
import com.agenthun.eseal.connectivity.service.PathType;
import com.agenthun.eseal.utils.ApiLevelHelper;
import com.agenthun.eseal.utils.VersionHelper;
import com.pekingopera.versionupdate.util.FileUtils;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/22 11:58.
 */

public class AboutFragment extends Fragment {
    private static final String TAG = "AboutFragment";

    private static final String MY_HOME_PAGE_URL = "https://agenthun.github.io";

    Toolbar toolbar;

    @Bind(R.id.about_content)
    View aboutContent;
    @Bind(R.id.web_content)
    View webContent;
    @Bind(R.id.webView)
    WebView webView;
    @Bind(R.id.progress)
    ContentLoadingProgressBar progressBar;
    @Bind(R.id.web_error_content)
    View webErrorContent;

    @Bind(R.id.app_version_name)
    AppCompatTextView appVersionName;

    @Bind(R.id.app_new_version_hint)
    AppCompatTextView appNewVersionHint;
    @Bind(R.id.app_new_version_name)
    AppCompatTextView appNewVersionName;

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
        ButterKnife.bind(this, view);

        String versionName = VersionHelper.getVersionName(getContext());
        appVersionName.setText(versionName);

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webContent.getVisibility() == View.VISIBLE) {
                    webContent.setVisibility(View.GONE);
                    aboutContent.setVisibility(View.VISIBLE);
                    toolbar.setTitle(R.string.about);
                } else if (webContent.getVisibility() == View.GONE) {
                    getActivity().onBackPressed();
                }
            }
        });

        setupWebView();

        checkUpdateVersion(true);
        return view;
    }

    @OnClick({R.id.update_version_area, R.id.app_introduction, R.id.app_thanks, R.id.app_about_me})
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.update_version_area:
                Log.d(TAG, "onClick() returned: update_version_area");
                checkUpdateVersion(false);
                break;
            case R.id.app_introduction:
                Log.d(TAG, "onClick() returned: app_introduction");
                showIntroduction();
                break;
            case R.id.app_thanks:
                Log.d(TAG, "onClick() returned: app_thanks");
                showThanks();
                break;
            case R.id.app_about_me:
                Log.d(TAG, "onClick() returned: app_about_me");
                showAboutMe();
                break;
            default:
                break;
        }
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

    private void showWebView() {
        webView.loadUrl(MY_HOME_PAGE_URL);

        aboutContent.setVisibility(View.GONE);
        webContent.setVisibility(View.VISIBLE);

        webView.setVisibility(View.VISIBLE);

        progressBar.setVisibility(View.VISIBLE);
        progressBar.show();

        webErrorContent.setVisibility(View.GONE);
    }

    private void showWebViewLoadError() {
        webView.setVisibility(View.GONE);

        progressBar.setVisibility(View.GONE);
        progressBar.hide();

        webErrorContent.setVisibility(View.VISIBLE);
    }

    private void showIntroduction() {
        showDialog(getString(R.string.text_app_introduction), R.layout.dialog_introduction,
                getString(R.string.text_ok), null);
    }

    private void showThanks() {
        showDialog(getString(R.string.text_thanks), getString(R.string.text_thanks_name),
                null, null, null, null);
    }

    private void showAboutMe() {
        toolbar.setTitle(R.string.text_app_about_me);
        showWebView();
    }

    private void showNoNewVersion() {
        appNewVersionHint.setVisibility(View.GONE);
        appNewVersionName.setVisibility(View.GONE);
    }

    private void showNewVersion(String newVersionName) {
        appNewVersionHint.setVisibility(View.VISIBLE);
        appNewVersionName.setVisibility(View.VISIBLE);
        appNewVersionName.setText(newVersionName);
    }

    private void showCheckUpdateVersionError() {
        showMessage(getString(R.string.error_check_update_version));
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

    private void processUpdateVersion(boolean auto, final UpdateResponse.Entity entity) {
        if (entity == null) {
            showCheckUpdateVersionError();
        }
        //有更新版本
        if (entity.getVersionCode() > Integer.parseInt(VersionHelper.getVersionCode(getContext()))) {
            showNewVersion(entity.getVersionName());
        } else {
            showNoNewVersion();
        }

        if (!auto) {
//            entity.setVersionCode(100); //for my test download

            if (entity.getVersionCode() > Integer.parseInt(VersionHelper.getVersionCode(getContext()))) {
                String message = getString(R.string.text_update_latest_version) + entity.getVersionName().trim() + "\n" +
                        getString(R.string.text_update_version_size) +
                        FileUtils.HumanReadableFilesize(entity.getApkSize()) + "\n\n" +
                        getString(R.string.text_update_version_content) + "\n" +
                        entity.getUpdateContent().replace("\\r\\n", "\r\n");

                String packageName = VersionHelper.getPackageName(getContext());
                final String name = packageName.substring(packageName.lastIndexOf('.') + 1, packageName.length())
                        + "_v_" + entity.getVersionName().replaceAll("\\.", "_").trim();

                final String path = getContext().getExternalFilesDir(null) + File.separator + name + ".apk";
                File futureStudioIconFile = new File(path);

                if (futureStudioIconFile.exists()) {
                    //文件已下载
                    showDialog(getString(R.string.text_downloaded_new_app_version),
                            message,
                            getString(R.string.text_app_install_now),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    processInstallApk(getContext(), path);
                                }
                            },
                            getString(R.string.text_app_update_later),
                            null
                    );
                } else {
                    //文件未下载
                    showDialog(getString(R.string.text_found_new_app_version),
                            message,
                            getString(R.string.text_app_update_now),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DownloadService.start(getContext(), entity.getUpdateUrl(), name);//下载显示方式Notification进度条
                                }
                            },
                            getString(R.string.text_app_update_later),
                            null
                    );
                }
            } else {
                showMessage(getString(R.string.text_app_already_the_latest_version));
            }
        }
    }

    private void checkUpdateVersion(final boolean auto) {
        RetrofitManager.builder(PathType.ESeal_UPDATE_SERVICE_URL)
                .checkAppUpdateObservable()
                .subscribe(new Subscriber<UpdateResponse.Entity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!auto) {
                            showCheckUpdateVersionError();
                        } else {
                            Log.d(TAG, "Error - auto checkUpdateVersion");
                        }
                    }

                    @Override
                    public void onNext(UpdateResponse.Entity entity) {
                        processUpdateVersion(auto, entity);
                    }
                });
    }
}
