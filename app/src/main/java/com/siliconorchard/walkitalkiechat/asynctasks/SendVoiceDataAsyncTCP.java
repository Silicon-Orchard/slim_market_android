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
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * Created by adminsiriconorchard on 5/6/16.
 */
public class SendVoiceDataAsyncTCP extends AsyncTask<FileMessage, Integer, Boolean> {

    private int totalSize;

    private OnProgressUpdate mOnProgressUpdate;
    private OnPreExecute mOnPreExecute;
    private OnPostExecute mOnPostExecute;

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
        boolean isSuccess = false;
        if(mFile == null || mHostClientList == null || mHostClientList.size() < 1) {
            return false;
        }
        try {

            FileInputStream fileinputstream = new FileInputStream(mFile);
            byte[] fileData = Utils.inputStreamToByteArray(fileinputstream);
            String wholeMessage = Base64.encodeToString(fileData, Base64.NO_WRAP);

            Log.e("TAG_LOG", "Sending Message\n" + wholeMessage);

            FileMessage fileMessage = params[0];
            fileMessage.setVoiceMessage(wholeMessage);
            String message = fileMessage.getJsonString();
            for(int i = 0; i<mHostClientList.size(); i++) {
                String ipAddress = mHostClientList.get(i).getIpAddress();
                if(!ipAddress.equals(myIpAddress)) {
                    if(sendVoiceMessage(ipAddress, message)) {
                        isSuccess = true;
                    }
                }
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
            isSuccess = false;
        }  catch (IOException e) {
            e.printStackTrace();
            isSuccess = false;
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        }

        return isSuccess;
    }

    private boolean sendVoiceMessage(String ipAddress, String message) {
        boolean retVal = false;
        try {
            /*Socket socket = new Socket(ipAddress,
                    Utils.getPortNumberByIpAddress(ipAddress));*/
            Socket socket = new Socket(ipAddress,
                    Constant.VOICE_SERVER_PORT);
            //Get the output stream of the client socket
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            //Write data to the output stream of the client socket
            out.println(message);
            socket.close();
            retVal = true;
        } catch (IOException e) {
            e.printStackTrace();
            retVal = false;
        }
        return retVal;
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