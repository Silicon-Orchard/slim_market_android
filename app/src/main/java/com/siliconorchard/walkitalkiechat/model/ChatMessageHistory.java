package com.siliconorchard.walkitalkiechat.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by adminsiriconorchard on 6/10/16.
 */
public class ChatMessageHistory implements Parcelable {

    private String ipAddress;
    private String deviceName;
    private String deviceId;
    private String message;
    private boolean isSent;
    private String filePath;
    private String fileName;
    private int fileType;

    public ChatMessageHistory() {

    }

    public ChatMessageHistory(Parcel in) {
        this.ipAddress = in.readString();
        this.deviceName = in.readString();
        this.deviceId = in.readString();
        this.message = in.readString();
        this.isSent = in.readInt() == 1 ? true : false;
        this.filePath = in.readString();
        this.fileName = in.readString();
        this.fileType = in.readInt();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setIsSent(boolean sentOrReceived) {
        this.isSent = sentOrReceived;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ipAddress);
        dest.writeString(this.deviceName);
        dest.writeString(this.deviceId);
        dest.writeString(this.message);
        dest.writeInt(this.isSent ? 1 : 0);
        dest.writeString(this.filePath);
        dest.writeString(this.fileName);
        dest.writeInt(this.fileType);
    }

    public static final Creator CREATOR = new Creator() {
        public ChatMessageHistory createFromParcel(Parcel in) {
            return new ChatMessageHistory(in);
        }

        public ChatMessageHistory[] newArray(int size) {
            return new ChatMessageHistory[size];
        }
    };


    public static final int FILE_TYPE_AUDIO = 1;
    public static final int FILE_TYPE_VIDEO = 2;
    public static final int FILE_TYPE_PHOTO = 3;
    public static final int FILE_TYPE_OTHERS = 4;
}
