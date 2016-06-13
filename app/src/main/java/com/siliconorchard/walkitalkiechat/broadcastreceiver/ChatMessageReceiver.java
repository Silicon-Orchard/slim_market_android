package com.siliconorchard.walkitalkiechat.broadcastreceiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.siliconorchard.walkitalkiechat.AppController;
import com.siliconorchard.walkitalkiechat.activities.ChatActivity;
import com.siliconorchard.walkitalkiechat.asynctasks.SendMessageAsync;
import com.siliconorchard.walkitalkiechat.model.ChannelInfo;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.singleton.GlobalDataHolder;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import org.json.JSONException;

/**
 * Created by adminsiriconorchard on 4/18/16.
 */
public class ChatMessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String message = bundle.getString(Constant.KEY_CLIENT_MESSAGE);
            if(message != null) {
                Log.e("TAG_LOG","Message: "+message);
            } else {
                Log.e("TAG_LOG","Blank message found");
                return;
            }
            try {
                ChatMessage chatMessage = new ChatMessage(message);
                /*Activity currentActivity = ((AppController)context.getApplicationContext()).getCurrentActivity();
                if(currentActivity instanceof ChatActivity
                        && chatMessage.getType() != ChatMessage.TYPE_REQUEST_INFO
                        && chatMessage.getType() != ChatMessage.TYPE_CREATE_CHANNEL
                        && chatMessage.getType() != ChatMessage.TYPE_JOIN_CHANNEL) {
                    return;
                }*/
                processMessage(context, chatMessage);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processMessage(Context context, ChatMessage receivedMessage) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.SHARED_PREF_NAME, Activity.MODE_PRIVATE);
        String ipAddress = sharedPreferences.getString(Constant.KEY_MY_IP_ADDRESS, null);
        if(ipAddress == null) {
            Log.e("TAG_LOG", "Self Ip address not found");
            return;
        }
        HostInfo hostInfo = new HostInfo();
        hostInfo.setIpAddress(receivedMessage.getIpAddress());
        SendMessageAsync sendMessageAsync = new SendMessageAsync();

        switch (receivedMessage.getType()) {
            case ChatMessage.TYPE_MESSAGE:
                //Toast.makeText(context, "New message arrived", Toast.LENGTH_LONG).show();
                break;
            case ChatMessage.TYPE_ADD_CLIENT:
                break;
            case ChatMessage.TYPE_REQUEST_INFO:
                //Toast.makeText(context, "New message arrived", Toast.LENGTH_LONG).show();
                try {
                    HostInfo hInfo = Utils.getHostInfoFromChatMessage(receivedMessage);
                    if(GlobalDataHolder.getInstance().addToHostList(hInfo)) {
                        publishContactModifyNotification(context);
                    }
                    ChatMessage sendingMessage = generateChatMessage(sharedPreferences, ipAddress, ChatMessage.TYPE_RECEIVE_INFO, 0);
                    Log.e("TAG_LOG", sendingMessage.getJsonString());
                    sendMessageAsync.execute(hostInfo, sendingMessage.getJsonString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case ChatMessage.TYPE_RECEIVE_INFO:
                try {
                    HostInfo hInfo = Utils.getHostInfoFromChatMessage(receivedMessage);
                    if(GlobalDataHolder.getInstance().addToHostList(hInfo)) {
                        publishContactModifyNotification(context);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case ChatMessage.TYPE_CREATE_CHANNEL:
                //Toast.makeText(context, "New message arrived", Toast.LENGTH_LONG).show();
                if(!GlobalDataHolder.getInstance().isChannelExistsInMyChannels(receivedMessage.getChannelNumber())) {
                    if(!GlobalDataHolder.getInstance().isChannelExistsInOtherChannels(receivedMessage.getChannelNumber())) {
                        ChannelInfo channelInfo = new ChannelInfo();
                        channelInfo.setIpAddress(receivedMessage.getIpAddress());
                        channelInfo.setDeviceId(receivedMessage.getDeviceId());
                        channelInfo.setDeviceName(receivedMessage.getDeviceName());
                        channelInfo.setChannelNumber(receivedMessage.getChannelNumber());
                        GlobalDataHolder.getInstance().addToOtherChannelList(channelInfo);
                    }
                    return;
                }

                try {
                    ChatMessage sendingMessage = generateChatMessage(sharedPreferences, ipAddress, ChatMessage.TYPE_CHANNEL_DUPLICATE, receivedMessage.getChannelNumber());
                    Log.e("TAG_LOG", sendingMessage.getJsonString());
                    sendMessageAsync.execute(hostInfo, sendingMessage.getJsonString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case ChatMessage.TYPE_LEFT_APPLICATION:
                try {
                    HostInfo hInfo = Utils.getHostInfoFromChatMessage(receivedMessage);
                    if(GlobalDataHolder.getInstance().removeFromHostList(hInfo)) {
                        publishContactModifyNotification(context);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private ChatMessage generateChatMessage(SharedPreferences sharedPreferences, String ipAddress, int type, int channelNumber) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(type);
        chatMessage.setIpAddress(ipAddress);
        chatMessage.setDeviceId(sharedPreferences.getString(Constant.KEY_DEVICE_ID, Constant.DEVICE_ID_UNKNOWN));
        chatMessage.setDeviceName(sharedPreferences.getString(Constant.KEY_MY_DEVICE_NAME, Constant.DEVICE_ID_UNKNOWN));
        switch (type) {
            case ChatMessage.TYPE_CHANNEL_DUPLICATE:
                chatMessage.setChannelNumber(channelNumber);
                break;
        }
        return chatMessage;
    }

    private void publishContactModifyNotification(Context context) {
        Intent intentContactModified = new Intent(Constant.RECEIVER_NOTIFICATION_CONTACT_LIST_MODIFIED);
        intentContactModified.putExtra(Constant.KEY_IS_CONTACT_MODIFIED, true);
        context.sendBroadcast(intentContactModified);
    }
}
