package com.siliconorchard.walkitalkiechat.model;

import android.os.Parcel;

/**
 * Created by adminsiriconorchard on 4/28/16.
 */
public class ChannelInfo extends HostInfo{

    private int channelNumber;

    public ChannelInfo() {

    }
    public ChannelInfo(Parcel in) {
        super(in);
        this.channelNumber = in.readInt();
    }

    public int getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(channelNumber);
    }

    public static final Creator CREATOR = new Creator() {
        public ChannelInfo createFromParcel(Parcel in) {
            return new ChannelInfo(in);
        }

        public ChannelInfo[] newArray(int size) {
            return new ChannelInfo[size];
        }
    };
}
