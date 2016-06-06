package com.siliconorchard.walkitalkiechat.activities;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.adapter.AdapterNetworkList;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.singleton.GlobalDataHolder;
import com.siliconorchard.walkitalkiechat.utilities.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adminsiriconorchard on 4/18/16.
 */
public class DiscoveryActivity extends DiscoveryActivityAbstract {

    private AdapterNetworkList mAdapterHostList;
    protected List<HostInfo> mListHostInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.discovery);
        initView();
        initListener();
    }

    private void initView() {
        ipAddress = getIntent().getExtras().getString(Constant.KEY_MY_IP_ADDRESS, null);
        // Discover
        btnDiscover = (Button) findViewById(R.id.btn_discover);
        mLvHost = (ListView) findViewById(R.id.lv_host);
        mTvListEmpty = (TextView) findViewById(R.id.list_empty);
        mLayoutLoading = (RelativeLayout) findViewById(R.id.rl_loading_panel);
        if(GlobalDataHolder.getInstance().getListHostInfo() != null) {
            mListHostInfo = GlobalDataHolder.getInstance().getListHostInfo();
        } else {
            mListHostInfo = new ArrayList<>();
        }
        mAdapterHostList = new AdapterNetworkList(this, mListHostInfo);
        mLvHost.setAdapter(mAdapterHostList);
    }

    private void initListener() {
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startDiscovering();
            }
        });
        mLvHost.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("TAG_LOG","On Item Clicked");
                if (ipAddress != null && ipAddress.length()>0) {
                    Intent chatActivity = new Intent(DiscoveryActivity.this, ChatActivity.class);
                    chatActivity.putExtra(Constant.KEY_MY_IP_ADDRESS, ipAddress);
                    chatActivity.putExtra(Constant.KEY_HOST_INFO, mListHostInfo.get(position));
                    startActivity(chatActivity);
                }
            }
        });
    }


    @Override
    protected void setInfo() {
        // Info
        ((TextView) findViewById(R.id.info_ip)).setText(info_ip_str);
        ((TextView) findViewById(R.id.info_in)).setText(info_in_str);
        ((TextView) findViewById(R.id.info_mo)).setText(info_mo_str);
    }


    @Override
    protected void initList() {
        mListHostInfo = new ArrayList<>();
        mAdapterHostList = new AdapterNetworkList(this, mListHostInfo);
        mLvHost.setAdapter(mAdapterHostList);
    }

    @Override
    protected void addToHostList(ChatMessage chatMessage) {
        mTvListEmpty.setVisibility(View.GONE);
        HostInfo hostInfo = new HostInfo();
        hostInfo.setIpAddress(chatMessage.getIpAddress());
        hostInfo.setDeviceId(chatMessage.getDeviceId());
        hostInfo.setDeviceName(chatMessage.getDeviceName());
        mListHostInfo.add(hostInfo);
        GlobalDataHolder.getInstance().setListHostInfo(mListHostInfo);
        mAdapterHostList.notifyDataSetChanged();
    }

    @Override
    protected void onSearchFinished() {
        if(mListHostInfo != null && mListHostInfo.size()>0) {
            GlobalDataHolder.getInstance().setListHostInfo(mListHostInfo);
        } else {
            mTvListEmpty.setText(R.string.no_device_found);
            mTvListEmpty.setVisibility(View.VISIBLE);
        }
    }
}
