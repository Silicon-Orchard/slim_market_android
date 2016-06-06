package com.siliconorchard.walkitalkiechat.asynctasks;

import android.os.AsyncTask;

import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.model.VoiceMessage;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

/**
 * Created by adminsiriconorchard on 5/16/16.
 */
public class SendVoiceChatAsync extends AsyncTask<VoiceMessage, Integer, Boolean> {

    private byte[] voiceBytes;

    private List<HostInfo> mHostClientList;

    private String myIpAddress;

    public String getMyIpAddress() {
        return myIpAddress;
    }

    public void setMyIpAddress(String myIpAddress) {
        this.myIpAddress = myIpAddress;
    }

    public List<HostInfo> getClientIPAddressList() {
        return mHostClientList;
    }

    public void setClientIPAddressList(List<HostInfo> hostList) {
        this.mHostClientList = hostList;
    }

    public byte[] getVoiceBytes() {
        return voiceBytes;
    }

    public void setVoiceBytes(byte[] bytes) {
        this.voiceBytes = bytes;
    }

    @Override
    protected Boolean doInBackground(VoiceMessage... params) {
        if(mHostClientList == null || mHostClientList.size() < 1) {
            return false;
        }
        try {
            VoiceMessage voiceMessage = null;//params[0];
            String message = null;
            if(voiceMessage != null) {
                message = voiceMessage.getJsonString();
            }
            for(int i = 0; i<mHostClientList.size(); i++) {
                if(!mHostClientList.get(i).getIpAddress().equals(myIpAddress)) {
                    InetAddress receiverAddress = InetAddress.getByName(mHostClientList.get(i).getIpAddress());
                    if(message != null) {
                        byte[] buffer1 = message.getBytes();
                        DatagramPacket packet = new DatagramPacket(
                                buffer1, buffer1.length, receiverAddress, Constant.VOICE_CHAT_PORT);

                        DatagramSocket datagramSocket = new DatagramSocket();
                        datagramSocket.send(packet);
                    } else {
                        DatagramPacket packet = new DatagramPacket(
                                voiceBytes, voiceBytes.length, receiverAddress, Constant.VOICE_CHAT_PORT);

                        DatagramSocket datagramSocket = new DatagramSocket();
                        datagramSocket.send(packet);
                    }
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
