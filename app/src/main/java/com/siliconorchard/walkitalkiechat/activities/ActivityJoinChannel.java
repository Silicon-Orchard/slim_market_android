package com.siliconorchard.walkitalkiechat.activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.asynctasks.SendMessageAsync;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.singleton.GlobalDataHolder;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by adminsiriconorchard on 4/27/16.
 */
public class ActivityJoinChannel extends ActivityChannelBase {

    private Button mBtnPublicChannelA;
    private Button mBtnPublicChannelB;

    private int channelNumber;
    private AlertDialog mAlertDialog;

    private boolean isChannelFound;

    private AsyncTask mWaitTask;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_join_channel;
    }

    @Override
    protected void initSubView() {
        mBtnPublicChannelA = (Button) findViewById(R.id.btn_channel_a);
        mBtnPublicChannelB = (Button) findViewById(R.id.btn_channel_b);
    }

    @Override
    protected void initSubListeners() {
        mBtnPublicChannelA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinPublicChannel(Constant.PUBLIC_CHANNEL_NUMBER_A);
            }
        });
        mBtnPublicChannelB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinPublicChannel(Constant.PUBLIC_CHANNEL_NUMBER_B);
            }
        });
    }

    @Override
    protected String getTitleText() {
        return getString(R.string.join_channel);
    }

    @Override
    protected void submitInfo(int channelNo) {
        isChannelFound = false;
        channelNumber = channelNo;
        joinChannel();
    }


    private void joinChannel() {
        mLayoutLoading.setVisibility(View.VISIBLE);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(ChatMessage.TYPE_JOIN_CHANNEL);
        chatMessage.setIpAddress(myIpAddress);
        chatMessage.setDeviceName(Utils.getDeviceName(mSharedPref));
        chatMessage.setDeviceId(Utils.getDeviceId(this, mSharedPref));
        chatMessage.setChannelNumber(channelNumber);

        try {
            HostInfo hostInfo = GlobalDataHolder.getInstance().getHostInfoByChannelNumber(channelNumber);
            if(hostInfo != null) {
                SendMessageAsync sendMessageAsync = new SendMessageAsync();
                String message = chatMessage.getJsonString();
                sendMessageAsync.execute(hostInfo, message);
            } else {
                Utils.sendBroadCastMessageToRegisteredClients(chatMessage);
            }
            waitForChannelResponse();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showNotFoundDialog() {
        mLayoutLoading.setVisibility(View.GONE);
        AlertDialog.Builder  builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error_not_found);
        builder.setMessage(R.string.error_channel_not_found_text);
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


    private void waitForChannelResponse() {
        mWaitTask = new AsyncTask<Void,Void,Void>() {
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
                if(!isChannelFound) {
                    mLayoutLoading.setVisibility(View.GONE);
                    showNotFoundDialog();
                }
            }
        }.execute();
    }


    private void joinPublicChannel(int channelNo) {
        String name = mEtMyName.getText().toString().trim();
        if(name == null || name.length()<1) {
            Toast.makeText(this, getString(R.string.error_name_must_not_empty),Toast.LENGTH_LONG).show();
            return;
        }
        Intent chatActivity = new Intent(this, ActivityChatPublicChannel.class);
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.KEY_CHANNEL_NUMBER, channelNo);
        bundle.putString(Constant.KEY_MY_IP_ADDRESS, myIpAddress);
        chatActivity.putExtras(bundle);
        startActivity(chatActivity);
    }

    @Override
    protected void onDuplicateFound(ChatMessage chatMessage) {

    }

    @Override
    protected void onChannelFound(ChatMessage chatMessage) {
        isChannelFound = true;
        if(mWaitTask != null) {
            mWaitTask.cancel(true);
        }
        ArrayList<HostInfo> hostInfoArrayList = new ArrayList<>();
        if(chatMessage.getClientList() != null) {
            hostInfoArrayList.addAll(chatMessage.getClientList());
        }
        Intent chatActivity = new Intent(this, ActivityChatPrivateChannel.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constant.KEY_HOST_INFO_LIST, hostInfoArrayList);
        bundle.putInt(Constant.KEY_CHANNEL_NUMBER, channelNumber);
        bundle.putString(Constant.KEY_MY_IP_ADDRESS, myIpAddress);
        chatActivity.putExtras(bundle);
        startActivity(chatActivity);
    }
}
