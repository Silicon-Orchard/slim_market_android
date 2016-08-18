package com.siliconorchard.walkitalkiechat.runnable;

import android.util.Log;

import com.siliconorchard.walkitalkiechat.model.FileMessage;
import com.siliconorchard.walkitalkiechat.model.ReceiveFileTCP;
import com.siliconorchard.walkitalkiechat.utilities.Constant;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by adminsiriconorchard on 8/18/16.
 */
public class RunnableReceiveFileTCP extends RunnableBase {

    private ServerSocket serverSocket;
    private int portNumber;
    public static final int DATA_PACKET_LENGTH = 65536;

    private HashMap<String, ReceiveFileTCP> mReceivingQueue;

    private int channelNumber;
    private OnReceiveCallBacks mOnReceiveCallBacks;

    public int getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    public RunnableReceiveFileTCP() {
        super(true);
        portNumber = Constant.VOICE_SERVER_PORT;
    }

    @Override
    public void run() {
        try {
            mReceivingQueue = new HashMap<>();
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(portNumber)); // <-- now bind it
            Socket socClient = null;
            while(isRunThread()) {
                socClient = serverSocket.accept();
                new ProcessFile().processFile(socClient);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSocket() {
        try{
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        terminate();
    }

    private class ProcessFile {
        private Socket clientSocket;

        public void processFile(Socket socket) {
            clientSocket = socket;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InputStream inputStream = clientSocket.getInputStream();
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(inputStream));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line = br.readLine();
                        while (line != null) {
                            stringBuilder.append(line);
                            line = br.readLine();
                        }
                        processStringMessage(stringBuilder.toString(), clientSocket.getInetAddress());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void processStringMessage(String message, InetAddress inetAddress) throws JSONException{
        FileMessage fileMessage = new FileMessage(message);
        if(fileMessage.getChannelNumber() != channelNumber) {
            return;
        }
        String keyIpString = inetAddress.getHostAddress();
        Log.e("TAG_LOG","Host Name: "+keyIpString);
        ReceiveFileTCP receiveFile = mReceivingQueue.get(keyIpString);
        if(receiveFile == null) {
            receiveFile = new ReceiveFileTCP(mOnReceiveCallBacks);
            mReceivingQueue.put(keyIpString, receiveFile);
        }
        while(receiveFile.isBusy()) { }
        if(receiveFile.processFileMessage(fileMessage)) {
            mReceivingQueue.remove(keyIpString);
        }
    }

    public void setOnReceiveCallBacks(OnReceiveCallBacks onReceiveCallBacks) {
        this.mOnReceiveCallBacks = onReceiveCallBacks;
    }

    public interface OnReceiveCallBacks {
        int onPreReceive(FileMessage fileMessage);
        void onProgressUpdate(final FileMessage fileMessage, int position);
        void onPostReceive(FileMessage fileMessage, File file, int position);
        void onErrorOccur(String errorText, int position);
    }
}
