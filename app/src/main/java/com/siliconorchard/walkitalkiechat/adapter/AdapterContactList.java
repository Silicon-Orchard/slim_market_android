package com.siliconorchard.walkitalkiechat.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.model.HostInfo;

import java.util.List;

/**
 * Created by adminsiriconorchard on 6/14/16.
 */
public class AdapterContactList extends AdapterHostListBase {

    private Drawable drawableArrow;

    public AdapterContactList(Activity activity, List<HostInfo> hostInfoList) {
        super(activity, hostInfoList);
        mInflater = LayoutInflater.from(activity);
        mListHostInfo = hostInfoList;
        drawableArrow = activity.getResources().getDrawable(R.drawable.arrow_right);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_contact_list, null);
            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.tv_recipient);
            viewHolder.ivArrow = (ImageView) convertView.findViewById(R.id.iv_right_arrow);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        HostInfo hostInfo = mListHostInfo.get(position);
        viewHolder.textView.setText(hostInfo.getDeviceName());
        viewHolder.ivArrow.setImageDrawable(drawableArrow);
        return convertView;
    }
}
