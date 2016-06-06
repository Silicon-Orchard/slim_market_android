package com.siliconorchard.walkitalkiechat.runnable;

import android.util.Base64;
import android.util.Log;

import com.siliconorchard.walkitalkiechat.model.VoiceMessage;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by adminsiriconorchard on 5/6/16.
 */
public class RunnableReceiveFile extends RunnableBase {

    private DatagramSocket sDataGramSocket;
    public static final int DATA_PACKET_LENGTH = 65508;//16384;
    private String receivedDataString = null;
    private int numOfReceivedMsg = 0;
    private OnReceiveCallBacks mOnReceiveCallBacks;
    private int channelNumber;

    private File mFile;

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
                processReceivedVoiceData(receivedData);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private synchronized void processData(byte[] receivedData) {
        try {
            String message = new String(receivedData);
            VoiceMessage voiceMessage = new VoiceMessage(message);
            if(voiceMessage.getChannelNumber() != channelNumber) {
                return;
            }
            if(numOfReceivedMsg == 0 && mOnReceiveCallBacks != null) {
                mOnReceiveCallBacks.onPreReceive(voiceMessage);
            }
            numOfReceivedMsg++;
            //Log.e("TAG_LOG","Received message\n"+message);
            if(receivedDataString == null) {
                String nullText = "";
                receivedDataString = Base64.encodeToString(nullText.getBytes(), Base64.NO_WRAP);
            }




            final boolean isContinue = voiceMessage.getCurrentChunkNo()<voiceMessage.getTotalChunkCount();
            Log.e("TAG_LOG","Current/Total, isContinue: "+voiceMessage.getCurrentChunkNo()+"/"+voiceMessage.getTotalChunkCount()+","+isContinue);
            if(mOnReceiveCallBacks != null && isContinue) {
                mOnReceiveCallBacks.onProgressUpdate(voiceMessage);
            }

            if(isContinue) {
                receivedDataString = receivedDataString + voiceMessage.getVoiceMessage();
            } else {
                receivedDataString = receivedDataString + voiceMessage.getVoiceMessage();
                if(numOfReceivedMsg != voiceMessage.getTotalChunkCount()) {
                    String errorMessage = "Data missing occurs,(Received/Sent) "+numOfReceivedMsg+"/"+voiceMessage.getTotalChunkCount();
                    onError(errorMessage);
                    return;
                }
                if(mFile == null) {
                    mFile = Utils.createFile(Constant.BASE_PATH+File.separator+Constant.FOLDER_NAME, voiceMessage.getDeviceName()+".mp3");
                }
                if(mFile == null) {
                    onError("File cannot be created");
                    return;
                }
                //Log.e("TAG_LOG","Received/Sent: "+numOfReceivedMsg+"/"+voiceMessage.getTotalChunkCount());
                //Log.e("TAG_LOG","ReceivedData: \n"+receivedDataString);
                byte[] filePart = Base64.decode(receivedDataString, Base64.NO_WRAP);
                FileOutputStream out = new FileOutputStream(mFile.getAbsolutePath());
                out.write(filePart);
                out.close();
                if(mOnReceiveCallBacks != null) {
                    mOnReceiveCallBacks.onPostReceive(voiceMessage, mFile);
                }
                receivedDataString = null;
                mFile = null;
                numOfReceivedMsg = 0;
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
        receivedDataString = null;
        numOfReceivedMsg = 0;
    }


    public static interface OnReceiveCallBacks {
        public abstract void onPreReceive(VoiceMessage voiceMessage);
        public abstract void onProgressUpdate(final VoiceMessage voiceMessage);
        public abstract void onPostReceive(VoiceMessage voiceMessage, File file);
        public abstract void onErrorOccur(String errorText);
    }

    private void processReceivedVoiceData(byte[] data) {
        new Thread(new RunnableProcessVoiceBytes(data)).start();
    }
    private class RunnableProcessVoiceBytes extends RunnableBase{
        private byte[] receivedBytes;
        public RunnableProcessVoiceBytes(byte[] data) {
            super(true);
            this.receivedBytes = data;
        }

        @Override
        public void run() {
            processData(receivedBytes);
        }
    }
}
