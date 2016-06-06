package com.siliconorchard.walkitalkiechat.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.asynctasks.AbstractHostAsync;
import com.siliconorchard.walkitalkiechat.asynctasks.SearchHostAsync;
import com.siliconorchard.walkitalkiechat.asynctasks.SendRequestMessageAsync;
import com.siliconorchard.walkitalkiechat.discovery.Network.HostBean;
import com.siliconorchard.walkitalkiechat.discovery.Network.NetInfo;
import com.siliconorchard.walkitalkiechat.discovery.Utils.Prefs;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.singleton.GlobalDataHolder;
import com.siliconorchard.walkitalkiechat.utilities.Constant;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adminsiriconorchard on 4/20/16.
 */
public abstract class DiscoveryActivityAbstract extends ActivityBase {


    private ConnectivityManager connMgr;

    protected final static String EXTRA_WIFI = "wifiDisabled";
    protected Context ctxt;
    protected SharedPreferences prefs = null;
    protected NetInfo net = null;
    protected String info_ip_str = "";
    protected String info_in_str = "";
    protected String info_mo_str = "";

    protected ListView mLvHost;
    protected TextView mTvListEmpty;


    protected int requestNumber;
    protected int processedNumber;

    protected ChatMessage mChatPingMessage;
    protected String mPingMessageText;


    protected final String TAG = "ActivityDiscovery";
    protected int currentNetwork = 0;
    protected long network_ip = 0;
    protected long network_start = 0;
    protected long network_end = 0;
    protected List<HostBean> hosts = null;
    protected Button btnDiscover;
    protected AbstractHostAsync mDiscoveryTask = null;

    protected String ipAddress;

    protected RelativeLayout mLayoutLoading;

