package com.agenthun.eseallite.pagetimepicker;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.agenthun.eseallite.R;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/15 15:40.
 */

public class TimePickerFragment extends Fragment implements TimePickerContract.View {
    private static final String TAG = "TimePickerFragment";
    private static final String TAG_TIME_FROM = "TAG_TIME_FROM";
    private static final String TAG_TIME_TO = "TAG_TIME_TO";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private PickTimeListener mPickTimeListener;
    private TimePickerDialog mTimePickerDialog;

    AppCompatEditText timeFrom;
    AppCompatEditText timeTo;

    private TimePickerContract.Presenter mPresenter;

    public static TimePickerFragment newInstance(PickTimeListener pickTimeListener) {
        TimePickerFragment fragment = new TimePickerFragment();
        if (pickTimeListener != null) {
            fragment.mPickTimeListener = pickTimeListener;
        }
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_time_picker, container, false);

        timeFrom = (AppCompatEditText) view.findViewById(R.id.time_from);
        timeFrom.setOnClickListener(__ -> getTimePickerDialog().show(getFragmentManager(), TAG_TIME_FROM));

        timeTo = (AppCompatEditText) view.findViewById(R.id.time_to);
        timeTo.setOnClickListener(__ -> getTimePickerDialog().show(getFragmentManager(), TAG_TIME_TO));

        view.findViewById(R.id.pick_time_button).setOnClickListener(__ -> {
            if (mPickTimeListener != null) {
                String from = timeFrom.getText().toString();
                String to = timeTo.getText().toString();

                if (TextUtils.isEmpty(from)) {
                    showPickBeginTimeNullError();
                    return;
                }
                if (TextUtils.isEmpty(to)) {
                    showPickEndTimeNullError();
                    return;
                }
                try {
                    Date dateFrom = DATE_FORMAT.parse(from);
                    Date dateTo = DATE_FORMAT.parse(to);
                    if (!dateTo.after(dateFrom)) {
                        showPickTimeLogicError();
                        return;
                    }
                } catch (ParseException e) {
                    return;
                }

                mPickTimeListener.onTimePicked(from, to);
                getActivity().onBackPressed();
            }
        });
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
    public void setPresenter(TimePickerContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showPickBeginTimeNullError() {
        showMessage(getString(R.string.error_time_from));
    }

    @Override
    public void showPickEndTimeNullError() {
        showMessage(getString(R.string.error_time_to));
    }

    @Override
    public void showPickTimeLogicError() {
        showMessage(getString(R.string.error_time_from_to));
    }

    private void showMessage(String message) {
        Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        ((TextView) (snackbar.getView().findViewById(R.id.snackbar_text)))
                .setTextColor(ContextCompat.getColor(getContext(), R.color.blue_grey_100));
        snackbar.show();
    }

    private TimePickerDialog getTimePickerDialog() {
        if (mTimePickerDialog != null) {
            return mTimePickerDialog;
        }
        mTimePickerDialog = new TimePickerDialog.Builder()
                .setCallBack(mOnDateSetListener)
                .setTitleStringId(getString(R.string.text_time_picker_title))
                .setSureStringId(getString(R.string.text_time_picker_ok))
                .setCancelStringId(getString(R.string.text_time_picker_cancel))
                .setYearText(getString(R.string.text_time_picker_year))
                .setMonthText(getString(R.string.text_time_picker_month))
                .setDayText(getString(R.string.text_time_picker_day))
                .setHourText(getString(R.string.text_time_picker_hour))
                .setMinuteText(getString(R.string.text_time_picker_minute))
                .setCyclic(false)
                .setCurrentMillseconds(System.currentTimeMillis())
                .setThemeColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary))
                .setType(Type.ALL)
//                .setWheelItemTextNormalColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary))
                .setWheelItemTextSelectorColor(ContextCompat.getColor(getActivity(), R.color.colorAccent))
                .setWheelItemTextSize(16)
                .build();
        return mTimePickerDialog;
    }

    private OnDateSetListener mOnDateSetListener = new OnDateSetListener() {
        @Override
        public void onDateSet(TimePickerDialog timePickerView, long millseconds) {
            final String time = DATE_FORMAT.format(new Date(millseconds));
            String tag = timePickerView.getTag();
            if (TAG_TIME_FROM.equals(tag)) {
                Log.d(TAG, "onDateSet() returned: from " + time);
                getView().post(new Runnable() {
                    @Override
                    public void run() {
                        timeFrom.setText(time);
                    }
                });
            }
            if (TAG_TIME_TO.equals(tag)) {
                Log.d(TAG, "onDateSet() returned: to " + time);
                getView().post(new Runnable() {
                    @Override
                    public void run() {
                        timeTo.setText(time);
                    }
                });
            }
        }
    };

    //itemClick interface
    public interface PickTimeListener {
        void onTimePicked(String from, String to);
    }
}
