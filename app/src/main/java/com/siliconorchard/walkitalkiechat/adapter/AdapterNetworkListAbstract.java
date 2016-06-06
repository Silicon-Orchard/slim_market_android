package com.siliconorchard.walkitalkiechat.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.model.HostInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adminsiriconorchard on 4/21/16.
 */
public abstract class AdapterNetworkListAbstract extends BaseAdapter {

    protected List<HostInfo> mList;
    protected Activity mActivity;
    protected LayoutInflater mInflater;

    public AdapterNetworkListAbstract(Activity activity, List<HostInfo> list) {
        this.mActivity = activity;
        this.mList = list;
        this.mInflater = activity.getLayoutInflater();
    }

    @Override
    public int getCount() {
        if(mList != null) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(mList != null && mList.size()>position) {
            return mList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void addToList(HostInfo hostInfo) {
        if(mList == null) {
            mList = new ArrayList<>();
        }
        mList.add(hostInfo);
    }

    public class ViewHolder {
        TextView tvName;
        TextView tvIp;
        TextView tvDeviceId;
        CheckBox checkBox;
    }
}
