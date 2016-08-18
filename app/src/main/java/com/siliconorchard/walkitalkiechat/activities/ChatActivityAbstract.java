package com.siliconorchard.walkitalkiechat.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.model.FileMessage;
import com.siliconorchard.walkitalkiechat.runnable.RunnableReceiveFileTCP;
import com.siliconorchard.walkitalkiechat.runnable.RunnableReceiveVoiceChat;
import com.siliconorchard.walkitalkiechat.utilities.Constant;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by adminsiriconorchard on 5/3/16.
 */
public abstract class ChatActivityAbstract extends ChatActivityBase {

    private RunnableReceiveVoiceChat mRunnableReceiveVoiceChat;
    private Thread mThreadVoiceChat;
    private RunnableReceiveFileTCP mRunnableReceiveFileWhole;
    private Thread mThread;
    protected boolean doNotInitThread;

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

        mLvChatHistory = (ListView) findViewById(R.id.lv_chat_history);
        mTvTitle = (TextView) findViewById(R.id.tv_title_name);
        mLayoutBack = (LinearLayout) findViewById(R.id.layout_back);
        mTvTitle.setText("Channel: " + channelNumber);

        mEtChat = (EditText) findViewById(R.id.et_chat);
        mBtnSend = (ImageView) findViewById(R.id.btn_send);

        mLayoutProgress = (LinearLayout) findViewById(R.id.ll_progress_bar);
        mTvPercent = (TextView) findViewById(R.id.tv_percent);
        mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        mProgress.setMax(MAX_PROGRESS_BAR);
        mLayoutProgress.setVisibility(View.GONE);
        mBtnVoice = (ImageView) findViewById(R.id.btn_voice);

        mLvRecipientList = (ListView) findViewById(R.id.lv_recipient_list);

        mIvAttachFile = (ImageView) findViewById(R.id.iv_attach_file);
        mIvStreamVoice = (ImageView) findViewById(R.id.iv_stream_voice);
        mIvStreamVideo = (ImageView) findViewById(R.id.iv_stream_video);
        mLayoutShareLocation = (LinearLayout) findViewById(R.id.ll_share_location);

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

        mIvAttachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAttachPopup();
            }
        });
        mIvStreamVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeVoiceCall();
            }
        });
        mIvStreamVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toastUnderConstructionMessage();
            }
        });
        mLayoutShareLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toastUnderConstructionMessage();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!doNotInitThread) {
            registerReceiver(receiver, new IntentFilter(Constant.SERVICE_NOTIFICATION_STRING_CHAT_FOREGROUND));
            runThread();
            startVoiceChatThread();
            doNotInitThread = true;
        }
    }

    /*@Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        stopThread();
        stopVoiceChatThread();
        stopStreaming();
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(doNotInitThread) {
            doOnDestroy();
        }
    }

    public void doOnDestroy() {
        sendChannelLeftMessage();
        unregisterReceiver(receiver);
        stopThread();
        stopVoiceChatThread();
        stopStreaming();
        doNotInitThread = false;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String message = bundle.getString(Constant.KEY_CLIENT_MESSAGE);
                try {
                    ChatMessage chatMessage = new ChatMessage(message);
                    if (channelNumber != chatMessage.getChannelNumber()) {
                        return;
                    }
                    switch (chatMessage.getType()) {
                        case ChatMessage.TYPE_MESSAGE:
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

    protected void runThread() {
        mRunnableReceiveFileWhole = new RunnableReceiveFileTCP();
        mRunnableReceiveFileWhole.setOnReceiveCallBacks(new RunnableReceiveFileTCP.OnReceiveCallBacks() {
            @Override
            public void onPreReceive(final FileMessage fileMessage) {
                mTvPercent.post(new Runnable() {
                    @Override
                    public void run() {
                        //mTvClientMsg.append("\n" + voiceMessage.getDeviceName() + ": Sending voice mail...");
                        //addChatMessage(voiceMessage.getDeviceName(), "Sending voice mail..");
                    }
                });
            }

            @Override
            public void onProgressUpdate(final FileMessage fileMessage) {
                mTvPercent.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgress.setVisibility(View.VISIBLE);
                        mTvPercent.setVisibility(View.VISIBLE);
                        int progress = fileMessage.getCurrentChunkNo() * 100 / fileMessage.getTotalChunkCount();
                        if (progress > 100) {
                            progress = 100;
                        }
                        mTvPercent.setText("" + progress + "%");
                        mProgress.setProgress(progress);
                    }
                });
            }

            @Override
            public void onPostReceive(final FileMessage fileMessage, final File file) {
                if (fileMessage.getCurrentChunkNo() >= fileMessage.getTotalChunkCount()) {
                    mTvPercent.post(new Runnable() {
                        @Override
                        public void run() {
                            //mTvClientMsg.append("\nYou received a voice mail from " + voiceMessage.getDeviceName());
                            String message = null;
                            if(fileMessage.getFileType() == Constant.FILE_TYPE_AUDIO) {
                                message = "Voice mail received.";
                            } else {
                                message = fileMessage.getFileName();
                            }
                            addFileMessage(fileMessage, message, false, file.getAbsolutePath());
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
        mThread = new Thread(mRunnableReceiveFileWhole);
        mRunnableReceiveFileWhole.setChannelNumber(channelNumber);
        mThread.start();
    }

    protected void stopThread() {
        if (mRunnableReceiveFileWhole != null) {
            mRunnableReceiveFileWhole.closeSocket();
            mRunnableReceiveFileWhole.terminate();
            mThread = null;
            mRunnableReceiveFileWhole = null;
        }
    }

    private void startVoiceChatThread() {
        mRunnableReceiveVoiceChat = new RunnableReceiveVoiceChat();
        mThreadVoiceChat = new Thread(mRunnableReceiveVoiceChat);
        mRunnableReceiveVoiceChat.setChannelNumber(channelNumber);
        mThreadVoiceChat.start();
    }

    private void stopVoiceChatThread() {
        if (mRunnableReceiveVoiceChat != null) {
            mRunnableReceiveVoiceChat.terminate();
            mRunnableReceiveVoiceChat.closeSocket();
            mThreadVoiceChat = null;
            mRunnableReceiveVoiceChat = null;
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constant.REQUEST_CODE_SELECT_SINGLE_PICTURE && resultCode == Activity.RESULT_OK) {
            initUriAndFile(data, true);
            FileMessage fileMessage = sendFileMessage(mSelectedFile, mSelectedFile.getName(), Constant.FILE_TYPE_PHOTO);
            addFileMessage(fileMessage, fileMessage.getFileName(), true, mSelectedFile.getAbsolutePath());
        } else if(requestCode == Constant.REQUEST_CODE_SELECT_ANY_FILE && resultCode == Activity.RESULT_OK) {
            initUriAndFile(data, false);
            String fileName = mSelectedFile.getName();
            int fileType = extractFileType(fileName);
            FileMessage fileMessage = sendFileMessage(mSelectedFile, fileName, fileType);
            addFileMessage(fileMessage, fileMessage.getFileName(), true, mSelectedFile.getAbsolutePath());
        }
    }

    protected abstract int getLayoutID();
    protected abstract void initSubView(Bundle bundle);
    protected abstract void processJoinChannelMessage(ChatMessage chatMessage);
    protected abstract void updateRecipientList(ChatMessage chatMessage);
    protected abstract boolean hasRecipient();
}
