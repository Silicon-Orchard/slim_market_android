package com.siliconorchard.walkitalkiechat.activities;

import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.adapter.AdapterChatHistory;
import com.siliconorchard.walkitalkiechat.adapter.AdapterRecipientList;
import com.siliconorchard.walkitalkiechat.asynctasks.SendMessageAsync;
import com.siliconorchard.walkitalkiechat.asynctasks.SendVoiceChatAsync;
import com.siliconorchard.walkitalkiechat.asynctasks.SendFileDataTCP;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.ChatMessageHistory;
import com.siliconorchard.walkitalkiechat.model.FileMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by adminsiriconorchard on 6/17/16.
 */
public abstract class ChatActivityBase extends ActivitySelectFileAndPhotoBase {

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

    protected AdapterRecipientList adapterRecipientList;

    protected ListView mLvChatHistory;
    protected AdapterChatHistory adapterChatHistory;

    protected ImageView mIvAttachFile;
    protected ImageView mIvStreamVoice;
    protected ImageView mIvStreamVideo;
    protected LinearLayout mLayoutShareLocation;

    protected static final int SAMPLE_RATE_IN_HZ = 11025;
    protected boolean isStreaming;

    protected void addChatMessage(String name, String msg, boolean isSent, String filePath) {
        ChatMessageHistory chatMessage = new ChatMessageHistory();
        chatMessage.setDeviceName(name);
        chatMessage.setMessage(msg);
        chatMessage.setIsSent(isSent);
        if (filePath != null) {
            chatMessage.setFilePath(filePath);
        }
        adapterChatHistory.addMessage(chatMessage);
        mLvChatHistory.post(new Runnable() {
            public void run() {
                mLvChatHistory.setSelection(mLvChatHistory.getCount() - 1);
            }
        });
    }

    protected int addFileMessage(FileMessage fileMessage, String msg, boolean isSent, String filePath) {
        ChatMessageHistory chatMessage = generateChatHistoryMessage(fileMessage, msg, isSent, filePath);
        int addedIndex = adapterChatHistory.addMessage(chatMessage);
        mLvChatHistory.post(new Runnable() {
            public void run() {
                mLvChatHistory.setSelection(mLvChatHistory.getCount() - 1);
            }
        });
        return addedIndex;
    }

