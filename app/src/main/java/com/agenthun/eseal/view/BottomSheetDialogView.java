package com.agenthun.eseal.view;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.bean.base.LocationDetail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/8 上午12:26.
 */
public class BottomSheetDialogView {
    private static List<LocationDetail> details;
    private final View view;

    public BottomSheetDialogView(Context context, String containerNo, List<LocationDetail> details) {
        BottomSheetDialogView.details = details;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
//        dialog.getDelegate().setLocalNightMode();
        view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_dialog_recycler_view, null);

        AppCompatTextView textView = (AppCompatTextView) view.findViewById(R.id.bottom_sheet_title);
        textView.setText(context.getString(R.string.text_container_no) + " " + containerNo);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.bottom_sheet_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new SimpleAdapter());

        dialog.setContentView(view);
        dialog.show();
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

        public ViewHolder(View itemView) {
            super(itemView);
            securityLevelImageView = (ImageView) itemView.findViewById(R.id.securityLevel);
            timeTextView = (AppCompatTextView) itemView.findViewById(R.id.createDatetime);
            actionTypeTextView = (AppCompatTextView) itemView.findViewById(R.id.actionType);
        }
    }

    private static class SimpleAdapter extends RecyclerView.Adapter<ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.list_item_freight_track, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String actionType = details.get(position).getStatus();
            switch (actionType) {
                case "0":
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_lock_black_24dp);
                    holder.securityLevelImageView.setColorFilter(
                            ContextCompat.getColor(App.getContext(), R.color.dark_gray));
                    break;
                case "1":
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_lock_black_24dp);
                    holder.securityLevelImageView.setColorFilter(
                            ContextCompat.getColor(App.getContext(), R.color.dark_gray));
                    break;
                case "2":
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_warning_black_24dp);
                    holder.securityLevelImageView.setColorFilter(
                            ContextCompat.getColor(App.getContext(), R.color.red_500));
                    break;
                case "3":
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_lock_black_24dp);
                    holder.securityLevelImageView.setColorFilter(
                            ContextCompat.getColor(App.getContext(), R.color.colorPrimary));
                    break;
                case "4":
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_lock_open_black_24dp);
                    holder.securityLevelImageView.setColorFilter(
                            ContextCompat.getColor(App.getContext(), R.color.colorPrimary));
                    break;
                case "5":
                    holder.securityLevelImageView.setImageResource(R.drawable.ic_settings_black_24dp);
                    holder.securityLevelImageView.setColorFilter(
                            ContextCompat.getColor(App.getContext(), R.color.dark_gray));
                    break;
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
        }

        @Override
        public int getItemCount() {
            return details.size();
        }

        //获取相应的ActionType
        private String getActionType(String actionType) {
            switch (actionType) {
                case "0":
                    return App.getContext().getString(R.string.action_type_0);
                case "1":
                    return App.getContext().getString(R.string.action_type_1);
                case "2":
                    return App.getContext().getString(R.string.action_type_2);
                case "3":
                    return App.getContext().getString(R.string.action_type_3);
                case "4":
                    return App.getContext().getString(R.string.action_type_4);
                case "5":
                    return App.getContext().getString(R.string.action_type_5);
            }
            return "";
        }
    }
}
