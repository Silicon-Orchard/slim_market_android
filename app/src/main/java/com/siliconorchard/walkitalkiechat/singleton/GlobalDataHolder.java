package com.siliconorchard.walkitalkiechat.singleton;

import com.siliconorchard.walkitalkiechat.discovery.Network.HostBean;
import com.siliconorchard.walkitalkiechat.model.ChannelInfo;
import com.siliconorchard.walkitalkiechat.model.HostInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adminsiriconorchard on 4/20/16.
 */
public class GlobalDataHolder {
    private static GlobalDataHolder mInstance;
    private static List<HostBean> mListHostBean;
    private static List<HostInfo> mListHostInfo;
    private static List<ChannelInfo> myChannels;
    private static List<ChannelInfo> otherChannels;

    private GlobalDataHolder() {

    }

    public static GlobalDataHolder getInstance() {
        if(mInstance == null) {
            mInstance = new GlobalDataHolder();
        }
        return mInstance;
    }

    public List<HostBean> getListHostBean() {
        return mListHostBean;
    }

    public void setListHostBean(List<HostBean> listHostBean) {
        GlobalDataHolder.mListHostBean = listHostBean;
    }

    public List<HostInfo> getListHostInfo() {
        return mListHostInfo;
    }

    public void setListHostInfo(List<HostInfo> listHostInfo) {
        GlobalDataHolder.mListHostInfo = listHostInfo;
    }

    public void addToHostList(HostInfo hostInfo) {
        if(mListHostInfo == null) {
            mListHostInfo = new ArrayList<>();
        }
        for(int i = 0; i<mListHostInfo.size(); i++) {
            if(mListHostInfo.get(i).getIpAddress().equals(hostInfo.getIpAddress())) {
                return;
            }
        }
        mListHostInfo.add(hostInfo);
    }

    public void removeFromHostList(HostInfo hostInfo) {
        if(mListHostInfo == null) {
            return;
        }
        for(int i = 0; i<mListHostInfo.size(); i++) {
            if(mListHostInfo.get(i).getIpAddress().equals(hostInfo.getIpAddress())) {
                mListHostInfo.remove(i);
                return;
            }
        }
    }

    public List<ChannelInfo> getMyChannels() {
        return myChannels;
    }

    public void setMyChannels(List<ChannelInfo> myChannels) {
        GlobalDataHolder.myChannels = myChannels;
    }

    public List<ChannelInfo> getOtherChannels() {
        return otherChannels;
    }

    public void setOtherChannels(List<ChannelInfo> otherChannels) {
        GlobalDataHolder.otherChannels = otherChannels;
    }

    public void addToMyChannelList(ChannelInfo channelInfo) {
        if(myChannels == null) {
            myChannels = new ArrayList<>();
            myChannels.add(channelInfo);
            return;
        }
        for(int i = 0; i<myChannels.size(); i++) {
            if(myChannels.get(i).getChannelNumber() == channelInfo.getChannelNumber()) {
                return;
            }
        }
        myChannels.add(channelInfo);
    }

    public void addToOtherChannelList(ChannelInfo channelInfo) {
        if(otherChannels == null) {
            otherChannels = new ArrayList<>();
            otherChannels.add(channelInfo);
            return;
        }
        for(int i = 0; i<otherChannels.size(); i++) {
            if(otherChannels.get(i).getChannelNumber() == channelInfo.getChannelNumber()) {
                return;
            }
        }
        otherChannels.add(channelInfo);
    }

    public boolean isChannelExists(int channelNumber) {
        return isChannelExistsInMyChannels(channelNumber) || isChannelExistsInOtherChannels(channelNumber);
    }

    public boolean isChannelExistsInMyChannels(int channelNumber) {
        if(myChannels != null) {
            for(int i = 0; i<myChannels.size(); i++) {
                if(channelNumber == myChannels.get(i).getChannelNumber()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isChannelExistsInOtherChannels(int channelNumber) {
        if(otherChannels != null) {
            for(int i = 0; i<otherChannels.size(); i++) {
                if(channelNumber == otherChannels.get(i).getChannelNumber()) {
                    return true;
                }
            }
        }
        return false;
    }

    public HostInfo getHostInfoByChannelNumber(int channelNumber) {
        if(otherChannels != null) {
            for(int i = 0; i<otherChannels.size(); i++) {
                if(channelNumber == otherChannels.get(i).getChannelNumber()) {
                    return otherChannels.get(i);
                }
            }
        }
        return null;
    }
}
