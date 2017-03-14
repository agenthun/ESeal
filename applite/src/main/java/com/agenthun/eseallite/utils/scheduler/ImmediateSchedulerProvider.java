package com.agenthun.eseallite.utils.scheduler;

import android.support.annotation.NonNull;

import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/28 16:01.
 */

public class ImmediateSchedulerProvider implements BaseSchedulerProvider {
    private static ImmediateSchedulerProvider instance = new ImmediateSchedulerProvider();

    public static ImmediateSchedulerProvider getInstance() {
        return instance;
    }

    @NonNull
    @Override
    public Scheduler computation() {
        return Schedulers.immediate();
    }

    @NonNull
    @Override
    public Scheduler io() {
        return Schedulers.immediate();
    }

    @NonNull
    @Override
    public Scheduler ui() {
        return Schedulers.immediate();
    }
}
