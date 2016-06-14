package com.siliconorchard.walkitalkiechat.activities;

import android.os.Bundle;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.utilities.Constant;

/**
 * Created by adminsiriconorchard on 6/14/16.
 */
public class ActivityChatOne2One extends ChatActivityAbstract{

    @Override
    protected int getLayoutID() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initSubView(Bundle bundle) {
        HostInfo hostInfo = (HostInfo) bundle.getParcelable(Constant.KEY_HOST_INFO);
        if(hostInfo != null) {
            addToReceiverList(hostInfo, ClientType.TYPE_JOINER);
        }
        mTvTitle.setText(hostInfo.getDeviceName());
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
        if(mListHostInfo == null || mListHostInfo.size() < 1) {
            return false;
        } else {
            return true;
        }
    }
}
