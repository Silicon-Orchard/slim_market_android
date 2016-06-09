package com.siliconorchard.walkitalkiechat.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import org.json.JSONException;

/**
 * Created by adminsiriconorchard on 4/27/16.
 */
public abstract class ActivityChannelBase extends ActivityBase {

    private TextView mTvTitle;
    private LinearLayout mLayoutBack;

    protected EditText mEtMyName;
    protected EditText mEtChannelNo;
    protected Button mBtnSubmit;
    protected RelativeLayout mLayoutLoading;

    protected String myIpAddress;
    protected SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initView();
        initListener();
    }

    private void initView() {
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            myIpAddress = bundle.getString(Constant.KEY_MY_IP_ADDRESS, null);
        }
        mSharedPref = getSharedPreferences(Constant.SHARED_PREF_NAME, MODE_PRIVATE);
        mTvTitle = (TextView) findViewById(R.id.tv_title_name);
        mLayoutBack = (LinearLayout) findViewById(R.id.layout_back);
        mTvTitle.setText(getTitleText());
        mEtMyName = (EditText) findViewById(R.id.et_my_name);
        mEtChannelNo = (EditText) findViewById(R.id.et_channel_no);
        mEtMyName.setText(Utils.getDeviceName(mSharedPref));
        mBtnSubmit = (Button) findViewById(R.id.btn_submit);
        mLayoutLoading = (RelativeLayout) findViewById(R.id.rl_loading_panel);
        initSubView();
    }

    private void initListener() {
        mLayoutBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityChannelBase.this.finish();
            }
        });

        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        initSubListeners();
    }

    protected boolean isValidNameSaved() {
        String name = mEtMyName.getText().toString().trim();
        if(name == null || name.length()<1) {
            Toast.makeText(this, getString(R.string.error_name_must_not_empty),Toast.LENGTH_LONG).show();
            return false;
        }
        Utils.setDeviceName(mSharedPref, name);
        return true;
    }

    private void submit() {
        if(!isValidNameSaved()) {
            return;
        }
        String channelText = mEtChannelNo.getText().toString().trim();
        if(channelText == null || channelText.length() != 4 ) {
            Toast.makeText(this, getString(R.string.error_enter_four_digit_channel_no), Toast.LENGTH_LONG).show();
            return;
        }
        submitInfo(Integer.parseInt(channelText));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(myIpAddress == null || myIpAddress.length()<1) {
            Toast.makeText(this, R.string.error_ip_address_not_found, Toast.LENGTH_LONG).show();
            this.finish();
        }

        registerReceiver(receiver, new IntentFilter(Constant.SERVICE_NOTIFICATION_STRING_CHAT_FOREGROUND));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    protected abstract int getLayoutId();
    protected abstract void initSubView();
    protected abstract void initSubListeners();
    protected abstract String getTitleText();
    protected abstract void submitInfo(int channelNo);
    protected abstract void onDuplicateFound(ChatMessage chatMessage);
    protected abstract void onChannelFound(ChatMessage chatMessage);

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String message = bundle.getString(Constant.KEY_CLIENT_MESSAGE);
                try {
                    ChatMessage chatMessage = new ChatMessage(message);
                    switch (chatMessage.getType()) {
                        case ChatMessage.TYPE_CHANNEL_DUPLICATE:
                            onDuplicateFound(chatMessage);
                            break;
                        case ChatMessage.TYPE_CHANNEL_FOUND:
                            onChannelFound(chatMessage);
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
