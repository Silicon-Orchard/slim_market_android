package com.siliconorchard.walkitalkiechat.runnable;

import android.util.Base64;
import android.util.Log;

import com.siliconorchard.walkitalkiechat.model.VoiceMessage;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by adminsiriconorchard on 5/11/16.
 */
public class RunnableReceiveFileTCP extends RunnableBase {
    private static ServerSocket sServerSocket;

    private int PORT_NUMBER;

    private OnReceiveCallBacks mOnReceiveCallBacks;
    private int channelNumber;

    private File mFile;

    public RunnableReceiveFileTCP(int portNumber) {
        super(true);
        PORT_NUMBER = portNumber;
        PORT_NUMBER = Constant.VOICE_SERVER_PORT;
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    public void closeSocket() {
        try{
            sServerSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            sServerSocket = new ServerSocket();
            sServerSocket.setReuseAddress(true);
            sServerSocket.bind(new InetSocketAddress(PORT_NUMBER)); // <-- now bind it
            Socket socClient = null;
            while (isRunThread()) {
                try {
                    socClient = sServerSocket.accept();
                    socClient.setSoTimeout(2000);
                    InputStream is = socClient.getInputStream();
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(is));
                    while (!br.ready()) {

                    }
                    String message = br.readLine();
                    processReceivedVoiceData(message);
                    br.close();
                    socClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void processVoiceMessage(String message) {
        try{
            Log.e("TAG_LOG",message);
            VoiceMessage voiceMessage = new VoiceMessage(message);
            if(voiceMessage.getChannelNumber() != channelNumber) {
                return;
            }
            if(mOnReceiveCallBacks != null) {
                mOnReceiveCallBacks.onPreReceive(voiceMessage);
            }
            if(mFile == null) {
                mFile = Utils.createFile(Constant.BASE_PATH + File.separator + Constant.FOLDER_NAME, voiceMessage.getDeviceName() + ".mp3");
            }
            if(mFile == null) {
                onError("File cannot be created");
                return;
            }
            byte[] filePart = Base64.decode(voiceMessage.getVoiceMessage(), Base64.NO_WRAP);
            FileOutputStream out = new FileOutputStream(mFile.getAbsolutePath());
            out.write(filePart);
            out.close();
            if(mOnReceiveCallBacks != null) {
                mOnReceiveCallBacks.onPostReceive(voiceMessage, mFile);
            }
            mFile = null;
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

    private void processReceivedVoiceData(String message) {
        new Thread(new RunnableProcessVoiceBytes(message)).start();
    }
    private class RunnableProcessVoiceBytes extends RunnableBase{
        private String receivedMessage;
        public RunnableProcessVoiceBytes(String receivedMessage) {
            super(true);
            this.receivedMessage = receivedMessage;
        }

        @Override
        public void run() {
            processVoiceMessage(receivedMessage);
        }
    }

    public static interface OnReceiveCallBacks {
        public abstract void onPreReceive(VoiceMessage voiceMessage);
        public abstract void onProgressUpdate(final VoiceMessage voiceMessage);
        public abstract void onPostReceive(VoiceMessage voiceMessage, File file);
        public abstract void onErrorOccur(String errorText);
    }
}
