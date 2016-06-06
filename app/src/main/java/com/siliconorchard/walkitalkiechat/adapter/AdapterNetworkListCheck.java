package com.siliconorchard.walkitalkiechat.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.model.HostInfo;

import java.util.List;

/**
 * Created by adminsiriconorchard on 4/18/16.
 */
public class AdapterNetworkListCheck extends AdapterNetworkListAbstract {


    public AdapterNetworkListCheck(Activity activity, List<HostInfo> list) {
        super(activity, list);
        this.mActivity = activity;
        this.mList = list;
        this.mInflater = activity.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.item_network_info_list, null);
            viewHolder = new ViewHolder();
            viewHolder.tvName = (TextView)convertView.findViewById(R.id.tv_name);
            viewHolder.tvIp = (TextView)convertView.findViewById(R.id.tv_ip_address);
            viewHolder.tvDeviceId = (TextView)convertView.findViewById(R.id.tv_device_id);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.cb_host_check);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        HostInfo hostInfoCheck = mList.get(position);
        viewHolder.tvName.setText(hostInfoCheck.getDeviceName());
        viewHolder.tvIp.setText(hostInfoCheck.getIpAddress());
        viewHolder.tvDeviceId.setText(hostInfoCheck.getDeviceId());
        viewHolder.checkBox.setChecked(hostInfoCheck.isChecked());
        viewHolder.checkBox.setTag(position);

        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int location = (int) buttonView.getTag();
                mList.get(location).setIsChecked(isChecked);
            }
        });

        return convertView;
    }
}
