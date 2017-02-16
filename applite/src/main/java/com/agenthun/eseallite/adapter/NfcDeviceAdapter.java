package com.agenthun.eseallite.adapter;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.agenthun.eseallite.R;
import com.agenthun.eseallite.utils.DeviceSearchSuggestion;

import java.util.List;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/9 上午1:27.
 */
public class NfcDeviceAdapter extends RecyclerView.Adapter<NfcDeviceAdapter.DeviceViewHolder> {
    private static final String TAG = "NfcDeviceAdapter";
    private List<DeviceSearchSuggestion> devices;

    public NfcDeviceAdapter(List<DeviceSearchSuggestion> devices) {
        this.devices = devices;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_device_one_line, null);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, final int position) {
        int type = devices.get(position).getType();
        holder.deviceType.setImageLevel(type);
        holder.deviceName.setText(devices.get(position).getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(devices.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public DeviceSearchSuggestion getItem(int position) {
        return devices.get(position);
    }

    public void clearAllDatas() {
        devices.clear();
        notifyDataSetChanged();
    }

    public void updateAllDatas(List<DeviceSearchSuggestion> devices) {
        this.devices = devices;
        notifyDataSetChanged();
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder {
        private ImageView deviceType;
        private AppCompatTextView deviceName;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            deviceType = (ImageView) itemView.findViewById(R.id.device_type);
            deviceName = (AppCompatTextView) itemView.findViewById(R.id.device_name);
        }
    }

    //itemClick interface
    public interface OnItemClickListener {
        void onItemClick(DeviceSearchSuggestion deviceSearchSuggestion);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
}
