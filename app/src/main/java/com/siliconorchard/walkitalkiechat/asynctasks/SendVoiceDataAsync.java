package com.siliconorchard.walkitalkiechat.asynctasks;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.model.FileMessage;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

/**
 * Created by adminsiriconorchard on 5/6/16.
 */
public class SendVoiceDataAsync extends AsyncTask<FileMessage, Integer, Boolean> {

    private int totalSize;

    private OnProgressUpdate mOnProgressUpdate;
    private OnPreExecute mOnPreExecute;
    private OnPostExecute mOnPostExecute;
    private static final int FILE_PACKET_LENGTH = 16384;

    private File mFile;
    private List<HostInfo> mHostClientList;

    private String myIpAddress;

    public String getMyIpAddress() {
        return myIpAddress;
    }

    public void setMyIpAddress(String myIpAddress) {
        this.myIpAddress = myIpAddress;
    }

    public OnProgressUpdate getOnProgressUpdate() {
        return mOnProgressUpdate;
    }

    public void setOnProgressUpdate(OnProgressUpdate onProgressUpdate) {
        this.mOnProgressUpdate = onProgressUpdate;
    }

    public OnPreExecute getOnPreExecute() {
        return mOnPreExecute;
    }

    public void setOnPreExecute(OnPreExecute onPreExecute) {
        this.mOnPreExecute = onPreExecute;
    }

    public OnPostExecute getOnPostExecute() {
        return mOnPostExecute;
    }

    public void setOnPostExecute(OnPostExecute onPostExecute) {
        this.mOnPostExecute = onPostExecute;
    }

    public List<HostInfo> getClientIPAddressList() {
        return mHostClientList;
    }

    public void setClientIPAddressList(List<HostInfo> hostList) {
        this.mHostClientList = hostList;
    }

    public File getFile() {
        return mFile;
    }

    public void setFile(File file) {
        this.mFile = file;
    }

    @Override
    protected void onPreExecute() {
        if(mOnPreExecute != null) {
            mOnPreExecute.onPreExecute();
        }
    }

    @Override
    protected Boolean doInBackground(FileMessage... params) {
        if(mFile == null || mHostClientList == null || mHostClientList.size() < 1) {
            return false;
        }
        try {

            FileInputStream fileinputstream = new FileInputStream(mFile);
            byte[] fileData = Utils.inputStreamToByteArray(fileinputstream);
            String wholeMessage = Base64.encodeToString(fileData, Base64.NO_WRAP);

            Log.e("TAG_LOG", "Sending Message\n" + wholeMessage);

            FileMessage fileMessage = params[0];

            totalSize = wholeMessage.length();
            Log.e("TAG_LOG", "Total size: " + totalSize);
            int numOfMessages = totalSize/FILE_PACKET_LENGTH;
            if(totalSize % FILE_PACKET_LENGTH != 0) {
                numOfMessages++;
            }
            fileMessage.setTotalChunkCount(numOfMessages);
            fileMessage.setFileType(fileMessage.getFileType());

            int sentSize = 0;
            int currentMessage = 0;
            while(true) {
                boolean isContinue = true;
                int prevIndex = sentSize;
                sentSize += FILE_PACKET_LENGTH;
                if(sentSize>=totalSize) {
                    sentSize = totalSize;
                    isContinue = false;
                }
                String filePart = wholeMessage.substring(prevIndex, sentSize);
                fileMessage.setCurrentChunkNo(++currentMessage);
                fileMessage.setVoiceMessage(filePart);
                String message = fileMessage.getJsonString();



                for(int i = 0; i<mHostClientList.size(); i++) {
                    if(!mHostClientList.get(i).getIpAddress().equals(myIpAddress)) {
                        byte[] buffer1 = message.getBytes();
                        InetAddress receiverAddress = InetAddress.getByName(mHostClientList.get(i).getIpAddress());
                        DatagramPacket packet = new DatagramPacket(
                                buffer1, buffer1.length, receiverAddress, Constant.VOICE_SERVER_PORT);

                        DatagramSocket datagramSocket = new DatagramSocket();
                        datagramSocket.send(packet);
                    }
                }
                publishProgress(sentSize);
                if(!isContinue) {
                    //saveFile(receivedDataString);
                    break;
                }
                Thread.sleep(100);
            }
            return true;

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int sentSize = values[0];
        int progress = sentSize*100/totalSize;
        if(mOnProgressUpdate != null) {
            mOnProgressUpdate.onProgressUpdate(progress);
        }
    }

    @Override
    protected void onPostExecute(Boolean retVal) {
        if(mOnPostExecute != null) {
            mOnPostExecute.onPostExecute(retVal);
        }
    }

    public static interface OnPreExecute {
        public abstract void onPreExecute();
    }
    public static interface OnProgressUpdate {
        public abstract void onProgressUpdate(int progress);
    }
    public static interface OnPostExecute {
        public abstract void onPostExecute(boolean isExecuted);
    }
}