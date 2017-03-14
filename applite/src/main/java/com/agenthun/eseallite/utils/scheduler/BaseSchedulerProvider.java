package com.agenthun.eseallite.utils.scheduler;

import android.support.annotation.NonNull;

import rx.Scheduler;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/28 15:55.
 */

public interface BaseSchedulerProvider {

    @NonNull
    Scheduler computation();

    @NonNull
    Scheduler io();

    @NonNull
    Scheduler ui();
}
