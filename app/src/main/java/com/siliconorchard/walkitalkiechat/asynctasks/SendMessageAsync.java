package com.siliconorchard.walkitalkiechat.asynctasks;

import android.os.AsyncTask;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.utilities.Constant;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by adminsiriconorchard on 4/25/16.
 */
public class SendMessageAsync extends AsyncTask<Object, Void, Integer> {

    protected DatagramSocket mDatagramSocket;
    protected boolean isCloseSocket;

    @Override
    protected Integer doInBackground(Object... params) {
        int retVal = 0;
        try {
            if(isCloseSocket) {
                return retVal;
            }
            HostInfo hostInfo = (HostInfo) params[0];
            String message = (String)params[1];
            mDatagramSocket = new DatagramSocket();
            byte[] buffer = message.getBytes();
            InetAddress receiverAddress = InetAddress.getByName(hostInfo.getIpAddress());
            DatagramPacket packet = new DatagramPacket(
                    buffer, buffer.length, receiverAddress, Constant.FIRST_SERVER_PORT);
            mDatagramSocket.send(packet);
            retVal = 1;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }


    public void closeSocket() {
        if(mDatagramSocket != null) {
            try {
                mDatagramSocket.close();
                mDatagramSocket = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isCloseSocket = true;
    }

    public boolean isCloseSocket() {
        return isCloseSocket;
    }

    public void setIsCloseSocket(boolean isCloseSocket) {
        this.isCloseSocket = isCloseSocket;
    }
}
