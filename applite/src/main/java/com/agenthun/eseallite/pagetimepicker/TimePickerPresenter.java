package com.agenthun.eseallite.pagetimepicker;

import android.content.Context;

import com.agenthun.eseallite.utils.scheduler.SchedulerProvider;

import rx.subscriptions.CompositeSubscription;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/3/2 20:13.
 */

public class TimePickerPresenter implements TimePickerContract.Presenter {

    private Context mContext;

    private TimePickerContract.View mView;

    private SchedulerProvider mSchedulerProvider;

    private CompositeSubscription mCompositeSubscription;

    public TimePickerPresenter(Context context, TimePickerContract.View view, SchedulerProvider schedulerProvider) {
        mContext = context;
        mView = view;
        mSchedulerProvider = schedulerProvider;

        mCompositeSubscription = new CompositeSubscription();
        mView.setPresenter(this);
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {
        mCompositeSubscription.clear();
    }
}
