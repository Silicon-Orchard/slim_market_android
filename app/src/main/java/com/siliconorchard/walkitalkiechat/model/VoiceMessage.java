package com.siliconorchard.walkitalkiechat.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adminsiriconorchard on 5/10/16.
 */
public class VoiceMessage {
    private String deviceName;
    private int totalChunkCount;
    private int currentChunkNo;
    private String voiceMessage;
    private int channelNumber;

    private static final String JSON_KEY_DEVICE_NAME = "device_name";
    private static final String JSON_KEY_TOTAL_CHUNK_COUNT = "voice_message_chunkCount";
    private static final String JSON_KEY_CURRENT_CHUNK_NO = "voice_message_current_chunk";
    private static final String JSON_KEY_VOICE_MESSAGE = "voice_message";
    private static final String JSON_KEY_CHANNEL_NUMBER = "channel_id";


    public VoiceMessage() {

    }

    public VoiceMessage(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        this.deviceName = jsonObject.getString(JSON_KEY_DEVICE_NAME);
        if(jsonObject.has(JSON_KEY_TOTAL_CHUNK_COUNT)) {
            this.totalChunkCount = jsonObject.getInt(JSON_KEY_TOTAL_CHUNK_COUNT);
            this.currentChunkNo = jsonObject.getInt(JSON_KEY_CURRENT_CHUNK_NO);
        }
        this.voiceMessage = jsonObject.getString(JSON_KEY_VOICE_MESSAGE);
        this.channelNumber = jsonObject.getInt(JSON_KEY_CHANNEL_NUMBER);
    }

    public String getJsonString() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_KEY_DEVICE_NAME, this.deviceName);
        jsonObject.put(JSON_KEY_TOTAL_CHUNK_COUNT, this.totalChunkCount);
        jsonObject.put(JSON_KEY_CURRENT_CHUNK_NO, this.currentChunkNo);
        jsonObject.put(JSON_KEY_VOICE_MESSAGE, this.voiceMessage);
        jsonObject.put(JSON_KEY_CHANNEL_NUMBER, this.channelNumber);
        return jsonObject.toString();
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getTotalChunkCount() {
        return totalChunkCount;
    }

    public void setTotalChunkCount(int totalChunkCount) {
        this.totalChunkCount = totalChunkCount;
    }

    public int getCurrentChunkNo() {
        return currentChunkNo;
    }

    public void setCurrentChunkNo(int currentChunkNo) {
        this.currentChunkNo = currentChunkNo;
    }

    public String getVoiceMessage() {
        return voiceMessage;
    }

    public void setVoiceMessage(String voiceMessage) {
        this.voiceMessage = voiceMessage;
    }

    public int getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }
}
