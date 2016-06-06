package com.siliconorchard.walkitalkiechat.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.siliconorchard.walkitalkiechat.utilities.Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by adminsiriconorchard on 4/13/16.
 */
public class ServiceServer extends IntentService {
    private static ServerSocket sServerSocket;

    public ServiceServer() {
        super(Constant.SERVER_SERVICE_NAME);
    }
    public ServiceServer(String name) {
        super(name);
    }

    /*@Override
    protected void onHandleIntent(Intent intent) {
        IP_ADDRESS = intent.getExtras().getString(Constant.KEY_MY_IP_ADDRESS, null);
        PORT_NUMBER = intent.getExtras().getInt(Constant.KEY_MY_PORT, 0);
        try {
            sServerSocket = new ServerSocket();
            sServerSocket.setReuseAddress(true);
            sServerSocket.bind(new InetSocketAddress(PORT_NUMBER)); // <-- now bind it
            Socket socClient = null;
            while (true) {
                try {
                    socClient = sServerSocket.accept();
                    Log.e("TAG_LOG", "Starting message receiving from service");
                    socClient.setSoTimeout(2000);
                    InputStream is = socClient.getInputStream();
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(is));
                    while (!br.ready()) {

                    }
                    String message = br.readLine();
                    br.close();
                    socClient.close();
                    publishResults(message);
                    Log.e("TAG_LOG", "Ending message receiving from service");
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
    }*/



    private static DatagramSocket sDataGramSocket;
    public static final int DATA_PACKET_LENGTH = 65508;

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            sDataGramSocket = new DatagramSocket(Constant.FIRST_SERVER_PORT);
            while (true) {
                try {
                    byte[] buffer = new byte[DATA_PACKET_LENGTH];
                    if(sDataGramSocket.isClosed()) {
                        this.stopSelf();
                        break;
                    }
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                    sDataGramSocket.receive(datagramPacket);
                    byte[] receivedData = datagramPacket.getData();
                    Log.e("TAG_LOG", "Ending message receiving from service");
                    publishResults(receivedData);
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
    private void publishResults(byte[] data) {
        String message = new String(data);
        publishResults(message);
    }

    public static void closeSocket() {
        try {
            if(sServerSocket != null) {
                sServerSocket.close();
            }
            if(sDataGramSocket != null) {
                sDataGramSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void publishResults(String message) {
        Intent intentForeGround = new Intent(Constant.SERVICE_NOTIFICATION_STRING_CHAT_FOREGROUND);
        intentForeGround.putExtra(Constant.KEY_CLIENT_MESSAGE, message);
        sendBroadcast(intentForeGround);

        Intent intentBackGround = new Intent(Constant.SERVICE_NOTIFICATION_STRING_CHAT_BACKGROUND);
        intentBackGround.putExtra(Constant.KEY_CLIENT_MESSAGE, message);
        sendBroadcast(intentBackGround);
    }
}
