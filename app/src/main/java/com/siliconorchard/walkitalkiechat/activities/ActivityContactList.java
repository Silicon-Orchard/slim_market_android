package com.siliconorchard.walkitalkiechat.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.adapter.AdapterRecipientList;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.singleton.GlobalDataHolder;
import com.siliconorchard.walkitalkiechat.utilities.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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

    private AdapterRecipientList adapterRecipientList;

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
    }

    private void initContactList() {
        mListHostInfo = GlobalDataHolder.getInstance().getListHostInfo();
        adapterRecipientList = new AdapterRecipientList(this, mListHostInfo);
        mLvRecipientList.setAdapter(adapterRecipientList);
    }

    private void updateContactList() {
        mListHostInfo = GlobalDataHolder.getInstance().getListHostInfo();
        adapterRecipientList.notifyDataSetChanged();
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
}
