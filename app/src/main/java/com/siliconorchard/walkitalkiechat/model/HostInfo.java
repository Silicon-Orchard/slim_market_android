package com.siliconorchard.walkitalkiechat.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by adminsiriconorchard on 4/18/16.
 */
public class HostInfo implements Parcelable{
    private String ipAddress;
    private String deviceId;
    private String deviceName;
    private boolean isChecked;

    public HostInfo() {

    }

    public HostInfo(Parcel in) {
        this.ipAddress = in.readString();
        this.deviceId = in.readString();
        this.deviceName = in.readString();
        this.isChecked = in.readInt() == 1 ? true : false;
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

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ipAddress);
        dest.writeString(this.deviceId);
        dest.writeString(this.deviceName);
        dest.writeInt(this.isChecked ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator CREATOR = new Creator() {
        public HostInfo createFromParcel(Parcel in) {
            return new HostInfo(in);
        }

        public HostInfo[] newArray(int size) {
            return new HostInfo[size];
        }
    };
}
