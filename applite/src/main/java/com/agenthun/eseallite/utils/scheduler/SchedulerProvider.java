package com.agenthun.eseallite.utils.scheduler;

import android.support.annotation.NonNull;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/28 15:57.
 */

public class SchedulerProvider implements BaseSchedulerProvider {

    private static SchedulerProvider instance = new SchedulerProvider();

    public static SchedulerProvider getInstance() {
        return instance;
    }

    @NonNull
    @Override
    public Scheduler computation() {
        return Schedulers.computation();
    }

    @NonNull
    @Override
    public Scheduler io() {
        return Schedulers.io();
    }

    @NonNull
    @Override
    public Scheduler ui() {
        return AndroidSchedulers.mainThread();
    }
}
