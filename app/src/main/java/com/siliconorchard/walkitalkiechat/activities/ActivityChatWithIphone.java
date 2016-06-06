package com.siliconorchard.walkitalkiechat.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.asynctasks.SendMessageAsync;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by adminsiriconorchard on 4/17/16.
 */
public class ActivityChatWithIphone extends ActivityBase {

    private EditText mEtIp;
    private EditText mEtPort;
    private EditText mEtMessage;
    private TextView mTvMessage;
    private Button mBtnSend;

    private String ipAddress;
    private int portNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_iphone);
        initView();
        initListener();
    }

    private void initView() {
        mEtIp = (EditText) findViewById(R.id.et_ip_address);
        mEtPort = (EditText) findViewById(R.id.et_port_number);
        mEtMessage= (EditText) findViewById(R.id.et_message);
        mTvMessage = (TextView) findViewById(R.id.tv_message_box);
        mBtnSend = (Button) findViewById(R.id.btn_send);

        mEtIp.setText("192.168.1.");
    }

    private void initListener() {
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ipAddress = mEtIp.getText().toString();
                portNumber = Integer.parseInt(mEtPort.getText().toString());
                sendMessageToIPhone(mEtMessage.toString().trim());
            }
        });
    }

    private void sendMessageToIPhone(String strMsg) {
        if(portNumber< 1) {
            Toast.makeText(this, "Port number not found", Toast.LENGTH_LONG).show();
            return;
        }
        if(strMsg == null || strMsg.length()<1) {
            Toast.makeText(this, "Nothing to send", Toast.LENGTH_LONG).show();
            return;
        }
        HashMap<String, String> message = new HashMap<>();
        message.put("message", strMsg);
        message.put("from","android");
        SendMessageAsync clientAST = new SendMessageAsync();
        //ipList.add(ipAddress);
        //portList.add(portNumber);
        HostInfo hostInfo = new HostInfo();
        hostInfo.setIpAddress(ipAddress);
        String messageText = mEtMessage.getText().toString().trim();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(messageText);
        chatMessage.setIpAddress("192.168.112");
        chatMessage.setDeviceName("Tamal");
        chatMessage.setDeviceId("fasfasf");
        chatMessage.setType(ChatMessage.TYPE_MESSAGE);
        Log.e("TAG_LOG",messageText);
        try {
            clientAST.execute(hostInfo, chatMessage.getJsonString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
