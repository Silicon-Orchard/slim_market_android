package com.siliconorchard.walkitalkiechat.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adminsiriconorchard on 5/10/16.
 */
public class FileMessage {
    private String deviceName;
    private int totalChunkCount;
    private int currentChunkNo;
    private String voiceMessage;
    private int channelNumber;
    private String fileName;
    private int fileType;

    private static final String JSON_KEY_DEVICE_NAME = "device_name";
    private static final String JSON_KEY_TOTAL_CHUNK_COUNT = "file_chunk_count";
    private static final String JSON_KEY_CURRENT_CHUNK_NO = "file_current_chunk";
    private static final String JSON_KEY_VOICE_MESSAGE = "file_message";
    private static final String JSON_KEY_CHANNEL_NUMBER = "channel_id";
    private static final String JSON_KEY_FILE_NAME = "file_name";
    private static final String JSON_KEY_FILE_TYPE = "file_type";


    public FileMessage() {

    }

    public FileMessage(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        this.deviceName = jsonObject.getString(JSON_KEY_DEVICE_NAME);
        if(jsonObject.has(JSON_KEY_TOTAL_CHUNK_COUNT)) {
            this.totalChunkCount = jsonObject.getInt(JSON_KEY_TOTAL_CHUNK_COUNT);
            this.currentChunkNo = jsonObject.getInt(JSON_KEY_CURRENT_CHUNK_NO);
        }
        this.voiceMessage = jsonObject.getString(JSON_KEY_VOICE_MESSAGE);
        this.channelNumber = jsonObject.getInt(JSON_KEY_CHANNEL_NUMBER);
        this.fileName = jsonObject.getString(JSON_KEY_FILE_NAME);
        this.fileType = jsonObject.getInt(JSON_KEY_FILE_TYPE);
    }

    public String getJsonString() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_KEY_DEVICE_NAME, this.deviceName);
        jsonObject.put(JSON_KEY_TOTAL_CHUNK_COUNT, this.totalChunkCount);
        jsonObject.put(JSON_KEY_CURRENT_CHUNK_NO, this.currentChunkNo);
        jsonObject.put(JSON_KEY_VOICE_MESSAGE, this.voiceMessage);
        jsonObject.put(JSON_KEY_CHANNEL_NUMBER, this.channelNumber);
        jsonObject.put(JSON_KEY_FILE_NAME, this.fileName);
        jsonObject.put(JSON_KEY_FILE_TYPE, this.fileType);
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }
}