    protected List<SendRequestMessageAsync> mRequestInfoAsyncList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctxt = getApplicationContext();
        prefs = getSharedPreferences(Constant.SHARED_PREF_NAME, MODE_PRIVATE);
        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        net = new NetInfo(ctxt);
    }

    protected abstract void setInfo();
    protected abstract void addToHostList(ChatMessage chatMessage);
    protected abstract void initList();
    protected abstract void onSearchFinished();

    protected void findNetworkInfo() {
        final NetworkInfo ni = connMgr.getActiveNetworkInfo();
        if (ni != null) {
            //Log.i(TAG, "NetworkState="+ni.getDetailedState());
            if (ni.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                int type = ni.getType();
                //Log.i(TAG, "NetworkType="+type);
                if (type == ConnectivityManager.TYPE_WIFI) { // WIFI
                    net.getWifiInfo();
                    if (net.ssid != null) {
                        net.getIp();
                        info_ip_str = getString(R.string.net_ip, net.ip, net.cidr, net.intf);
                        info_in_str = getString(R.string.net_ssid, net.ssid);
                        info_mo_str = getString(R.string.net_mode, getString(
                                R.string.net_mode_wifi, net.speed, WifiInfo.LINK_SPEED_UNITS));
                        setButtons(false);
                    }
                } else if (type == ConnectivityManager.TYPE_MOBILE) { // 3G
                    if (prefs.getBoolean(Prefs.KEY_MOBILE, Prefs.DEFAULT_MOBILE)
                            || prefs.getString(Prefs.KEY_INTF, Prefs.DEFAULT_INTF) != null) {
                        net.getMobileInfo();
                        if (net.carrier != null) {
                            net.getIp();
                            info_ip_str = getString(R.string.net_ip, net.ip, net.cidr, net.intf);
                            info_in_str = getString(R.string.net_carrier, net.carrier);
                            info_mo_str = getString(R.string.net_mode,
                                    getString(R.string.net_mode_mobile));
                            setButtons(false);
                        }
                    }
                } else if (type == 3 || type == 9) { // ETH
                    net.getIp();
                    info_ip_str = getString(R.string.net_ip, net.ip, net.cidr, net.intf);
                    info_in_str = "";
                    info_mo_str = getString(R.string.net_mode) + getString(R.string.net_mode_eth);
                    setButtons(false);
                    Log.i(TAG, "Ethernet connectivity detected!");
                } else {
                    Log.i(TAG, "Connectivity unknown!");
                    info_mo_str = getString(R.string.net_mode)
                            + getString(R.string.net_mode_unknown);
                }
            } else {
                cancelTasks();
            }
        } else {
            cancelTasks();
        }

        // Always update network info
        setInfo();
        initNetworkInfo();
    }


    public void increaseResponse() {
        processedNumber++;
        Log.e("TAG_LOG", "Processed/Requested: " + processedNumber + "/" + requestNumber);
        if(requestNumber == processedNumber) {
            makeToast(R.string.discover_finished);
            mLayoutLoading.setVisibility(View.GONE);
            stopDiscovering();
            onSearchFinished();
        }
    }

    /**
     * Discover hosts
     */
    protected void startDiscovering() {
        hosts = new ArrayList<HostBean>();
        requestNumber = processedNumber = 0;
        mLayoutLoading.setVisibility(View.VISIBLE);

        initList();

        int method = 0;
        try {
            method = Integer.parseInt(prefs.getString(Prefs.KEY_METHOD_DISCOVER,
                    Prefs.DEFAULT_METHOD_DISCOVER));
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getMessage());
        }
        switch (method) {
            case 1:
                //mDiscoveryTask = new DnsDiscovery(ActivityDiscovery.this);
                break;
            case 2:
                // Root
                break;
            case 0:
            default:
                SearchHostAsync defaultDiscovery = new SearchHostAsync();
                defaultDiscovery.setDoRateControl(prefs.getBoolean(Prefs.KEY_RATECTRL_ENABLE,
                        Prefs.DEFAULT_RATECTRL_ENABLE));
                defaultDiscovery.setCustomRate(Integer.parseInt(prefs.getString(Prefs.KEY_TIMEOUT_DISCOVER,
                        Prefs.DEFAULT_TIMEOUT_DISCOVER)));
                mDiscoveryTask = defaultDiscovery;//new DefaultDiscovery(ActivityDiscovery.this);
        }
        mDiscoveryTask.setNetwork(network_ip, network_start, network_end);
        mDiscoveryTask.setOnPublishProgress(new AbstractHostAsync.OnPublishProgress() {
            @Override
            public void onPublishProgress(HostBean hostBean) {
                int index = Integer.parseInt(hostBean.ipAddress.substring(hostBean.ipAddress.lastIndexOf('.') + 1));
                if(index == 1) {
                    return;
                }
                addHost(hostBean);
            }
        });
        mDiscoveryTask.setOnFinishExecution(new AbstractHostAsync.OnFinishExecution() {
            @Override
            public void onFinishExecution() {
                GlobalDataHolder.getInstance().setListHostBean(hosts);
            }
        });
        mDiscoveryTask.execute();
        btnDiscover.setText(R.string.btn_discover_cancel);
        setButton(btnDiscover, R.drawable.cancel, false);

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cancelTasks();
            }
        });

        makeToast(R.string.discover_start);
        setProgressBarVisibility(true);
        setProgressBarIndeterminateVisibility(true);
    }
    

    public void stopDiscovering() {
        Log.e(TAG, "stopDiscovering()");
        mLayoutLoading.setVisibility(View.GONE);
        mTvListEmpty.setVisibility(View.VISIBLE);
        btnDiscover.setText(R.string.btn_discover);
        setButtons(false);
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startDiscovering();
            }
        });
        mDiscoveryTask = null;
        if(mRequestInfoAsyncList != null) {
            for(int i = 0; i<mRequestInfoAsyncList.size(); i++) {
                mRequestInfoAsyncList.get(i).closeSocket();
            }
        }
    }


    protected void addHost(HostBean hostBean) {
        hostBean.position = hosts.size();
        hosts.add(hostBean);
        //adapter.add(null); //commented out by tamal
        if(mChatPingMessage == null) {
            generatePingMessage();
            try {
                mPingMessageText = mChatPingMessage.getJsonString();
            } catch (JSONException e) {
                e.printStackTrace();
                mPingMessageText = null;
            }
        }
        if(mPingMessageText == null) {
            makeToast(R.string.preferences_error4);
            return;
        }
        Log.e("TAG_LOG", "Server IP(Before Tuning): " + hostBean.ipAddress);
        int subStringIndex = hostBean.ipAddress.indexOf('/');
        String hostIp;
        if(subStringIndex>0) {
            hostIp = hostBean.ipAddress.substring(0, subStringIndex);
        } else {
            hostIp = hostBean.ipAddress;
        }
        Log.e("TAG_LOG", "Server IP: " + hostIp);

        if(!hostIp.equals(ipAddress)) {
            requestNumber++;
            SendRequestMessageAsync sendRequestMessageAsync = new SendRequestMessageAsync();
            sendRequestMessageAsync.setDiscoveryActivity(this);
            HostInfo hostInfo = new HostInfo();
            hostInfo.setIpAddress(hostIp);
            sendRequestMessageAsync.execute(hostInfo, mPingMessageText);
            if(mRequestInfoAsyncList == null) {
                mRequestInfoAsyncList = new ArrayList<>();
            }
            mRequestInfoAsyncList.add(sendRequestMessageAsync);
        }
    }

    public void makeToast(int msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    protected void setButton(Button btn, int res, boolean disable) {
        if (disable) {
            setButtonOff(btn, res);
        } else {
            setButtonOn(btn, res);
        }
    }

    protected void setButtonOff(Button b, int drawable) {
        b.setClickable(false);
        b.setEnabled(false);
        b.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
    }

    protected void setButtonOn(Button b, int drawable) {
        b.setClickable(true);
        b.setEnabled(true);
        b.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
    }


    protected void generatePingMessage() {
        mChatPingMessage = null;
        if(ipAddress != null) {
            mChatPingMessage = new ChatMessage();
            mChatPingMessage.setType(ChatMessage.TYPE_REQUEST_INFO);
            mChatPingMessage.setIpAddress(ipAddress);
            mChatPingMessage.setDeviceId(prefs.getString(Constant.KEY_DEVICE_ID, Constant.DEVICE_ID_UNKNOWN));
            mChatPingMessage.setDeviceName(prefs.getString(Constant.KEY_MY_DEVICE_NAME, Constant.DEVICE_ID_UNKNOWN));
        }
    }


    protected void initNetworkInfo() {
        if (mDiscoveryTask != null) {
            setButton(btnDiscover, R.drawable.cancel, false);
            btnDiscover.setText(R.string.btn_discover_cancel);
            btnDiscover.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    cancelTasks();
                }
            });
        }

        if (currentNetwork != net.hashCode()) {
            Log.i(TAG, "Network info has changed");
            currentNetwork = net.hashCode();

            // Cancel running tasks
            cancelTasks();
        } else {
            return;
        }

        // Get ip information
        network_ip = NetInfo.getUnsignedLongFromIp(net.ip);
        if (prefs.getBoolean(Prefs.KEY_IP_CUSTOM, Prefs.DEFAULT_IP_CUSTOM)) {
            // Custom IP
            network_start = NetInfo.getUnsignedLongFromIp(prefs.getString(Prefs.KEY_IP_START,
                    Prefs.DEFAULT_IP_START));
            network_end = NetInfo.getUnsignedLongFromIp(prefs.getString(Prefs.KEY_IP_END,
                    Prefs.DEFAULT_IP_END));
        } else {
            // Custom CIDR
            if (prefs.getBoolean(Prefs.KEY_CIDR_CUSTOM, Prefs.DEFAULT_CIDR_CUSTOM)) {
                net.cidr = Integer.parseInt(prefs.getString(Prefs.KEY_CIDR, Prefs.DEFAULT_CIDR));
            }
            // Detected IP
            int shift = (32 - net.cidr);
            if (net.cidr < 31) {
                network_start = (network_ip >> shift << shift) + 1;
                network_end = (network_start | ((1 << shift) - 1)) - 1;
            } else {
                network_start = (network_ip >> shift << shift);
                network_end = (network_start | ((1 << shift) - 1));
            }
            // Reset ip start-end (is it really convenient ?)
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(Prefs.KEY_IP_START, NetInfo.getIpFromLongUnsigned(network_start));
            edit.putString(Prefs.KEY_IP_END, NetInfo.getIpFromLongUnsigned(network_end));
            edit.commit();
        }
    }

    protected void setButtons(boolean disable) {
        if (disable) {
            setButtonOff(btnDiscover, R.drawable.disabled);
        } else {
            setButtonOn(btnDiscover, R.drawable.discover);
        }
    }

    protected void cancelTasks() {
        if (mDiscoveryTask != null) {
            mDiscoveryTask.cancel(true);
            mDiscoveryTask = null;
        }
        stopDiscovering();
    }

    @Override
    public void onResume() {
        super.onResume();
        setButtons(true);
        findNetworkInfo();
        registerReceiver(receiver, new IntentFilter(Constant.SERVICE_NOTIFICATION_STRING_CHAT_FOREGROUND));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        cancelTasks();
    }

    protected BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String message = bundle.getString(Constant.KEY_CLIENT_MESSAGE);
                try {
                    ChatMessage chatMessage = new ChatMessage(message);
                    switch (chatMessage.getType()) {
                        case ChatMessage.TYPE_MESSAGE:
                            break;
                        case ChatMessage.TYPE_ADD_CLIENT:
                            break;
                        case ChatMessage.TYPE_REQUEST_INFO:
                            break;
                        case ChatMessage.TYPE_RECEIVE_INFO:
                            Log.e("TAG_LOG","Ping received");
                            addToHostList(chatMessage);
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

}