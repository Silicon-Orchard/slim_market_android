package com.siliconorchard.walkitalkiechat.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.adapter.AdapterContactList;
import com.siliconorchard.walkitalkiechat.asynctasks.SendMessageAsync;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.singleton.GlobalDataHolder;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import org.json.JSONException;

import java.util.List;

/**
 * Created by adminsiriconorchard on 6/13/16.
 */
public class ActivityContactList extends ActivityBase {

    private TextView mTvTitle;
    private LinearLayout mLayoutBack;

    private List<HostInfo> mListHostInfo;

    private SharedPreferences mSharedPref;
    private String myIpAddress;
    private ListView mLvRecipientList;

    private AdapterContactList adapterContactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        initView();
        initListeners();
    }

    private void initView() {
        mSharedPref = getSharedPreferences(Constant.SHARED_PREF_NAME, MODE_PRIVATE);
        Bundle bundle = getIntent().getExtras();
        myIpAddress = bundle.getString(Constant.KEY_MY_IP_ADDRESS, null);


        mTvTitle = (TextView) findViewById(R.id.tv_title_name);
        mLayoutBack = (LinearLayout) findViewById(R.id.layout_back);
        mTvTitle.setText(R.string.contact_list);

        mLvRecipientList = (ListView) findViewById(R.id.lv_recipient_list);
        initContactList();
    }

    private void initListeners() {
        mLayoutBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityContactList.this.finish();
            }
        });
        mLvRecipientList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HostInfo hostInfo = mListHostInfo.get(position);
                sendChatRequestMessage(hostInfo);
                Intent intent = new Intent(ActivityContactList.this, ActivityChatOne2One.class);
                intent.putExtra(Constant.KEY_HOST_INFO, hostInfo);
                intent.putExtra(Constant.KEY_CHANNEL_NUMBER, 0);
                intent.putExtra(Constant.KEY_MY_IP_ADDRESS, myIpAddress);
                startActivity(intent);
            }
        });
    }

    private void initContactList() {
        mListHostInfo = GlobalDataHolder.getInstance().getListHostInfo();
        adapterContactList = new AdapterContactList(this, mListHostInfo);
        mLvRecipientList.setAdapter(adapterContactList);
    }

    private void updateContactList() {
        mListHostInfo = GlobalDataHolder.getInstance().getListHostInfo();
        adapterContactList.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(Constant.RECEIVER_NOTIFICATION_CONTACT_LIST_MODIFIED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                boolean isModified = bundle.getBoolean(Constant.KEY_IS_CONTACT_MODIFIED);
                if(isModified) {
                    updateContactList();
                }
            }
        }
    };

    private void sendChatRequestMessage(HostInfo hostInfo) {
        try {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setIpAddress(myIpAddress);
            chatMessage.setDeviceId(Utils.getDeviceId(this, mSharedPref));
            chatMessage.setDeviceName(Utils.getDeviceName(mSharedPref));
            chatMessage.setType(ChatMessage.TYPE_ONE_TO_ONE_CHAT_REQUEST);
            String message = chatMessage.getJsonString();
            if (hostInfo != null ) {
                SendMessageAsync sendMessageAsync = new SendMessageAsync();
                sendMessageAsync.execute(hostInfo, message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
