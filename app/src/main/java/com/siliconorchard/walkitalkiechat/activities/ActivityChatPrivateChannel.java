package com.siliconorchard.walkitalkiechat.activities;

import android.os.Bundle;
import android.os.Parcelable;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.singleton.GlobalDataHolder;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adminsiriconorchard on 4/29/16.
 */
public class ActivityChatPrivateChannel extends ChatActivityAbstract{

    @Override
    protected int getLayoutID() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initSubView(Bundle bundle) {
        ArrayList<Parcelable> parcelableList = bundle.getParcelableArrayList(Constant.KEY_HOST_INFO_LIST);
        if(parcelableList != null && parcelableList.size()>0) {
            int size = parcelableList.size();
            HostInfo channelHost = (HostInfo) parcelableList.get(0);
            addToReceiverList(channelHost, ClientType.TYPE_CREATOR);
            for(int i = 1; i<size; i++) {
                HostInfo client = (HostInfo) parcelableList.get(i);
                addToReceiverList(client, ClientType.TYPE_JOINER);
            }
        } else {
            HostInfo channelHost = new HostInfo();
            channelHost.setDeviceName(Utils.getDeviceName(mSharedPref));
            channelHost.setIpAddress(myIpAddress);
            channelHost.setDeviceId(Utils.getDeviceId(this, mSharedPref));
            addToReceiverList(channelHost, ClientType.TYPE_CREATOR);
        }
    }

    @Override
    protected boolean isPrivateChannel() {
        return true;
    }

    @Override
    protected void processJoinChannelMessage(ChatMessage chatMessage) {
        if(!GlobalDataHolder.getInstance().isChannelExistsInMyChannels(chatMessage.getChannelNumber())) {
            return;
        }
        HostInfo hostInfo = getHostInfoFromChatMessage(chatMessage);
        addToReceiverList(hostInfo, ClientType.TYPE_JOINER);
        sendChannelFoundMessage();
    }

    @Override
    protected void updateRecipientList(ChatMessage chatMessage) {
        List<HostInfo> clientList = chatMessage.getClientList();
        if(clientList != null && clientList.size()>0) {
            HostInfo hostInfo = clientList.get(clientList.size()-1);
            addToReceiverList(hostInfo, ClientType.TYPE_JOINER);
        }
    }

    @Override
    protected boolean hasRecipient() {
        if(mListHostInfo == null || mListHostInfo.size() < 2) {
            return false;
        } else {
            return true;
        }
    }
}
