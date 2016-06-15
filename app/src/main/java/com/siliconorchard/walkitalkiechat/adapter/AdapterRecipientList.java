package com.siliconorchard.walkitalkiechat.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import java.util.List;

/**
 * Created by adminsiriconorchard on 6/8/16.
 */
public class AdapterRecipientList extends AdapterHostListBase {

    protected Drawable drawableActive;
    protected Drawable drawableInactive;

    public AdapterRecipientList(Activity activity, List<HostInfo> hostInfoList) {
        super(activity, hostInfoList);
        drawableActive = activity.getResources().getDrawable( R.drawable.ic_member_active );
        drawableInactive = activity.getResources().getDrawable( R.drawable.ic_member_inactive );
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
        if(hostInfo.isOnline()) {
            viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds( drawableActive, null, null, null);
        } else {
            viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds( drawableInactive, null, null, null);
        }
        return convertView;
    }
}
