package com.siliconorchard.walkitalkiechat.activities;

import android.os.Bundle;
import android.widget.EditText;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import org.json.JSONException;

/**
 * Created by adminsiriconorchard on 4/29/16.
 */
public class ActivityChatPublicChannel extends ChatActivityAbstract{

    private String channelName;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initSubView(Bundle bundle) {
        if(channelNumber == Constant.PUBLIC_CHANNEL_NUMBER_A) {
            channelName = getString(R.string.public_channel_a);
        } else {
            channelName = getString(R.string.public_channel_b);
        }
        mTvTitle.setText(channelName);
        mEtChat = (EditText) findViewById(R.id.et_chat);
        //mBtnSend = (Button) findViewById(R.id.btn_send);
        //mTvClientMsg.setText(" ");
        //mTvRecipientList.setText("You joined\n");
        HostInfo channelHost = new HostInfo();
        channelHost.setDeviceName(Utils.getDeviceName(mSharedPref));
        channelHost.setIpAddress(myIpAddress);
        channelHost.setDeviceId(Utils.getDeviceId(this, mSharedPref));
        addToReceiverList(channelHost, ClientType.TYPE_JOINER);
        joinChannel();
    }

    @Override
    protected boolean isPrivateChannel() {
        return false;
    }

    @Override
    protected void processJoinChannelMessage(ChatMessage chatMessage) {
        HostInfo hostInfo = getHostInfoFromChatMessage(chatMessage);
        addToReceiverList(hostInfo, ClientType.TYPE_JOINER);
        sendChannelFoundMessage();
    }

    @Override
    protected void updateRecipientList(ChatMessage chatMessage) {
        HostInfo hostInfo = getHostInfoFromChatMessage(chatMessage);
        addToReceiverList(hostInfo, ClientType.TYPE_JOINER);
    }

    private void joinChannel() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(ChatMessage.TYPE_JOIN_CHANNEL);
        chatMessage.setIpAddress(myIpAddress);
        chatMessage.setDeviceName(Utils.getDeviceName(mSharedPref));
        chatMessage.setDeviceId(Utils.getDeviceId(this, mSharedPref));
        chatMessage.setChannelNumber(channelNumber);

        try {
            Utils.sendBroadCastMessageToRegisteredClients(chatMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean hasRecipient() {
        if(mListHostInfo == null || mListHostInfo.size() < 1) {
            return false;
        } else {
            return true;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
