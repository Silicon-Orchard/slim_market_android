package com.siliconorchard.walkitalkiechat.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.asynctasks.SendMessageAsync;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import org.json.JSONException;

/**
 * Created by adminsiriconorchard on 6/14/16.
 */
public class ActivityChatOne2One extends ChatActivityAbstract{

    private HostInfo mHostInfo;
    private AlertDialog mAlertDialog;

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
        boolean isOnline = mHostInfo.isOnline();
        addToReceiverList(mHostInfo, ClientType.TYPE_JOINER);
        mHostInfo.setIsOnline(isOnline);
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
        if(!doNotInitThread) {
            registerReceiver(receiverChatAccept, new IntentFilter(Constant.RECEIVER_NOTIFICATION_CHAT_ACCEPT));
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiverChatAccept);
    }

    public boolean isSameHost(HostInfo hostInfo) {
        if(hostInfo.getIpAddress().equals(mHostInfo.getIpAddress())) {
            sendChatAcceptMessage();
            mHostInfo.setIsOnline(true);
            adapterRecipientList.notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    private void chatRequestNotification(Bundle bundle) {
        HostInfo hostInfo = (HostInfo) bundle.getParcelable(Constant.KEY_HOST_INFO);
        if(hostInfo.getIpAddress().equals(mHostInfo.getIpAddress())) {
            if(hostInfo.isOnline()) {
                mHostInfo.setIsOnline(true);
                adapterRecipientList.notifyDataSetChanged();
            } else {
                String title = getString(R.string.decline);
                String message = String.format(getString(R.string._declined_your_chat_request),hostInfo.getDeviceName());
                AlertDialog.Builder  builder = Utils.createAlertDialog(this, title, message);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mAlertDialog != null) {
                            mAlertDialog.dismiss();
                            ActivityChatOne2One.this.finish();
                        }
                    }
                });
                mAlertDialog = builder.create();
                mAlertDialog.show();
                mAlertDialog.getWindow().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.shape_voice_activity_bg));
            }
        }
    }

    private void sendChatAcceptMessage() {
        try {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setIpAddress(myIpAddress);
            chatMessage.setDeviceId(Utils.getDeviceId(this, mSharedPref));
            chatMessage.setDeviceName(Utils.getDeviceName(mSharedPref));
            chatMessage.setType(ChatMessage.TYPE_ONE_TO_ONE_CHAT_ACCEPT);
            String message = chatMessage.getJsonString();
            if (mHostInfo != null) {
                SendMessageAsync sendMessageAsync = new SendMessageAsync();
                sendMessageAsync.execute(mHostInfo, message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver receiverChatAccept = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                chatRequestNotification(bundle);
            }
        }
    };
}
