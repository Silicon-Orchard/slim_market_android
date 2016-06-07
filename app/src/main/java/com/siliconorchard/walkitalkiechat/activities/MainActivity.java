package com.siliconorchard.walkitalkiechat.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import org.json.JSONException;


public class MainActivity extends ActivityBase {

    private Button mBtnJoinChannel;
    private Button mBtnCreateChannel;

    private String ipAddress;
    private SharedPreferences mSharedPref;


    private TextView mTvTitle;
    private LinearLayout mLayoutBack;
    private LinearLayout mLayoutRight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
    }

    private void initView() {
        mSharedPref = getSharedPreferences(Constant.SHARED_PREF_NAME, MODE_PRIVATE);
        mBtnJoinChannel = (Button) findViewById(R.id.btn_join_channel);
        mBtnCreateChannel = (Button) findViewById(R.id.btn_create_channel);
        mTvTitle = (TextView) findViewById(R.id.tv_title_name);
        mLayoutBack = (LinearLayout) findViewById(R.id.layout_back);
        mLayoutRight = (LinearLayout) findViewById(R.id.layout_map_icon);
        mLayoutBack.setVisibility(View.GONE);
        mLayoutRight.setVisibility(View.GONE);
        mTvTitle.setText(R.string.main_menu);
        ipAddress = Utils.getDeviceIpAddress();
    }

    private void initListener() {
        mBtnJoinChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startJoinChannelActivity();
            }
        });
        mBtnCreateChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreateChannelActivity();
            }
        });
    }

    private void startChatting() {
        Intent chatActivity = new Intent(MainActivity.this, ChatActivity.class);
        chatActivity.putExtra(Constant.KEY_MY_IP_ADDRESS, ipAddress);
        startActivity(chatActivity);
    }

    private void startCreateChannelActivity() {
        Intent chatActivity = new Intent(this, ActivityCreateChannel.class);
        chatActivity.putExtra(Constant.KEY_MY_IP_ADDRESS, ipAddress);
        startActivity(chatActivity);
    }

    private void startJoinChannelActivity() {
        Intent chatActivity = new Intent(this, ActivityJoinChannel.class);
        chatActivity.putExtra(Constant.KEY_MY_IP_ADDRESS, ipAddress);
        startActivity(chatActivity);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendBroadCastLeftMessage();
        Log.e("TAG_LOG", "Left message broad cast sent");
    }

    private void sendBroadCastLeftMessage() {
        Utils.setIsInfoRequestSent(false);
        try {
            ChatMessage chatMessage = generateChatMessageBasics();
            chatMessage.setType(ChatMessage.TYPE_LEFT_APPLICATION);
            Utils.sendBroadCastMessageToRegisteredClients(chatMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ChatMessage generateChatMessageBasics() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setDeviceId(Utils.getDeviceId(this, mSharedPref));
        chatMessage.setIpAddress(ipAddress);
        chatMessage.setDeviceName(Utils.getDeviceName(mSharedPref));
        return chatMessage;
    }

}

