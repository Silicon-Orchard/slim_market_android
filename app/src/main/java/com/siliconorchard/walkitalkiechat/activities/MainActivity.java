package com.siliconorchard.walkitalkiechat.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.discovery.Network.NetInfo;
import com.siliconorchard.walkitalkiechat.discovery.Utils.Prefs;
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
    private AlertDialog mAlertDialog;


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
        Utils.findDeviceID(this, mSharedPref);
        if(ipAddress == null || ipAddress.length()<5) {
            showWifiNotEnabledDialog();
        } else {
            sendBroadcastRequestInfo();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendBroadCastLeftMessage();
        Log.e("TAG_LOG", "Left message broad cast sent");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constant.READ_PHONE_STATE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Utils.getDeviceIdFromTelephonyManager(this, mSharedPref);

                } else {
                    Utils.setDeviceId(Constant.DEVICE_ID_UNKNOWN);
                    mSharedPref.edit().putString(Constant.KEY_DEVICE_ID, Constant.DEVICE_ID_UNKNOWN).commit();
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void sendBroadcastRequestInfo() {
        if(Utils.isInfoRequestSent()) {
            return;
        }
        try {
            ChatMessage chatMessage = generateChatMessageBasics();
            chatMessage.setType(ChatMessage.TYPE_REQUEST_INFO);
            Utils.sendBroadCastMessage(chatMessage);
            Utils.setIsInfoRequestSent(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    private void showWifiNotEnabledDialog() {
        AlertDialog.Builder  builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.wifi_not_enabled);
        builder.setMessage(R.string.error_wifi_not_enabled_please_enable);
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_info);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                    MainActivity.this.finish();
                }
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }
}

