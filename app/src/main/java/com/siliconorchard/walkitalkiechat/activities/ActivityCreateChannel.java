package com.siliconorchard.walkitalkiechat.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.model.ChannelInfo;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.singleton.GlobalDataHolder;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import org.json.JSONException;

/**
 * Created by adminsiriconorchard on 4/27/16.
 */
public class ActivityCreateChannel extends ActivityChannelBase {

    private boolean isDuplicateFound;
    private int channelNumber;
    private AlertDialog mAlertDialog;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_create_channel;
    }

    @Override
    protected void initSubView() {

    }

    @Override
    protected void initSubListeners() {

    }

    @Override
    protected void submitInfo(int channelNo) {
        isDuplicateFound = false;
        channelNumber = channelNo;
        createChannel();
    }

    @Override
    protected String getTitleText() {
        return getString(R.string.create_channel);
    }

    @Override
    protected void onDuplicateFound(ChatMessage chatMessage) {
        if(isDuplicateFound) {
            return;
        }
        isDuplicateFound = true;
        showDuplicateDialog();
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setIpAddress(chatMessage.getIpAddress());
        channelInfo.setDeviceId(chatMessage.getDeviceId());
        channelInfo.setDeviceName(chatMessage.getDeviceName());
        channelInfo.setChannelNumber(chatMessage.getChannelNumber());
        GlobalDataHolder.getInstance().addToOtherChannelList(channelInfo);
    }

    private void showDuplicateDialog() {
        mLayoutLoading.setVisibility(View.GONE);
        AlertDialog.Builder  builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.already_exists);
        builder.setMessage(R.string.error_channel_no_already_exists);
        builder.setIcon(R.drawable.ic_info);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                }
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    private void createChannel() {
        mLayoutLoading.setVisibility(View.VISIBLE);
        if(GlobalDataHolder.getInstance().isChannelExists(channelNumber)) {
            showDuplicateDialog();
            mLayoutLoading.setVisibility(View.GONE);
            isDuplicateFound = true;
            return;
        }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setIpAddress(myIpAddress);
        chatMessage.setDeviceName(Utils.getDeviceName(mSharedPref));
        chatMessage.setDeviceId(Utils.getDeviceId(this, mSharedPref));
        chatMessage.setType(ChatMessage.TYPE_CREATE_CHANNEL);
        chatMessage.setChannelNumber(channelNumber);
        try {
            Utils.sendBroadCastMessageToRegisteredClients(chatMessage);
            waitForDuplicateResponse();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void waitForDuplicateResponse() {
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(Constant.WAITING_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mLayoutLoading.setVisibility(View.GONE);
                if(!isDuplicateFound) {
                    startChatting();
                }
            }
        }.execute();
    }

    @Override
    protected void onChannelFound(ChatMessage chatMessage) {

    }

    private void startChatting() {
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setIpAddress(myIpAddress);
        channelInfo.setDeviceId(Utils.getDeviceId(this, mSharedPref));
        channelInfo.setDeviceName(Utils.getDeviceName(mSharedPref));
        channelInfo.setChannelNumber(channelNumber);
        GlobalDataHolder.getInstance().addToMyChannelList(channelInfo);

        Intent chatActivity = new Intent(this, ActivityChatPrivateChannel.class);
        chatActivity.putExtra(Constant.KEY_MY_IP_ADDRESS, myIpAddress);
        chatActivity.putExtra(Constant.KEY_CHANNEL_NUMBER, channelNumber);
        startActivity(chatActivity);
    }
}
