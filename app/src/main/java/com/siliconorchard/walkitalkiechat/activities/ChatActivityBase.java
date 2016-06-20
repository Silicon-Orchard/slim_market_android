package com.siliconorchard.walkitalkiechat.activities;

import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
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
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.ChatMessageHistory;
import com.siliconorchard.walkitalkiechat.model.FileMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import org.json.JSONException;

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

    protected void addFileMessage(FileMessage fileMessage, String msg, boolean isSent, String filePath) {
        ChatMessageHistory chatMessage = new ChatMessageHistory();
        chatMessage.setDeviceName(fileMessage.getDeviceName());
        chatMessage.setMessage(msg);
        chatMessage.setIsSent(isSent);
        chatMessage.setFilePath(filePath);
        chatMessage.setFileType(fileMessage.getFileType());
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
                toastUnderConstructionMessage();
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

    protected enum ClientType {
        TYPE_CREATOR,
        TYPE_JOINER,
        TYPE_QUIT
    }

    protected abstract boolean isPrivateChannel();
}
