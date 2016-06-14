package com.siliconorchard.walkitalkiechat.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adminsiriconorchard on 4/13/16.
 */
public class ChatMessage {

    private String ipAddress;
    private String deviceName;
    private String deviceId;
    private String message;
    private int type;
    private HostInfo clientInfo;
    private int channelNumber;

    private List<HostInfo> clientList;

    private static final String JSON_KEY_IP_ADDRESS = "ip_address";
    private static final String JSON_KEY_DEVICE_NAME = "device_name";
    private static final String JSON_KEY_DEVICE_ID = "device_id";
    private static final String JSON_KEY_MESSAGE = "message";
    private static final String JSON_KEY_TYPE = "type";
    private static final String JSON_KEY_CHANNEL_NUMBER = "channel_id";
    private static final String JSON_KEY_CLIENT_ARRAY = "channel_members";
    private static final String JSON_KEY_CLIENT_INFO = "client_info";

    public static final int TYPE_MESSAGE = 1;
    public static final int TYPE_ADD_CLIENT = 2;
    public static final int TYPE_REQUEST_INFO = 3;
    public static final int TYPE_RECEIVE_INFO = 4;
    public static final int TYPE_CREATE_CHANNEL = 5;
    public static final int TYPE_JOIN_CHANNEL = 6;
    public static final int TYPE_CHANNEL_FOUND = 7;
    public static final int TYPE_CHANNEL_DUPLICATE = 8;
    public static final int TYPE_LEFT_CHANNEL = 9;
    public static final int TYPE_LEFT_APPLICATION = 10;
    public static final int TYPE_ONE_TO_ONE_CHAT_REQUEST = 11;
    public static final int TYPE_ONE_TO_ONE_CHAT_ACCEPT = 12;



    public ChatMessage() {

    }

    public ChatMessage(String jsonString) throws JSONException{
        JSONObject jsonObject = new JSONObject(jsonString);
        this.type = jsonObject.getInt(JSON_KEY_TYPE);
        this.ipAddress = jsonObject.getString(JSON_KEY_IP_ADDRESS);
        this.deviceName = jsonObject.getString(JSON_KEY_DEVICE_NAME);
        this.deviceId = jsonObject.getString(JSON_KEY_DEVICE_ID);
        switch (this.type) {
            case TYPE_MESSAGE:
                this.message = jsonObject.getString(JSON_KEY_MESSAGE);
                this.channelNumber = jsonObject.getInt(JSON_KEY_CHANNEL_NUMBER);
                break;
            case TYPE_ADD_CLIENT:
                clientInfo = new HostInfo();
                JSONObject clientObj = jsonObject.getJSONObject(JSON_KEY_CLIENT_INFO);
                clientInfo.setIpAddress(clientObj.getString(JSON_KEY_IP_ADDRESS));
                if(clientObj.has(JSON_KEY_DEVICE_ID)) {
                    clientInfo.setDeviceId(clientObj.getString(JSON_KEY_DEVICE_ID));
                }
                clientInfo.setDeviceName(clientObj.getString(JSON_KEY_DEVICE_NAME));
                this.channelNumber = jsonObject.getInt(JSON_KEY_CHANNEL_NUMBER);
                break;
            case TYPE_REQUEST_INFO:
            case TYPE_RECEIVE_INFO:
            case TYPE_LEFT_APPLICATION:
            case TYPE_ONE_TO_ONE_CHAT_REQUEST:
            case TYPE_ONE_TO_ONE_CHAT_ACCEPT:
                break;

            case TYPE_CHANNEL_FOUND:
                if(jsonObject.has(JSON_KEY_CLIENT_ARRAY)) {
                    JSONArray jsonArray = jsonObject.getJSONArray(JSON_KEY_CLIENT_ARRAY);
                    this.clientList = new ArrayList<>();
                    for(int i = 0; i<jsonArray.length(); i++) {
                        JSONObject hostObj = jsonArray.getJSONObject(i);
                        HostInfo hostInfo = new HostInfo();
                        hostInfo.setIpAddress(hostObj.getString(JSON_KEY_IP_ADDRESS));
                        if(hostObj.has(JSON_KEY_DEVICE_ID)) {
                            hostInfo.setDeviceId(hostObj.getString(JSON_KEY_DEVICE_ID));
                        }
                        hostInfo.setDeviceName(hostObj.getString(JSON_KEY_DEVICE_NAME));
                        clientList.add(hostInfo);
                    }
                }
            case TYPE_CREATE_CHANNEL:
            case TYPE_JOIN_CHANNEL:
            case TYPE_CHANNEL_DUPLICATE:
            case TYPE_LEFT_CHANNEL:
                this.channelNumber = jsonObject.getInt(JSON_KEY_CHANNEL_NUMBER);
                break;
        }

    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public HostInfo getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(HostInfo hostInfo) {
        this.clientInfo = hostInfo;
    }

    public int getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }


    public List<HostInfo> getClientList() {
        return clientList;
    }

    public void setClientList(List<HostInfo> listClients) {
        this.clientList = listClients;
    }

    public String getJsonString() throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_KEY_TYPE, this.type);

        jsonObject.put(JSON_KEY_IP_ADDRESS, this.ipAddress);
        jsonObject.put(JSON_KEY_DEVICE_NAME, this.deviceName);
        jsonObject.put(JSON_KEY_DEVICE_ID, this.deviceId);

        switch (this.type) {
            case TYPE_MESSAGE:
                jsonObject.put(JSON_KEY_MESSAGE, this.message);
                jsonObject.put(JSON_KEY_CHANNEL_NUMBER, this.channelNumber);
                break;
            case TYPE_ADD_CLIENT:
                JSONObject clientObj = new JSONObject();
                clientObj.put(JSON_KEY_IP_ADDRESS, clientInfo.getIpAddress());
                clientObj.put(JSON_KEY_DEVICE_NAME, clientInfo.getDeviceName());
                clientObj.put(JSON_KEY_DEVICE_ID, clientInfo.getDeviceId());
                jsonObject.put(JSON_KEY_CLIENT_INFO, clientObj);
                jsonObject.put(JSON_KEY_CHANNEL_NUMBER, this.channelNumber);
                break;

            case TYPE_REQUEST_INFO:
            case TYPE_RECEIVE_INFO:
            case TYPE_LEFT_APPLICATION:
            case TYPE_ONE_TO_ONE_CHAT_REQUEST:
            case TYPE_ONE_TO_ONE_CHAT_ACCEPT:
                break;

            case TYPE_CHANNEL_FOUND:
                if(this.clientList != null && this.clientList.size()>0) {
                    JSONArray jsonArray = new JSONArray();
                    for(int i = 0; i< clientList.size(); i++) {
                        JSONObject hostObj = new JSONObject();
                        HostInfo hostInfo = clientList.get(i);
                        hostObj.put(JSON_KEY_IP_ADDRESS, hostInfo.getIpAddress());
                        hostObj.put(JSON_KEY_DEVICE_NAME, hostInfo.getDeviceName());
                        hostObj.put(JSON_KEY_DEVICE_ID, hostInfo.getDeviceId());
                        jsonArray.put(hostObj);
                    }
                    jsonObject.put(JSON_KEY_CLIENT_ARRAY, jsonArray);
                }
            case TYPE_CREATE_CHANNEL:
            case TYPE_JOIN_CHANNEL:
            case TYPE_CHANNEL_DUPLICATE:
            case TYPE_LEFT_CHANNEL:
                jsonObject.put(JSON_KEY_CHANNEL_NUMBER, this.channelNumber);
                break;
        }
        return jsonObject.toString();
    }

}
