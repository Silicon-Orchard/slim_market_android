package com.siliconorchard.walkitalkiechat.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.utilities.Constant;

/**
 * Created by adminsiriconorchard on 6/14/16.
 */
public class ActivityChatOne2One extends ChatActivityAbstract{

    private HostInfo mHostInfo;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initSubView(Bundle bundle) {
        mHostInfo = (HostInfo) bundle.getParcelable(Constant.KEY_HOST_INFO);
        if(mHostInfo == null) {
            this.finish();
        }
        addToReceiverList(mHostInfo, ClientType.TYPE_JOINER);
        mTvTitle.setText(mHostInfo.getDeviceName());
    }

    @Override
    protected boolean isPrivateChannel() {
        return true;
    }

    @Override
    protected void processJoinChannelMessage(ChatMessage chatMessage) {

    }

    @Override
    protected void updateRecipientList(ChatMessage chatMessage) {

    }

    @Override
    protected boolean hasRecipient() {
        if(!mHostInfo.isOnline()) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiverChatAccept, new IntentFilter(Constant.RECEIVER_NOTIFICATION_CHAT_ACCEPT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiverChatAccept);
    }

    public boolean isSameHost(HostInfo hostInfo) {
        if(hostInfo.getIpAddress().equals(mHostInfo.getIpAddress())) {
            mHostInfo.setIsOnline(true);
            return true;
        } else {
            return false;
        }
    }
    private void showChatRequestDialog(Bundle bundle) {
        HostInfo hostInfo = (HostInfo) bundle.getParcelable(Constant.KEY_HOST_INFO);
        if(hostInfo.getIpAddress().equals(mHostInfo.getIpAddress())) {
            mHostInfo.setIsOnline(true);
        }
    }

    private BroadcastReceiver receiverChatAccept = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                showChatRequestDialog(bundle);
            }
        }
    };
}
