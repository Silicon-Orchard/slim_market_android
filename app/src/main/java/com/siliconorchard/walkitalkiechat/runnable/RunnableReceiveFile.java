package com.siliconorchard.walkitalkiechat.runnable;

import android.util.Log;

import com.siliconorchard.walkitalkiechat.model.FileMessage;
import com.siliconorchard.walkitalkiechat.model.ReceiveFile;
import com.siliconorchard.walkitalkiechat.utilities.Constant;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * Created by adminsiriconorchard on 5/6/16.
 */
public class RunnableReceiveFile extends RunnableBase {

    private DatagramSocket sDataGramSocket;
    public static final int DATA_PACKET_LENGTH = 65508;//16384;
    private OnReceiveCallBacks mOnReceiveCallBacks;
    private int channelNumber;

    private HashMap<String, ReceiveFile> mReceivingQueue;

    public int getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    public RunnableReceiveFile() {
        super(true);
    }

    public void closeSocket() {
        sDataGramSocket.close();
        terminate();
    }

    public OnReceiveCallBacks getOnReceiveCallBacks() {
        return mOnReceiveCallBacks;
    }

    public void setOnReceiveCallBacks(OnReceiveCallBacks onReceiveCallBacks) {
        this.mOnReceiveCallBacks = onReceiveCallBacks;
    }

    @Override
    public void run() {
        try {
            mReceivingQueue = new HashMap<>();
            sDataGramSocket = new DatagramSocket(Constant.VOICE_SERVER_PORT);
            while (isRunThread()) {
                byte[] buffer = new byte[DATA_PACKET_LENGTH];
                if(sDataGramSocket.isClosed()) {
                    break;
                }
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                sDataGramSocket.receive(datagramPacket);
                Log.e("TAG_LOG", "Ending message receiving from service");
                byte[] receivedData = datagramPacket.getData();
                processReceivedVoiceData(receivedData, datagramPacket.getAddress());

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void processData(byte[] receivedData, InetAddress inetAddress) {
        try {
            String message = new String(receivedData);
            FileMessage fileMessage = new FileMessage(message);
            if(fileMessage.getChannelNumber() != channelNumber) {
                return;
            }
            String keyIpString = inetAddress.getHostAddress();
            Log.e("TAG_LOG","Host Name: "+keyIpString);
            ReceiveFile receiveFile = mReceivingQueue.get(keyIpString);
            if(receiveFile == null) {
                receiveFile = new ReceiveFile();
                mReceivingQueue.put(keyIpString, receiveFile);
            }
            while(receiveFile.isBusy()) { }
            if(receiveFile.processFileMessage(mOnReceiveCallBacks, fileMessage)) {
                mReceivingQueue.remove(keyIpString);
            }

        } catch (Exception e) {
            e.printStackTrace();
            onError(e.toString());
        }
    }

    private void onError(String errorMessage) {
        Log.e("TAG_LOG", errorMessage);
        if(mOnReceiveCallBacks != null) {
            mOnReceiveCallBacks.onErrorOccur(errorMessage);
        }
    }


    public static interface OnReceiveCallBacks {
        public abstract void onPreReceive(FileMessage fileMessage);
        public abstract void onProgressUpdate(final FileMessage fileMessage);
        public abstract void onPostReceive(FileMessage fileMessage, File file);
        public abstract void onErrorOccur(String errorText);
    }

    private void processReceivedVoiceData(byte[] data, InetAddress inetAddress) {
        new Thread(new RunnableProcessVoiceBytes(data, inetAddress)).start();
    }
    private class RunnableProcessVoiceBytes extends RunnableBase{
        private byte[] receivedBytes;
        private InetAddress inetAddress;
        public RunnableProcessVoiceBytes(byte[] data, InetAddress address) {
            super(true);
            this.receivedBytes = data;
            this.inetAddress = address;
        }

        @Override
        public void run() {
            processData(receivedBytes, inetAddress);
        }
    }
}
