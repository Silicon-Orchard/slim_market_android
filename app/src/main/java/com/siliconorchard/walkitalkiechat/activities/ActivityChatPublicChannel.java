package com.siliconorchard.walkitalkiechat.activities;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.asynctasks.SendVoiceChatAsync;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.model.VoiceMessage;
import com.siliconorchard.walkitalkiechat.runnable.RunnableReceiveVoiceChat;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by adminsiriconorchard on 4/29/16.
 */
public class ActivityChatPublicChannel extends ChatActivityAbstract{

    private String channelName;

    private RunnableReceiveVoiceChat mRunnableReceiveVoiceChat;
    private Thread mThreadVoiceChat;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initSubView(Bundle bundle) {
        if(channelNumber == Constant.PUBLIC_CHANNEL_NUMBER_A) {
            channelName = getString(R.string.public_channel_a);
        } else {
            channelName = getString(R.string.public_channel_b);
        }
        mTvTitle.setText(channelName);
        mEtChat = (EditText) findViewById(R.id.et_chat);
        mBtnSend = (Button) findViewById(R.id.btn_send);
        //mTvClientMsg.setText(" ");
        //mTvRecipientList.setText("You joined\n");
        HostInfo channelHost = new HostInfo();
        channelHost.setDeviceName(Utils.getDeviceName(mSharedPref));
        channelHost.setIpAddress(myIpAddress);
        channelHost.setDeviceId(Utils.getDeviceId(this, mSharedPref));
        addToReceiverList(channelHost, ClientType.TYPE_JOINER);
        joinChannel();
    }

    @Override
    protected boolean isPrivateChannel() {
        return false;
    }

    @Override
    protected void processJoinChannelMessage(ChatMessage chatMessage) {
        HostInfo hostInfo = getHostInfoFromChatMessage(chatMessage);
        addToReceiverList(hostInfo, ClientType.TYPE_JOINER);
        sendChannelFoundMessage();
    }

    @Override
    protected void updateRecipientList(ChatMessage chatMessage) {
        HostInfo hostInfo = getHostInfoFromChatMessage(chatMessage);
        addToReceiverList(hostInfo, ClientType.TYPE_JOINER);
    }

    private void joinChannel() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(ChatMessage.TYPE_JOIN_CHANNEL);
        chatMessage.setIpAddress(myIpAddress);
        chatMessage.setDeviceName(Utils.getDeviceName(mSharedPref));
        chatMessage.setDeviceId(Utils.getDeviceId(this, mSharedPref));
        chatMessage.setChannelNumber(channelNumber);

        try {
            Utils.sendBroadCastMessageToRegisteredClients(chatMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean hasRecipient() {
        if(mListHostInfo == null || mListHostInfo.size() < 1) {
            return false;
        } else {
            return true;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        startVoiceChatThread();
        mBtnVoice.setText("Stream");
        mBtnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recording) {
                    stopStreaming();
                    mBtnVoice.setText("Stream");
                } else {
                    Thread recordThread = new Thread(new Runnable(){
                        @Override
                        public void run() {
                            recording = true;
                            startStreaming();
                        }
                    });
                    recordThread.start();
                    mBtnVoice.setText("Stop");
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopVoiceChatThread();
        stopStreaming();
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

    private static final int SAMPLE_RATE_IN_HZ = 11025;
    private boolean recording;

    private void stopStreaming() {
        recording = false;
    }

    private void startStreaming(){
        try {
            int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            short[] audioData = new short[minBufferSize];

            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    SAMPLE_RATE_IN_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize);

            audioRecord.startRecording();
            VoiceMessage voiceMessage = new VoiceMessage();
            voiceMessage.setDeviceName(Utils.getDeviceName(mSharedPref));
            voiceMessage.setChannelNumber(channelNumber);

            while(recording){
                int numberOfShort = audioRecord.read(audioData, 0, minBufferSize);
                if(numberOfShort>0) {
                    byte[] audioBytes = Utils.shortArrayToByteArray(audioData);
                    //voiceMessage.setVoiceMessage(Base64.encodeToString(audioBytes, Base64.NO_WRAP));
                    SendVoiceChatAsync sendVoiceChatAsync = new SendVoiceChatAsync();
                    sendVoiceChatAsync.setVoiceBytes(audioBytes);
                    sendVoiceChatAsync.setMyIpAddress(myIpAddress);
                    sendVoiceChatAsync.setClientIPAddressList(mListHostInfo);
                    sendVoiceChatAsync.execute();
                }
            }

            audioRecord.stop();
            //dataOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
