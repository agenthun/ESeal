package com.agenthun.eseal.view;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.bean.base.LocationDetail;
import com.agenthun.eseal.utils.LanguageUtil;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PendingResult;
import com.google.maps.model.GeocodingResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/8 上午12:26.
 */
public class BottomSheetDialogView {
    private static final String TAG = "BottomSheetDialogView";

    private static List<LocationDetail> details;
    private final View view;
    private Context mContext;

    private boolean mUsingGoogleMap = false;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public BottomSheetDialogView(final Context context, String containerNo, List<LocationDetail> details) {
        mContext = context;

        mUsingGoogleMap = "zh-CN".equals(LanguageUtil.getLanguage()) ? false : true;
//        mUsingGoogleMap = true; //for test googleMap

        BottomSheetDialogView.details = details;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
//        dialog.getDelegate().setLocalNightMode();
        view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_dialog_recycler_view, null);

        AppCompatTextView textView = (AppCompatTextView) view.findViewById(R.id.bottom_sheet_title);
        textView.setText(context.getString(R.string.text_container_no) + " " + containerNo);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.bottom_sheet_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        SimpleAdapter simpleAdapter = new SimpleAdapter();
        recyclerView.setAdapter(simpleAdapter);

        simpleAdapter.setOnItemClickListener(new SimpleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final LocationDetail locationDetail) {
                processReverseGeoCode(mUsingGoogleMap, locationDetail);
            }
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void processReverseGeoCode(boolean usingGoogleMap, final LocationDetail locationDetail) {
        if (usingGoogleMap) {
            GeoApiContext geoApiContext = new GeoApiContext().setApiKey(App.GOOGLE_MAP_API_KEY);
            GeocodingApi.reverseGeocode(geoApiContext, locationDetail.getGoogleMapLatLng())
                    .setCallback(new PendingResult.Callback<GeocodingResult[]>() {
                        @Override
                        public void onResult(GeocodingResult[] result) {
                            final String title = mContext.getString(R.string.text_current_position);
                            String time = "";
                            try {
                                time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                                        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(
                                                locationDetail.getReportTime()
                                        )
                                );
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            final String msg = time + "\r\n\r\n" + (result[0] == null ?
                                    mContext.getString(R.string.fail_get_current_location) : result[0].formattedAddress);

                            new Thread() {
                                @Override
                                public void run() {
                                    getView().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            new AlertDialog.Builder(mContext)
                                                    .setTitle(title)
                                                    .setMessage(msg)
                                                    .setPositiveButton(R.string.text_ok, null).show();
                                        }
                                    });
                                }
                            }.start();
                        }

                        @Override
                        public void onFailure(Throwable e) {
                            Log.d(TAG, "onFailure() GeocodingApi.reverseGeocode");
                            final String title = mContext.getString(R.string.text_current_position);
                            String time = "";
                            try {
                                time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                                        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(
                                                locationDetail.getReportTime()
                                        )
                                );
                            } catch (ParseException error) {
                                return;
                            }
                            final String msg = time + "\r\n\r\n" + mContext.getString(R.string.fail_get_current_location);

                            new Thread() {
                                @Override
                                public void run() {
                                    getView().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            new AlertDialog.Builder(mContext)
                                                    .setTitle(title)
                                                    .setMessage(msg)
                                                    .setPositiveButton(R.string.text_ok, null).show();
                                        }
                                    });
                                }
                            }.start();
                        }
                    });
        } else {
            GeoCoder geoCoder = GeoCoder.newInstance();
            geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(locationDetail.getLatLng()));
            geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                @Override
                public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

                }

                @Override
                public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                    final String title = mContext.getString(R.string.text_current_position);
                    String time = "";
                    try {
                        time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                                new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(
                                        locationDetail.getReportTime()
                                )
                        );
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    final String msg = time + "\r\n\r\n" + (reverseGeoCodeResult.getAddress().isEmpty() ?
                            reverseGeoCodeResult.getAddressDetail().city + ", " + reverseGeoCodeResult.getAddressDetail().province : reverseGeoCodeResult.getAddress());

                    new Thread() {
                        @Override
                        public void run() {
                            getView().post(new Runnable() {
                                @Override
                                public void run() {
                                    new AlertDialog.Builder(mContext)
                                            .setTitle(title)
                                            .setMessage(msg)
                                            .setPositiveButton(R.string.text_ok, null).show();
                                }
                            });
                        }
                    }.start();
                }
            });
        }
    }

    public static void show(Context context, String containerNo, List<LocationDetail> details) {
        new BottomSheetDialogView(context, containerNo, details);
    }

    public View getView() {
        return view;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView securityLevelImageView;
        private AppCompatTextView timeTextView;
        private AppCompatTextView actionTypeTextView;
        private View itemContent;

        public ViewHolder(View itemView) {
            super(itemView);
            securityLevelImageView = (ImageView) itemView.findViewById(R.id.securityLevel);
            timeTextView = (AppCompatTextView) itemView.findViewById(R.id.createDatetime);
            actionTypeTextView = (AppCompatTextView) itemView.findViewById(R.id.actionType);
            itemContent = itemView.findViewById(R.id.item_content);
        }
    }

    private static class SimpleAdapter extends RecyclerView.Adapter<ViewHolder> {
        private Context mContext;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.list_item_freight_track, null);
            this.mContext = parent.getContext();
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            String actionType = details.get(position).getUploadType();
            int securityLevel = Integer.parseInt(details.get(position).getSecurityLevel());
            switch (actionType) {
                case "0": //周期上传
                    if (securityLevel == 2) { //非法打开
                        holder.securityLevelImageView.setImageResource(R.drawable.ic_warning_black_24dp);
                        holder.securityLevelImageView.setColorFilter(
                                ContextCompat.getColor(mContext, R.color.red_500));
                        break;
                    } else { //正常
                        holder.securityLevelImageView.setImageResource(R.drawable.ic_lock_black_24dp);
                        holder.securityLevelImageView.setColorFilter(
                                ContextCompat.getColor(mContext, R.color.blue_grey_500));
                    }
                    break;
                case "100":
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_settings_black_24dp);
                    holder.securityLevelImageView.setColorFilter(
                            ContextCompat.getColor(mContext, R.color.blue_grey_500));
                    break;
                case "101": //上封
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_lock_black_24dp);
                    if (securityLevel == 2) { //非法状态
                        holder.securityLevelImageView.setColorFilter(
                                ContextCompat.getColor(mContext, R.color.red_500));
                    } else { //正常
                        holder.securityLevelImageView.setColorFilter(
                                ContextCompat.getColor(mContext, R.color.colorPrimary));
                    }
                    break;
                case "102": //解封
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_lock_open_black_24dp);
                    if (securityLevel == 2) { //非法状态
                        holder.securityLevelImageView.setColorFilter(
                                ContextCompat.getColor(mContext, R.color.red_500));
                    } else { //正常
                        holder.securityLevelImageView.setColorFilter(
                                ContextCompat.getColor(mContext, R.color.colorPrimary));
                    }
                    break;

