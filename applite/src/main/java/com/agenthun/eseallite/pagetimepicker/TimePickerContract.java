package com.agenthun.eseallite.pagetimepicker;

import com.agenthun.eseallite.BasePresenter;
import com.agenthun.eseallite.BaseView;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/3/2 20:01.
 */

public interface TimePickerContract {
    interface View extends BaseView<Presenter> {
        void showPickBeginTimeNullError();

        void showPickEndTimeNullError();

        void showPickTimeLogicError();
    }

    interface Presenter extends BasePresenter {

    }
}
