package com.siliconorchard.walkitalkiechat.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.adapter.AdapterChatHistory;
import com.siliconorchard.walkitalkiechat.adapter.AdapterRecipientList;
import com.siliconorchard.walkitalkiechat.asynctasks.SendMessageAsync;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.ChatMessageHistory;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.model.VoiceMessage;
import com.siliconorchard.walkitalkiechat.runnable.RunnableReceiveFile;
import com.siliconorchard.walkitalkiechat.runnable.RunnableReceiveVoiceChat;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by adminsiriconorchard on 5/3/16.
 */
public abstract class ChatActivityAbstract extends ActivityBase {

    //protected TextView mTvClientMsg;

    protected TextView mTvTitle;
    protected LinearLayout mLayoutBack;

    protected EditText mEtChat;
    protected ImageView mBtnSend;

    protected List<HostInfo> mListHostInfo;

    protected SharedPreferences mSharedPref;
    protected String myIpAddress;

    //protected TextView mTvRecipientList;
    protected ListView mLvRecipientList;
    protected int channelNumber;

    protected LinearLayout mLayoutProgress;
    protected TextView mTvPercent;
    protected ProgressBar mProgress;
    protected ImageView mBtnVoice;

    protected static final int MAX_PROGRESS_BAR = 100;

    private RunnableReceiveFile mRunnableReceiveFile;
    private Thread mThread;

    protected AdapterRecipientList adapterRecipientList;

    protected ListView mLvChatHistory;
    protected AdapterChatHistory adapterChatHistory;


