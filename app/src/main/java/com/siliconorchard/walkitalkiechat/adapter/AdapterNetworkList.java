package com.siliconorchard.walkitalkiechat.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.model.HostInfo;


import java.util.List;

/**
 * Created by adminsiriconorchard on 4/18/16.
 */
public class AdapterNetworkList extends AdapterNetworkListAbstract {

    public AdapterNetworkList(Activity activity, List<HostInfo> list) {
        super(activity, list);
        this.mActivity = activity;
        this.mList = list;
        this.mInflater = activity.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.list_host, null);
            viewHolder = new ViewHolder();
            viewHolder.tvName = (TextView)convertView.findViewById(R.id.tv_name);
            viewHolder.tvIp = (TextView)convertView.findViewById(R.id.tv_ip_address);
            viewHolder.tvDeviceId = (TextView)convertView.findViewById(R.id.tv_device_id);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        HostInfo hostInfo = mList.get(position);
        viewHolder.tvName.setText(hostInfo.getDeviceName());
        viewHolder.tvIp.setText(hostInfo.getIpAddress());
        viewHolder.tvDeviceId.setText(hostInfo.getDeviceId());

        return convertView;
    }
}
