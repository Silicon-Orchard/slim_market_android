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
import com.siliconorchard.walkitalkiechat.adapter.AdapterNetworkListCheck;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.singleton.GlobalDataHolder;
import com.siliconorchard.walkitalkiechat.utilities.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adminsiriconorchard on 4/21/16.
 */
public class DiscoveryActivityForResult extends DiscoveryActivityAbstract {

    private AdapterNetworkListCheck mAdapterHostList;
    protected List<HostInfo> mListHostInfo;
    private Button mBtnOk;
    private Button mBtnCancel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_disovery_for_result);
        initView();
        initListener();
    }

    private void initView() {
        ipAddress = getIntent().getExtras().getString(Constant.KEY_MY_IP_ADDRESS, null);

        btnDiscover = (Button) findViewById(R.id.btn_discover);
        mBtnOk = (Button) findViewById(R.id.btn_ok);
        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mLvHost = (ListView) findViewById(R.id.lv_host);
        mTvListEmpty = (TextView) findViewById(R.id.list_empty);
        mLayoutLoading = (RelativeLayout) findViewById(R.id.rl_loading_panel);
        if(GlobalDataHolder.getInstance().getListHostInfo() != null) {
            mListHostInfo = GlobalDataHolder.getInstance().getListHostInfo();
        } else {
            mListHostInfo = new ArrayList<>();
        }
        mAdapterHostList = new AdapterNetworkListCheck(this, mListHostInfo);
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

            }
        });

        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = 0;
                if (mListHostInfo == null) {
                    Log.e("TAG_LOG", "Count: " + count);
                    return;
                }
                ArrayList<HostInfo> hostInfoList = new ArrayList<>();
                for (int i = 0; i < mListHostInfo.size(); i++) {
                    if (mListHostInfo.get(i).isChecked()) {
                        count++;
                        HostInfo hostInfo = mListHostInfo.get(i);
                        hostInfo.setIsChecked(false);
                        hostInfoList.add(hostInfo);
                    }
                }
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(Constant.KEY_HOST_INFO_LIST, hostInfoList);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                DiscoveryActivityForResult.this.finish();
                Log.e("TAG_LOG", "Count: " + count);
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                DiscoveryActivityForResult.this.finish();
            }
        });
    }


    @Override
    protected void setInfo() {

    }


    @Override
    protected void initList() {
        mListHostInfo = new ArrayList<>();
        mAdapterHostList = new AdapterNetworkListCheck(this, mListHostInfo);
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