/*                case "2":
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_warning_black_24dp);
                    holder.securityLevelImageView.setColorFilter(
                            ContextCompat.getColor(mContext, R.color.red_500));
                    break;
                case "4":
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_lock_open_black_24dp);
                    holder.securityLevelImageView.setColorFilter(
                            ContextCompat.getColor(mContext, R.color.colorPrimary));
                    break;
                case "5":
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_settings_black_24dp);
                    holder.securityLevelImageView.setColorFilter(
                            ContextCompat.getColor(mContext, R.color.blue_grey_500));
                    break;*/
            }


            String time = "";
            try {
                time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(
                                details.get(position).getReportTime()
                        )
                );
            } catch (ParseException e) {
                e.printStackTrace();
            }
            holder.timeTextView.setText(time);
            holder.actionTypeTextView.setText(getActionType(actionType));

            holder.itemContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(v, details.get(position));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return details.size();
        }

        //获取相应的ActionType
        private String getActionType(String actionType) {
            switch (actionType) {
                case "0": //周期上传
                    return mContext.getString(R.string.action_type_0);
                case "100": //配置
                    return mContext.getString(R.string.action_type_100);
                case "101": //上封
                    return mContext.getString(R.string.action_type_101);
                case "102": //解封
                    return mContext.getString(R.string.action_type_102);
/*                case "2":
                    return mContext.getString(R.string.action_type_2);
                case "4":
                    return mContext.getString(R.string.action_type_4);*/
            }
            return "";
        }

        //itemClick interface
        private interface OnItemClickListener {
            void onItemClick(View view, LocationDetail locationDetail);
        }

        private OnItemClickListener mOnItemClickListener;

        private void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
            this.mOnItemClickListener = mOnItemClickListener;
        }
    }
}
