package com.siliconorchard.walkitalkiechat.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.util.List;


/**
 * Created by adminsiriconorchard on 4/12/16.
 */
public class ChatActivity extends ActivityBase {

    private TextView mTvClientMsg;

    private TextView mTvTitle;
    private LinearLayout mLayoutBack;

    private EditText mEtChat;
    private Button mBtnSend;

    private List<HostInfo> mListHostInfo;

    private SharedPreferences mSharedPref;
    private String myIpAddress;

    private TextView mTvRecipientList;
    private int channelNumber;

    private String channelCreatorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initView();
        initListeners();
    }

    private void initView() {
        mSharedPref = getSharedPreferences(Constant.SHARED_PREF_NAME, MODE_PRIVATE);
        Bundle bundle = getIntent().getExtras();
        myIpAddress = bundle.getString(Constant.KEY_MY_IP_ADDRESS, null);
        channelNumber = bundle.getInt(Constant.KEY_CHANNEL_NUMBER, 0);

        mTvClientMsg = (TextView) findViewById(R.id.tv_chat_history);
        mTvTitle = (TextView) findViewById(R.id.tv_title_name);
        mLayoutBack = (LinearLayout) findViewById(R.id.layout_back);
        mTvTitle.setText("Channel: "+channelNumber);

        mEtChat = (EditText) findViewById(R.id.et_chat);
        mBtnSend = (Button) findViewById(R.id.btn_send);
        mTvClientMsg.setText(" ");

        //mTvRecipientList = (TextView) findViewById(R.id.tv_recipient_list);
        HostInfo hostInfo = (HostInfo)bundle.getParcelable(Constant.KEY_HOST_INFO);
        if(hostInfo != null) {
            addToReceiverList(hostInfo);
        }
        ArrayList<Parcelable> parcelableList = bundle.getParcelableArrayList(Constant.KEY_HOST_INFO_LIST);
        if(parcelableList != null && parcelableList.size()>0) {
            int size = parcelableList.size();
            HostInfo channelHost = (HostInfo) parcelableList.get(0);
            channelCreatorName = channelHost.getDeviceName();
            for(int i = 0; i<size; i++) {
                HostInfo client = (HostInfo) parcelableList.get(i);
                addToReceiverList(client);
            }
        } else {
            channelCreatorName = Utils.getDeviceName(mSharedPref);
        }
    }

    private void initListeners() {
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListHostInfo == null || mListHostInfo.size() < 1) {
                    Toast.makeText(ChatActivity.this, "No user added to this chat list", Toast.LENGTH_LONG).show();
                    return;
                }
                String msg = mEtChat.getText().toString().trim();
                if (msg == null || msg.length() <= 0) {
                    return;
                }

                //Pass the server ip, port and client message to the AsyncTask
                mTvClientMsg.append("\nMe: " + msg);
                mEtChat.setText("");
                try {
                    sendBroadCastMessage(generateChatMessage(msg).getJsonString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        mTvClientMsg.setMovementMethod(new ScrollingMovementMethod());

        mLayoutBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatActivity.this.finish();
            }
        });
    }

    private ChatMessage generateChatMessage(String message) {
        ChatMessage chatMessage = generateChatMessage();
        chatMessage.setType(ChatMessage.TYPE_MESSAGE);
        chatMessage.setMessage(message);
        return chatMessage;
    }

    private ChatMessage generateChatMessage(HostInfo hostInfo) {
        ChatMessage chatMessage = generateChatMessage();
        chatMessage.setType(ChatMessage.TYPE_ADD_CLIENT);
        chatMessage.setClientInfo(hostInfo);
        return chatMessage;
    }

    private ChatMessage generateChatMessage(List<HostInfo> clientList) {
        ChatMessage chatMessage = generateChatMessage();
        chatMessage.setType(ChatMessage.TYPE_CHANNEL_FOUND);
        chatMessage.setClientList(clientList);
        return chatMessage;
    }

    private ChatMessage generateChatMessage() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setIpAddress(myIpAddress);
        chatMessage.setDeviceId(Utils.getDeviceId(this, mSharedPref));
        chatMessage.setDeviceName(Utils.getDeviceName(mSharedPref));
        chatMessage.setChannelNumber(channelNumber);
        return chatMessage;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(Constant.SERVICE_NOTIFICATION_STRING_CHAT_FOREGROUND));
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendChannelLeftMessage();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String message = bundle.getString(Constant.KEY_CLIENT_MESSAGE);
                try {
                    ChatMessage chatMessage = new ChatMessage(message);
                    if(channelNumber != chatMessage.getChannelNumber()) {
                        return;
                    }
                    switch (chatMessage.getType()) {
                        case ChatMessage.TYPE_MESSAGE:
                            mTvClientMsg.append("\n" + chatMessage.getDeviceName() + ": " + chatMessage.getMessage());
                            addToReceiverList(getHostInfoFromChatMessage(chatMessage));
                            break;
                        case ChatMessage.TYPE_ADD_CLIENT:
                            addToReceiverList(getHostInfoFromChatMessage(chatMessage));
                            addToReceiverList(chatMessage.getClientInfo());
                            mTvClientMsg.append("\n" + chatMessage.getClientInfo().getDeviceName() + ": Joined channel");
                            break;
                        case ChatMessage.TYPE_JOIN_CHANNEL:
                            processJoinChannelMessage(chatMessage);
                            break;
                        case ChatMessage.TYPE_LEFT_CHANNEL:
                            mTvClientMsg.append("\n" + chatMessage.getDeviceName() + ": Left channel");
                            removeFromReceiverList(getHostInfoFromChatMessage(chatMessage));
                            break;

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    private HostInfo getHostInfoFromChatMessage(ChatMessage chatMessage) {
        HostInfo hostInfo = new HostInfo();
        hostInfo.setIpAddress(chatMessage.getIpAddress());
        hostInfo.setDeviceId(chatMessage.getDeviceId());
        hostInfo.setDeviceName(chatMessage.getDeviceName());
        return hostInfo;
    }

    private void sendAddClientMessage(HostInfo hostInfo) {
        boolean isAdded = addToReceiverList(hostInfo);
        if(!isAdded) {
            return;
        }
        try {
            sendBroadCastMessage(generateChatMessage(hostInfo).getJsonString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean addToReceiverList(HostInfo hostInfo) {
        if(mListHostInfo == null) {
            mListHostInfo = new ArrayList<>();
        }
        for(int i = 0; i< mListHostInfo.size(); i++) {
            HostInfo hostInfoInList = mListHostInfo.get(i);
            if(hostInfoInList.getIpAddress().equals(hostInfo.getIpAddress())) {
                if(hostInfoInList.getDeviceName() == null || hostInfoInList.getDeviceName().length() <=0) {
                    mListHostInfo.set(i,hostInfo);
                    updateRecipientInfo();
                }
                return false;
            }
        }
        if(hostInfo.getIpAddress() != null && hostInfo.getIpAddress().length()>1) {
            mListHostInfo.add(hostInfo);
            updateRecipientInfo();
            return true;
        }
        return false;
    }

    private boolean removeFromReceiverList(HostInfo hostInfo) {
        if(mListHostInfo == null) {
            return false;
        }
        for(int i = 0; i< mListHostInfo.size(); i++) {
            HostInfo hostInfoInList = mListHostInfo.get(i);
            if(hostInfoInList.getIpAddress().equals(hostInfo.getIpAddress())) {
                mListHostInfo.remove(i);
                updateRecipientInfo();
                return true;
            }
        }
        return false;
    }

    private void updateRecipientInfo() {
        StringBuilder stringBuilder = new StringBuilder(channelCreatorName);
        stringBuilder.append("  Create Channel\n");
        int limit = mListHostInfo.size();
        for(int i = 0; i<limit; i++) {
            HostInfo hostInfo = mListHostInfo.get(i);
            String deviceName = hostInfo.getDeviceName();
            if(deviceName != null && deviceName.equals(channelCreatorName)) {

            } else {
                if(deviceName != null && deviceName.length()>0) {
                    stringBuilder.append(deviceName);
                } else {
                    stringBuilder.append(hostInfo.getIpAddress());
                }
                stringBuilder.append("  Join");
                if(i < limit-1) {
                    stringBuilder.append('\n');
                }
            }

        }
        mTvRecipientList.setText(stringBuilder.toString());
    }

    private void processJoinChannelMessage(ChatMessage chatMessage) {
        if(!GlobalDataHolder.getInstance().isChannelExistsInMyChannels(chatMessage.getChannelNumber())) {
            return;
        }
        HostInfo hostInfo = new HostInfo();
        hostInfo.setIpAddress(chatMessage.getIpAddress());
        hostInfo.setDeviceId(chatMessage.getDeviceId());
        hostInfo.setDeviceName(chatMessage.getDeviceName());

        sendChannelFoundMessage(hostInfo);
        sendAddClientMessage(hostInfo);

        addToReceiverList(hostInfo);
    }

    private void sendChannelFoundMessage(HostInfo hostInfo) {
        List<HostInfo> clientList = new ArrayList<>();
        HostInfo myHostInfo = new HostInfo();
        myHostInfo.setIpAddress(myIpAddress);
        myHostInfo.setDeviceId(Utils.getDeviceId(this, mSharedPref));
        myHostInfo.setDeviceName(Utils.getDeviceName(mSharedPref));
        clientList.add(myHostInfo);
        if(mListHostInfo != null) {
            clientList.addAll(mListHostInfo);
        }

        ChatMessage chatMessage = generateChatMessage(clientList);
        try {
            String message = chatMessage.getJsonString();
            SendMessageAsync sendMessageAsync = new SendMessageAsync();
            sendMessageAsync.execute(hostInfo, message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendChannelLeftMessage() {
        ChatMessage chatMessage = generateChatMessage();
        chatMessage.setType(ChatMessage.TYPE_LEFT_CHANNEL);
        try {
            sendBroadCastMessage(chatMessage.getJsonString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendBroadCastMessage(String message) {
        if (mListHostInfo != null && mListHostInfo.size() > 0) {
            for (int i = 0; i < mListHostInfo.size(); i++) {
                HostInfo receiver = mListHostInfo.get(i);
                SendMessageAsync sendMessageAsync = new SendMessageAsync();
                sendMessageAsync.execute(receiver, message);
            }
        }
    }

}