    private RunnableReceiveVoiceChat mRunnableReceiveVoiceChat;
    private Thread mThreadVoiceChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutID());
        initView();
        initListeners();
    }

    private void initView() {
        mSharedPref = getSharedPreferences(Constant.SHARED_PREF_NAME, MODE_PRIVATE);
        Bundle bundle = getIntent().getExtras();
        myIpAddress = bundle.getString(Constant.KEY_MY_IP_ADDRESS, null);
        channelNumber = bundle.getInt(Constant.KEY_CHANNEL_NUMBER, 0);

        //mTvClientMsg = (TextView) findViewById(R.id.tv_chat_history);
        mLvChatHistory = (ListView) findViewById(R.id.lv_chat_history);
        mTvTitle = (TextView) findViewById(R.id.tv_title_name);
        mLayoutBack = (LinearLayout) findViewById(R.id.layout_back);
        mTvTitle.setText("Channel: "+channelNumber);

        mEtChat = (EditText) findViewById(R.id.et_chat);
        mBtnSend = (ImageView) findViewById(R.id.btn_send);
        //mTvClientMsg.setText(" ");

        /*mTvRecipientList = (TextView) findViewById(R.id.tv_recipient_list);
        mTvRecipientList.setText("");*/

        mLayoutProgress = (LinearLayout) findViewById(R.id.ll_progress_bar);
        mTvPercent = (TextView) findViewById(R.id.tv_percent);
        mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        mProgress.setMax(MAX_PROGRESS_BAR);
        mLayoutProgress.setVisibility(View.GONE);
        mBtnVoice = (ImageView) findViewById(R.id.btn_voice);

        mLvRecipientList = (ListView) findViewById(R.id.lv_recipient_list);

        initChatHistoryList();

        initSubView(bundle);
    }

    private void initChatHistoryList() {
        adapterChatHistory = new AdapterChatHistory(this, null);
        mLvChatHistory.setAdapter(adapterChatHistory);
    }

    private void initListeners() {
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasRecipient()) {
                    Toast.makeText(ChatActivityAbstract.this, "There are no recipient to this chat room", Toast.LENGTH_LONG).show();
                    return;
                }
                if (mBtnVoice.isEnabled()) {
                    String msg = mEtChat.getText().toString().trim();
                    if (msg == null || msg.length() <= 0) {
                        return;
                    }
                    //mTvClientMsg.append("\nMe: " + msg);
                    addChatMessage("Me", msg, true, null);
                    mEtChat.setText("");
                    try {
                        sendBroadCastMessage(generateChatMessage(msg).getJsonString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    mBtnVoice.setEnabled(true);
                }

            }
        });

        //mTvClientMsg.setMovementMethod(new ScrollingMovementMethod());

        mLayoutBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatActivityAbstract.this.finish();
            }
        });

        mBtnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivityAbstract.this, RecordVoiceActivityForResult.class);
                Bundle bundle = new Bundle();
                ArrayList<Parcelable> hostList = new ArrayList<>();
                for (ListIterator<HostInfo> listIterator = mListHostInfo.listIterator(); listIterator.hasNext(); ) {
                    hostList.add(listIterator.next());
                }
                bundle.putParcelableArrayList(Constant.KEY_HOST_INFO_LIST, hostList);
                bundle.putInt(Constant.KEY_CHANNEL_NUMBER, channelNumber);
                bundle.putString(Constant.KEY_MY_IP_ADDRESS, myIpAddress);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void addChatMessage(String name, String msg, boolean isSent, String filePath) {
        ChatMessageHistory chatMessage = new ChatMessageHistory();
        chatMessage.setDeviceName(name);
        chatMessage.setMessage(msg);
        chatMessage.setIsSent(isSent);
        if(filePath != null) {
            chatMessage.setFilePath(filePath);
        }
        adapterChatHistory.addMessage(chatMessage);
        mLvChatHistory.post(new Runnable() {
            public void run() {
                mLvChatHistory.setSelection(mLvChatHistory.getCount() - 1);
            }
        });
    }

    protected ChatMessage generateChatMessage(String message) {
        ChatMessage chatMessage = generateChatMessage();
        chatMessage.setType(ChatMessage.TYPE_MESSAGE);
        chatMessage.setMessage(message);
        return chatMessage;
    }

    protected ChatMessage generateChatMessage(List<HostInfo> clientList) {
        ChatMessage chatMessage = generateChatMessage();
        chatMessage.setType(ChatMessage.TYPE_CHANNEL_FOUND);
        chatMessage.setClientList(clientList);
        return chatMessage;
    }

    protected ChatMessage generateChatMessage() {
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
        runThread();
        startVoiceChatThread();
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        stopThread();
        stopVoiceChatThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendChannelLeftMessage();
        stopThread();
        stopVoiceChatThread();
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
                            //mTvClientMsg.append("\n" + chatMessage.getDeviceName() + ": " + chatMessage.getMessage());
                            addChatMessage(chatMessage.getDeviceName(), chatMessage.getMessage(), false, null);
                            addToReceiverList(getHostInfoFromChatMessage(chatMessage), ClientType.TYPE_JOINER);
                            break;
                        case ChatMessage.TYPE_JOIN_CHANNEL:
                            processJoinChannelMessage(chatMessage);
                            break;
                        case ChatMessage.TYPE_LEFT_CHANNEL:
                            removeFromReceiverList(getHostInfoFromChatMessage(chatMessage));
                            break;

                        case ChatMessage.TYPE_CHANNEL_FOUND:
                            updateRecipientList(chatMessage);
                            break;

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    protected HostInfo getHostInfoFromChatMessage(ChatMessage chatMessage) {
        HostInfo hostInfo = new HostInfo();
        hostInfo.setIpAddress(chatMessage.getIpAddress());
        hostInfo.setDeviceId(chatMessage.getDeviceId());
        hostInfo.setDeviceName(chatMessage.getDeviceName());
        return hostInfo;
    }

    protected boolean addToReceiverList(HostInfo hostInfo, ClientType type) {
        if(mListHostInfo == null) {
            mListHostInfo = new ArrayList<>();
        }
        for(int i = 0; i< mListHostInfo.size(); i++) {
            HostInfo hostInfoInList = mListHostInfo.get(i);
            if(hostInfoInList.getIpAddress().equals(hostInfo.getIpAddress())) {
                hostInfoInList.setIsChecked(true);
                updateRecipientInfo(hostInfo, type);
                return false;
            }
        }
        if(hostInfo.getIpAddress() != null && hostInfo.getIpAddress().length()>1) {
            hostInfo.setIsChecked(true);
            mListHostInfo.add(hostInfo);
            updateRecipientInfo(hostInfo, type);
            return true;
        }
        return false;
    }



    protected boolean removeFromReceiverList(HostInfo hostInfo) {
        if(mListHostInfo == null) {
            return false;
        }
        for(int i = 0; i< mListHostInfo.size(); i++) {
            HostInfo hostInfoInList = mListHostInfo.get(i);
            if(hostInfoInList.getIpAddress().equals(hostInfo.getIpAddress())) {
                mListHostInfo.get(i).setIsChecked(false);
                updateRecipientInfo(hostInfo, ClientType.TYPE_QUIT);
                return true;
            }
        }
        return false;
    }

    protected void updateRecipientInfo(HostInfo hostInfo, ClientType type) {
        adapterRecipientList = new AdapterRecipientList(this, mListHostInfo);
        mLvRecipientList.setAdapter(adapterRecipientList);
        /*String name = hostInfo.getDeviceName();
        switch (type) {
            case TYPE_CREATOR:
                mTvRecipientList.append(name+"\tCreated Channel\n");
                break;
            case TYPE_JOINER:
                mTvRecipientList.append(name+"\tJoined\n");
                break;
            case TYPE_QUIT:
                mTvRecipientList.append(name+"\tQuit\n");
                break;
        }*/
    }



    protected void sendChannelFoundMessage() {
        List<HostInfo> clientList = new ArrayList<>();
        if(isPrivateChannel() && mListHostInfo != null) {
            clientList.addAll(mListHostInfo);
        }

        ChatMessage chatMessage = generateChatMessage(clientList);
        try {
            String message = chatMessage.getJsonString();
            sendBroadCastMessage(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void sendChannelLeftMessage() {
        ChatMessage chatMessage = generateChatMessage();
        chatMessage.setType(ChatMessage.TYPE_LEFT_CHANNEL);
        try {
            sendBroadCastMessage(chatMessage.getJsonString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void sendBroadCastMessage(String message) {
        if (mListHostInfo != null && mListHostInfo.size() > 0) {
            for (int i = 0; i < mListHostInfo.size(); i++) {
                HostInfo receiver = mListHostInfo.get(i);
                if(!receiver.getIpAddress().equals(myIpAddress)) {
                    SendMessageAsync sendMessageAsync = new SendMessageAsync();
                    sendMessageAsync.execute(receiver, message);
                }
            }
        }
    }

    protected enum ClientType {
        TYPE_CREATOR,
        TYPE_JOINER,
        TYPE_QUIT
    }

    protected void runThread() {
        mRunnableReceiveFile = new RunnableReceiveFile();
        mRunnableReceiveFile.setOnReceiveCallBacks(new RunnableReceiveFile.OnReceiveCallBacks() {
            @Override
            public void onPreReceive(final VoiceMessage voiceMessage) {
                mTvPercent.post(new Runnable() {
                    @Override
                    public void run() {
                        //mTvClientMsg.append("\n" + voiceMessage.getDeviceName() + ": Sending voice mail...");
                        //addChatMessage(voiceMessage.getDeviceName(), "Sending voice mail..");
                    }
                });
            }

            @Override
            public void onProgressUpdate(final VoiceMessage voiceMessage) {
                mTvPercent.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgress.setVisibility(View.VISIBLE);
                        mTvPercent.setVisibility(View.VISIBLE);
                        int progress = voiceMessage.getCurrentChunkNo() * 100 / voiceMessage.getTotalChunkCount();
                        if (progress > 100) {
                            progress = 100;
                        }
                        mTvPercent.setText("" + progress + "%");
                        mProgress.setProgress(progress);
                    }
                });
            }

            @Override
            public void onPostReceive(final VoiceMessage voiceMessage, final File file) {
                if (voiceMessage.getCurrentChunkNo() >= voiceMessage.getTotalChunkCount()) {
                    mTvPercent.post(new Runnable() {
                        @Override
                        public void run() {
                            //mTvClientMsg.append("\nYou received a voice mail from " + voiceMessage.getDeviceName());
                            addChatMessage(voiceMessage.getDeviceName(), "Voice mail received.", false, file.getAbsolutePath());
                            //mLayoutPlay.setVisibility(View.VISIBLE);
                            //mLayoutProgress.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onErrorOccur(final String errorText) {
                mTvPercent.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), errorText, Toast.LENGTH_LONG).show();
                        //mTvClientMsg.append("\n" + errorText);
                    }
                });
            }
        });
        mThread = new Thread(mRunnableReceiveFile);
        mRunnableReceiveFile.setChannelNumber(channelNumber);
        mThread.start();
    }

    protected void stopThread() {
        if(mRunnableReceiveFile != null) {
            mRunnableReceiveFile.closeSocket();
            mRunnableReceiveFile.terminate();
            mThread = null;
            mRunnableReceiveFile = null;
        }
    }

    private void startVoiceChatThread() {
        mRunnableReceiveVoiceChat = new RunnableReceiveVoiceChat();
        mThreadVoiceChat = new Thread(mRunnableReceiveVoiceChat);
        mRunnableReceiveVoiceChat.setChannelNumber(channelNumber);
        mThreadVoiceChat.start();
    }

    private void stopVoiceChatThread() {
        if(mRunnableReceiveVoiceChat != null) {
            mRunnableReceiveVoiceChat.terminate();
            mRunnableReceiveVoiceChat.closeSocket();
            mThreadVoiceChat = null;
            mRunnableReceiveVoiceChat = null;
        }
    }

    protected abstract int getLayoutID();
    protected abstract void initSubView(Bundle bundle);
    protected abstract boolean isPrivateChannel();
    protected abstract void processJoinChannelMessage(ChatMessage chatMessage);
    protected abstract void updateRecipientList(ChatMessage chatMessage);
    protected abstract boolean hasRecipient();
}
