package com.siliconorchard.walkitalkiechat.utilities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.siliconorchard.walkitalkiechat.AppController;
import com.siliconorchard.walkitalkiechat.asynctasks.SendMessageAsync;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.service.ServiceServer;
import com.siliconorchard.walkitalkiechat.singleton.GlobalDataHolder;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by adminsiriconorchard on 4/13/16.
 */
public class Utils {
    private static String DEVICE_NAME;
    private static String DEVICE_ID;
    private static boolean isInfoRequestSent;

    public static boolean findDeviceID(Activity activity, SharedPreferences sharedPreferences) {
        boolean isPermissionRequested = false;
        DEVICE_ID = sharedPreferences.getString(Constant.KEY_DEVICE_ID, null);
        if(DEVICE_ID == null) {
            try {
                getDeviceIdFromTelephonyManager(activity, sharedPreferences);
            } catch (SecurityException e) {
                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.READ_PHONE_STATE)) {

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.READ_PHONE_STATE},
                                Constant.READ_PHONE_STATE_PERMISSION);

                        isPermissionRequested = true;
                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }
            }
        }

        return isPermissionRequested;
    }

    public static void findDeviceName(SharedPreferences sharedPreferences) {
        DEVICE_NAME = sharedPreferences.getString(Constant.KEY_MY_DEVICE_NAME, null);
        if(DEVICE_NAME == null) {
            DEVICE_NAME = android.os.Build.MANUFACTURER + android.os.Build.PRODUCT + android.os.Build.MODEL;
            sharedPreferences.edit().putString(Constant.KEY_MY_DEVICE_NAME, DEVICE_NAME).commit();
        }
    }

    public static String getDeviceName(SharedPreferences sharedPreferences) {
        if(DEVICE_NAME == null) {
            findDeviceName(sharedPreferences);
        }
        return DEVICE_NAME;
    }

    public static void setDeviceName(SharedPreferences sharedPreferences, String deviceName) {
        if(deviceName == null) {
            return;
        }
        DEVICE_NAME = deviceName;
        sharedPreferences.edit().putString(Constant.KEY_MY_DEVICE_NAME, deviceName).commit();
    }

    public static String getDeviceId(Activity activity, SharedPreferences sharedPreferences) {
        if(DEVICE_ID == null) {
            findDeviceID(activity, sharedPreferences);
        }
        return DEVICE_ID;
    }


    /**
     * Get ip address of the device
     */
    public static String getDeviceIpAddress() {
        String ipAddress = null;
        try {
            //Loop through all the network interface devices
            for (Enumeration<NetworkInterface> enumeration = NetworkInterface
                    .getNetworkInterfaces(); enumeration.hasMoreElements(); ) {
                NetworkInterface networkInterface = enumeration.nextElement();
                //Loop through all the ip addresses of the network interface devices
                for (Enumeration<InetAddress> enumerationIpAddr = networkInterface.getInetAddresses(); enumerationIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumerationIpAddr.nextElement();
                    //Filter out loopback address and other irrelevant ip addresses
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
                        //Print the device ip address in to the text view
                        ipAddress = inetAddress.getHostAddress();

                    }
                }
            }
        } catch (SocketException e) {
            Log.e("ERROR:", e.toString());
        }
        return ipAddress;
    }

    public static boolean isServerServiceRunning(Context context, String serviceName) {
        boolean isRunning = false;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getPackageName())) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    public static void startServerService(Context context) {
        String ipAddress = getDeviceIpAddress();
        if(ipAddress == null) {
            Log.e("TAG_LOG", "Ip address not found");
            return;
        }
        Intent serviceIntent = new Intent(context, ServiceServer.class);
        int serverPort = Constant.FIRST_SERVER_PORT;
        serviceIntent.putExtra(Constant.KEY_MY_IP_ADDRESS, ipAddress);
        context.startService(serviceIntent);
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.SHARED_PREF_NAME, Activity.MODE_PRIVATE);
        sharedPreferences.edit().putString(Constant.KEY_MY_IP_ADDRESS, ipAddress).commit();
    }

    public static void getDeviceIdFromTelephonyManager(Activity activity, SharedPreferences sharedPreferences) {
        TelephonyManager telephonyManager = (TelephonyManager)activity.getSystemService(Context.TELEPHONY_SERVICE);
        DEVICE_ID = telephonyManager.getDeviceId();
        sharedPreferences.edit().putString(Constant.KEY_DEVICE_ID, DEVICE_ID).commit();
    }

    public static void setDeviceId(String deviceId) {
        DEVICE_ID = deviceId;
    }

    public static void sendBroadCastMessage(ChatMessage chatMessage) throws JSONException{
        String ipAddress = chatMessage.getIpAddress();
        String ipAddressBase = ipAddress.substring(0, ipAddress.lastIndexOf('.') + 1);
        String message = chatMessage.getJsonString();
        for(int i = 2; i<255; i++) {
            String clientAddress = ipAddressBase+i;
            if(!ipAddress.equals(clientAddress)) {
                HostInfo hostInfo = new HostInfo();
                hostInfo.setIpAddress(clientAddress);
                SendMessageAsync sendMessageAsync = new SendMessageAsync();
                sendMessageAsync.execute(hostInfo, message);
            }
        }
    }

    public static void sendBroadCastMessageToRegisteredClients(ChatMessage chatMessage) throws JSONException{
        String message = chatMessage.getJsonString();
        List<HostInfo> list = GlobalDataHolder.getInstance().getListHostInfo();
        if(list == null || list.size() < 1) {
            return;
        }
        for(int i = 0; i<list.size(); i++) {
            HostInfo hostInfo = list.get(i);
            SendMessageAsync sendMessageAsync = new SendMessageAsync();
            sendMessageAsync.execute(hostInfo, message);
        }
    }

    public static HostInfo getHostInfoFromChatMessage(ChatMessage chatMessage) {
        HostInfo hostInfo = new HostInfo();
        hostInfo.setIpAddress(chatMessage.getIpAddress());
        hostInfo.setDeviceId(chatMessage.getDeviceId());
        hostInfo.setDeviceName(chatMessage.getDeviceName());
        return hostInfo;
    }

    public static boolean isInfoRequestSent() {
        return isInfoRequestSent;
    }

    public static void setIsInfoRequestSent(boolean isInfoRequestSent) {
        Utils.isInfoRequestSent = isInfoRequestSent;
    }

    public static File createFile(String folderPath, String fileName) {
        final File dir = new File(AppController.getInstance().getFilesDir() + "/WalkiTalkie");
        dir.mkdirs();
        folderPath = dir.getAbsolutePath();

        if(fileName == null) {
            fileName = Constant.FILE_NAME;
        }
        File mFile = null;
        try {
            File folder = new File(folderPath);
            Log.e("TAG_LOG","Folder Path: "+folderPath);
            if(!folder.exists()) {
                if(folder.mkdirs()) {
                    Log.e("TGA_LOG","Directory created");
                } else {
                    Log.e("TGA_LOG", "Directory not created");
                }
            }
            mFile = new File(folderPath, fileName);
            if(!mFile.exists()) {
                mFile.createNewFile();
            }
        } catch (IOException e) {
            mFile = null;
            e.printStackTrace();
        }
        return mFile;
    }

    public static byte[] inputStreamToByteArray(FileInputStream inStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inStream.read(buffer)) > 0) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    public static int getPortNumberByIpAddress(String ipAddress) {
        int port = 0;
        try {
            if(ipAddress!=null) {
                port = Constant.FIRST_SERVER_PORT + Integer.parseInt(ipAddress.substring(ipAddress.lastIndexOf('.') + 1));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return port;
    }

    public static byte[] shortToBytes(short s) {
        return new byte[]{(byte)(s & 0x00FF),(byte)((s & 0xFF00)>>8)};
    }

    public static byte[] shortArrayToByteArray(short[] s) {
        byte[] byteArray = new byte[s.length * 2];
        for(int i = 0; i<s.length; i++) {
            byte[] bytes = shortToBytes(s[i]);
            byteArray[i*2] = bytes[0];
            byteArray[i*2 + 1] = bytes[1];
        }
        return byteArray;
    }

    public static short bytesToShort(byte low, byte high) {
        return (short) (low+(high<<8));
    }

    public static short[] byteArrayToShortArray(byte[] byteArray, int length) {
        short[] shortArray = new short[length/2];
        for(int i = 0; i<length; i+=2) {
            shortArray[i/2] = bytesToShort(byteArray[i], byteArray[i+1]);
        }
        return shortArray;
    }

    private static int[] mSampleRates = new int[] { 8000, 11025, 16000, 22050, 44100 };
    public static AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        Log.e("TAG_LOG", "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
                                + channelConfig);
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        Log.e("TAG_LOG", rate + "Exception, keep trying.",e);
                    }
                }
            }
        }
        return null;
    }

}