    protected ChatMessageHistory generateChatHistoryMessage(FileMessage fileMessage, String msg, boolean isSent, String filePath) {
        ChatMessageHistory chatMessage = new ChatMessageHistory();
        if(isSent) {
            chatMessage.setDeviceName("Me");
        } else {
            chatMessage.setDeviceName(fileMessage.getDeviceName());
        }
        chatMessage.setMessage(msg);
        chatMessage.setIsSent(isSent);
        chatMessage.setFilePath(filePath);
        chatMessage.setFileType(fileMessage.getFileType());
        return chatMessage;
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


    protected void toastUnderConstructionMessage() {
        Toast.makeText(this, R.string.this_function_is_under_construction, Toast.LENGTH_LONG).show();
    }

    protected void showAttachPopup() {
        LayoutInflater layoutInflater
                = (LayoutInflater) getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup_attach_file, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout llUploadFile = (LinearLayout) popupView.findViewById(R.id.ll_upload_file);
        llUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //toastUnderConstructionMessage();
                selectAnyFile();
                popupWindow.dismiss();
            }
        });

        LinearLayout llUploadPhoto = (LinearLayout) popupView.findViewById(R.id.ll_upload_photo);
        llUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //toastUnderConstructionMessage();
                selectPictureOption();
                popupWindow.dismiss();
            }
        });
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(mIvAttachFile, 0 - Utils.dpToPx(100) + (mIvAttachFile.getWidth() / 3), 0 - Utils.dpToPx(200) - (3 * mIvAttachFile.getHeight() / 2));
    }

    protected HostInfo getHostInfoFromChatMessage(ChatMessage chatMessage) {
        HostInfo hostInfo = new HostInfo();
        hostInfo.setIpAddress(chatMessage.getIpAddress());
        hostInfo.setDeviceId(chatMessage.getDeviceId());
        hostInfo.setDeviceName(chatMessage.getDeviceName());
        return hostInfo;
    }

    protected boolean addToReceiverList(HostInfo hostInfo, ClientType type) {
        if (mListHostInfo == null) {
            mListHostInfo = new ArrayList<>();
        }
        for (int i = 0; i < mListHostInfo.size(); i++) {
            HostInfo hostInfoInList = mListHostInfo.get(i);
            if (hostInfoInList.getIpAddress().equals(hostInfo.getIpAddress())) {
                hostInfoInList.setIsOnline(true);
                updateRecipientInfo(hostInfo, type);
                return false;
            }
        }
        if (hostInfo.getIpAddress() != null && hostInfo.getIpAddress().length() > 1) {
            hostInfo.setIsOnline(true);
            mListHostInfo.add(hostInfo);
            updateRecipientInfo(hostInfo, type);
            return true;
        }
        return false;
    }

    protected boolean removeFromReceiverList(HostInfo hostInfo) {
        if (mListHostInfo == null) {
            return false;
        }
        for (int i = 0; i < mListHostInfo.size(); i++) {
            HostInfo hostInfoInList = mListHostInfo.get(i);
            if (hostInfoInList.getIpAddress().equals(hostInfo.getIpAddress())) {
                mListHostInfo.get(i).setIsOnline(false);
                updateRecipientInfo(hostInfo, ClientType.TYPE_QUIT);
                return true;
            }
        }
        return false;
    }

    protected void updateRecipientInfo(HostInfo hostInfo, ClientType type) {
        adapterRecipientList = new AdapterRecipientList(this, mListHostInfo);
        mLvRecipientList.setAdapter(adapterRecipientList);
    }


    protected void sendChannelFoundMessage() {
        List<HostInfo> clientList = new ArrayList<>();
        if (isPrivateChannel() && mListHostInfo != null) {
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
                if (!receiver.getIpAddress().equals(myIpAddress)) {
                    SendMessageAsync sendMessageAsync = new SendMessageAsync();
                    sendMessageAsync.execute(receiver, message);
                }
            }
        }
    }

    protected FileMessage generateFileMessage(String fileName, int fileType) {
        FileMessage fileMessage = new FileMessage();
        fileMessage.setDeviceName(Utils.getDeviceName(mSharedPref));
        fileMessage.setChannelNumber(channelNumber);
        fileMessage.setFileName(fileName);
        fileMessage.setFileType(fileType);

        return fileMessage;
    }
    protected void sendFileMessage(File file, FileMessage fileMessage, final int chatHistoryIndex) {
        if(fileMessage.getFileName() == null) {
            Toast.makeText(this,"No file to send",Toast.LENGTH_LONG).show();
        }

        try {
            SendFileDataTCP sendFileDataTCP = new SendFileDataTCP();
            sendFileDataTCP.setFile(file);
            sendFileDataTCP.setClientIPAddressList(mListHostInfo);
            sendFileDataTCP.setMyIpAddress(myIpAddress);
            sendFileDataTCP.setOnPreExecute(new SendFileDataTCP.OnPreExecute() {
                @Override
                public void onPreExecute() {
                    //mLayoutProgress.setVisibility(View.VISIBLE);
                    adapterChatHistory.updateProgress(chatHistoryIndex, 0, false);
                }
            });
            sendFileDataTCP.setOnProgressUpdate(new SendFileDataTCP.OnProgressUpdate() {
                @Override
                public void onProgressUpdate(int progress) {
                    /*if (progress > 100) {
                        progress = 100;
                    }
                    Log.e("TAG_LOG", "Progress Value: " + progress);
                    mTvPercent.setText("" + progress + "%");
                    mProgress.setProgress(progress);
                    mProgress.setProgress(progress);*/
                    adapterChatHistory.updateProgress(chatHistoryIndex, progress, false);
                }
            });

            sendFileDataTCP.setOnPostExecute(new SendFileDataTCP.OnPostExecute() {
                @Override
                public void onPostExecute(boolean isExecuted) {
                    if (isExecuted) {
                        Toast.makeText(getApplicationContext(), "File sent", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "File sending failed", Toast.LENGTH_LONG).show();
                        adapterChatHistory.updateProgress(chatHistoryIndex, -1, true);
                    }
                    mLayoutProgress.setVisibility(View.GONE);
                }
            });
            sendFileDataTCP.execute(fileMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void makeVoiceCall() {
        if(isStreaming) {
            stopStreaming();
            mIvStreamVoice.setImageResource(R.drawable.ic_record);
        } else {
            Thread recordThread = new Thread(new Runnable(){
                @Override
                public void run() {
                    isStreaming = true;
                    startStreaming();
                }
            });
            recordThread.start();
            mIvStreamVoice.setImageResource(R.drawable.ic_stop);
        }
    }

    protected void stopStreaming() {
        isStreaming = false;
    }

    protected void startStreaming(){
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
            while(isStreaming){
                int numberOfShort = audioRecord.read(audioData, 0, minBufferSize);
                if(numberOfShort>0) {
                    byte[] audioBytes = Utils.shortArrayToByteArray(audioData);
                    SendVoiceChatAsync sendVoiceChatAsync = new SendVoiceChatAsync();
                    sendVoiceChatAsync.setVoiceBytes(audioBytes);
                    sendVoiceChatAsync.setMyIpAddress(myIpAddress);
                    sendVoiceChatAsync.setClientIPAddressList(mListHostInfo);
                    sendVoiceChatAsync.execute();
                }
            }
            audioRecord.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected enum ClientType {
        TYPE_CREATOR,
        TYPE_JOINER,
        TYPE_QUIT
    }

    protected abstract boolean isPrivateChannel();
}
