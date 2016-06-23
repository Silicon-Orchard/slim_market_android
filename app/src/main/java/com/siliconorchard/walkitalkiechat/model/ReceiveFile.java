package com.siliconorchard.walkitalkiechat.model;

import android.util.Base64;
import android.util.Log;

import com.siliconorchard.walkitalkiechat.runnable.RunnableReceiveFile;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by adminsiriconorchard on 6/23/16.
 */
public class ReceiveFile {

    private String receivedDataString = null;
    private int numOfReceivedMsg = 0;
    private boolean isBusy;

    public boolean isBusy() {
        return this.isBusy;
    }

    public boolean processFileMessage(RunnableReceiveFile.OnReceiveCallBacks mOnReceiveCallBacks, FileMessage fileMessage) {
        boolean isFinished = false;
        isBusy = true;
        try {
            if(numOfReceivedMsg == 0 && mOnReceiveCallBacks != null) {
                mOnReceiveCallBacks.onPreReceive(fileMessage);
            }
            numOfReceivedMsg++;
            if(receivedDataString == null) {
                String nullText = "";
                receivedDataString = Base64.encodeToString(nullText.getBytes(), Base64.NO_WRAP);
            }
            final boolean isContinue = fileMessage.getCurrentChunkNo()< fileMessage.getTotalChunkCount();
            Log.e("TAG_LOG","Current/Total, isContinue: "+ fileMessage.getCurrentChunkNo()+"/"+ fileMessage.getTotalChunkCount()+","+isContinue);
            if(mOnReceiveCallBacks != null && isContinue) {
                mOnReceiveCallBacks.onProgressUpdate(fileMessage);
            }

            if(isContinue) {
                receivedDataString = receivedDataString + fileMessage.getVoiceMessage();
            } else {
                fileReceivingFinish(mOnReceiveCallBacks, fileMessage);
                isFinished = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            onError(mOnReceiveCallBacks, e.toString());
        }
        isBusy = false;
        return isFinished;
    }

    private void fileReceivingFinish(RunnableReceiveFile.OnReceiveCallBacks mOnReceiveCallBacks, FileMessage fileMessage) throws IOException{
        receivedDataString = receivedDataString + fileMessage.getVoiceMessage();
        if(numOfReceivedMsg != fileMessage.getTotalChunkCount()) {
            String errorMessage = "Data missing occurs,(Received/Sent) "+numOfReceivedMsg+"/"+ fileMessage.getTotalChunkCount();
            onError(mOnReceiveCallBacks, errorMessage);
            return;
        }
        String path = null;
        switch (fileMessage.getFileType()) {
            case Constant.FILE_TYPE_AUDIO:
                path = Constant.BASE_PATH+Constant.FOLDER_NAME_AUDIO;
                break;
            case Constant.FILE_TYPE_VIDEO:
                path = Constant.BASE_PATH+Constant.FOLDER_NAME_VIDEO;
                break;
            case Constant.FILE_TYPE_PHOTO:
                path = Constant.BASE_PATH+Constant.FOLDER_NAME_PHOTO;
                break;
            default:
                path = Constant.BASE_PATH+Constant.FOLDER_NAME_OTHER;
                break;
        }
        File file = Utils.createFile(path, fileMessage.getFileName());
        if(file == null) {
            onError(mOnReceiveCallBacks, "File cannot be created");
            return;
        }
        byte[] filePart = Base64.decode(receivedDataString, Base64.NO_WRAP);
        FileOutputStream out = new FileOutputStream(file.getAbsolutePath());
        out.write(filePart);
        out.flush();
        out.close();
        if(mOnReceiveCallBacks != null) {
            mOnReceiveCallBacks.onPostReceive(fileMessage, file);
        }
        receivedDataString = null;
        numOfReceivedMsg = 0;
    }


    private void onError(RunnableReceiveFile.OnReceiveCallBacks mOnReceiveCallBacks, String errorMessage) {
        Log.e("TAG_LOG", errorMessage);
        if(mOnReceiveCallBacks != null) {
            mOnReceiveCallBacks.onErrorOccur(errorMessage);
        }
        receivedDataString = null;
        numOfReceivedMsg = 0;
    }
}
