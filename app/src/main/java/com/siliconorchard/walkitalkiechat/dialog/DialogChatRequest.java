package com.siliconorchard.walkitalkiechat.dialog;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.activities.ActivityChatOne2One;
import com.siliconorchard.walkitalkiechat.asynctasks.SendMessageAsync;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import org.json.JSONException;

/**
 * Created by adminsiriconorchard on 6/14/16.
 */
public class DialogChatRequest extends DialogFragment implements
        android.view.View.OnClickListener {

    private View rootView;
    private TextView mTvChatReq;
    private Button mBtnAccept;
    private Button mBtnDecline;
    private HostInfo mHostInfo;

    private String myIpAddress;
    private SharedPreferences mSharedPref;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_chat_request, container, false);
        initView();
        initListener();
        return rootView;
    }

    private void initView() {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        mSharedPref = getActivity().getSharedPreferences(Constant.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mTvChatReq = (TextView) rootView.findViewById(R.id.tv_chat_req);
        mBtnAccept = (Button) rootView.findViewById(R.id.btn_accept);
        mBtnDecline = (Button) rootView.findViewById(R.id.btn_decline);
        myIpAddress = getArguments().getString(Constant.KEY_MY_IP_ADDRESS);
        mHostInfo = (HostInfo) getArguments().getParcelable(Constant.KEY_HOST_INFO);
        if (mHostInfo != null) {
            mTvChatReq.setText(String.format(getString(R.string._wants_to_chat_with_you), mHostInfo.getDeviceName()));
        } else {
            dismiss();
        }
    }

    private void initListener() {
        mBtnAccept.setOnClickListener(this);
        mBtnDecline.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_accept:
                sendChatAcceptMessage(ChatMessage.TYPE_ONE_TO_ONE_CHAT_ACCEPT);
                startOne2OneChat();
                dismiss();
                break;
            case R.id.btn_decline:
                sendChatAcceptMessage(ChatMessage.TYPE_ONE_TO_ONE_CHAT_DECLINE);
                dismiss();
                break;
            default:
                break;
        }
    }


    private void startOne2OneChat() {
        Intent intent = new Intent(getActivity(), ActivityChatOne2One.class);
        intent.putExtra(Constant.KEY_HOST_INFO, mHostInfo);
        intent.putExtra(Constant.KEY_CHANNEL_NUMBER, 0);
        intent.putExtra(Constant.KEY_MY_IP_ADDRESS, myIpAddress);
        getActivity().startActivity(intent);
    }

    private void sendChatAcceptMessage(int messageType) {
        try {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setIpAddress(myIpAddress);
            chatMessage.setDeviceId(Utils.getDeviceId(getActivity(), mSharedPref));
            chatMessage.setDeviceName(Utils.getDeviceName(mSharedPref));
            chatMessage.setType(messageType);
            String message = chatMessage.getJsonString();
            if (mHostInfo != null) {
                SendMessageAsync sendMessageAsync = new SendMessageAsync();
                sendMessageAsync.execute(mHostInfo, message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}