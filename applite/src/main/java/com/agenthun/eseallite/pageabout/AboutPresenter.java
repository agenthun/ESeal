package com.agenthun.eseallite.pageabout;

import android.content.Context;
import android.util.Log;

import com.agenthun.eseallite.R;
import com.agenthun.eseallite.bean.updateByRetrofit.UpdateResponse;
import com.agenthun.eseallite.connectivity.manager.RetrofitManager;
import com.agenthun.eseallite.connectivity.service.PathType;
import com.agenthun.eseallite.utils.VersionHelper;
import com.agenthun.eseallite.utils.scheduler.BaseSchedulerProvider;
import com.pekingopera.versionupdate.util.FileUtils;

import java.io.File;

import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/3/1 09:49.
 */

public class AboutPresenter implements AboutContract.Presenter {

    private static final String TAG = "AboutPresenter";

    private Context mContext;

    private AboutContract.View mView;

    private BaseSchedulerProvider mSchedulerProvider;

    private CompositeSubscription mSubscriptions;

    public AboutPresenter(Context context,
                          AboutContract.View view,
                          BaseSchedulerProvider schedulerProvider) {
        mContext = context;
        mView = view;
        mSchedulerProvider = schedulerProvider;

        mSubscriptions = new CompositeSubscription();
        mView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        checkUpdateVersion(true);
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }


    @Override
    public void checkUpdateVersion(boolean auto) {
        mSubscriptions.add(RetrofitManager
                .builder(PathType.WEB_SERVICE_V2_TEST)
                .checkAppLiteUpdateObservable()
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Subscriber<UpdateResponse.Entity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!auto) {
                            mView.showCheckUpdateVersionError();
                        } else {
                            Log.d(TAG, "Error - auto checkUpdateVersion");
                        }
                    }

                    @Override
                    public void onNext(UpdateResponse.Entity entity) {
                        processUpdateVersion(auto, entity);
                    }
                }));
    }

    private void processUpdateVersion(boolean auto, UpdateResponse.Entity entity) {
        if (entity == null) {
            mView.showCheckUpdateVersionError();
        }
        //有更新版本
        if (entity.getVersionCode() > Integer.parseInt(VersionHelper.getVersionCode(mContext))) {
            mView.showNewVersion(entity.getVersionName());
        } else {
            mView.showNoNewVersion();
        }

        if (!auto) {
//            entity.setVersionCode(100); //for my test download

            if (entity.getVersionCode() > Integer.parseInt(VersionHelper.getVersionCode(mContext))) {
                String message = mContext.getString(R.string.text_update_latest_version) + entity.getVersionName().trim() + "\n" +
                        mContext.getString(R.string.text_update_version_size) +
                        FileUtils.HumanReadableFilesize(entity.getApkSize()) + "\n\n" +
                        mContext.getString(R.string.text_update_version_content) + "\n" +
                        entity.getUpdateContent().replace("\\r\\n", "\r\n");

                String packageName = VersionHelper.getPackageName(mContext);
                String name = packageName.substring(packageName.lastIndexOf('.') + 1, packageName.length())
                        + "_v_" + entity.getVersionName().replaceAll("\\.", "_").trim();

                final String path = mContext.getExternalFilesDir(null) + File.separator + name + ".apk";
                File futureStudioIconFile = new File(path);

                if (futureStudioIconFile.exists()) {
                    //文件已下载
                    mView.showNewVersionDownloaded(message, path);
                } else {
                    //文件未下载
                    mView.showNewVersionFound(message, entity.getUpdateUrl(), name);
                }
            } else {
                mView.showAlreadyLatestVersion();
            }
        }
    }
}
