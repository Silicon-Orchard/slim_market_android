package com.siliconorchard.walkitalkiechat.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.model.HostInfo;

import java.util.List;

/**
 * Created by adminsiriconorchard on 6/14/16.
 */
public abstract class AdapterHostListBase extends BaseAdapter {

    protected LayoutInflater mInflater;
    protected List<HostInfo> mListHostInfo;

    public AdapterHostListBase(Activity activity, List<HostInfo> hostInfoList) {
        mInflater = LayoutInflater.from(activity);
        mListHostInfo = hostInfoList;
    }

    @Override
    public int getCount() {
        if (mListHostInfo != null) {
            return mListHostInfo.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mListHostInfo != null) {
            return mListHostInfo.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    class ViewHolder {
        TextView textView;
        ImageView ivArrow;
    }
}