package com.siliconorchard.walkitalkiechat.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import java.util.List;

/**
 * Created by adminsiriconorchard on 6/8/16.
 */
public class AdapterRecipientList extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<HostInfo> mListHostInfo;
    private Drawable drawableActive;
    private Drawable drawableInactive;

    public AdapterRecipientList(Activity activity, List<HostInfo> hostInfoList) {
        mInflater = LayoutInflater.from(activity);
        mListHostInfo = hostInfoList;
        drawableActive = activity.getResources().getDrawable( R.drawable.ic_member_active );
        drawableInactive = activity.getResources().getDrawable( R.drawable.ic_member_inactive );
    }

    @Override
    public int getCount() {
        if(mListHostInfo != null) {
            return mListHostInfo.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(mListHostInfo != null) {
            return mListHostInfo.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.item_recipient_list, null);
            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView)convertView.findViewById(R.id.tv_recipient);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        HostInfo hostInfo = mListHostInfo.get(position);
        viewHolder.textView.setText(hostInfo.getDeviceName());
        viewHolder.textView.setCompoundDrawablePadding(Utils.dpToPx(5));
        if(hostInfo.isChecked()) {
            viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds( drawableActive, null, null, null);
        } else {
            viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds( drawableInactive, null, null, null);
        }
        return convertView;
    }

    class ViewHolder {
        TextView textView;
    }
}
