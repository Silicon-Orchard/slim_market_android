package com.siliconorchard.walkitalkiechat.model;

import android.util.Base64;
import android.util.Log;

import com.siliconorchard.walkitalkiechat.runnable.RunnableReceiveFileTCP;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by adminsiriconorchard on 8/18/16.
 */
public class ReceiveFileTCP {

    private int numOfReceivedMsg = 0;
    private boolean isBusy;
    private boolean isFirst;

    private File mFile;
    private FileOutputStream fileOutputStream;
    private int chatPosition;

    private RunnableReceiveFileTCP.OnReceiveCallBacks mOnReceiveCallBacks;


    public ReceiveFileTCP(RunnableReceiveFileTCP.OnReceiveCallBacks onReceiveCallBacks) {
        isFirst = true;
        this.mOnReceiveCallBacks = onReceiveCallBacks;
    }

    public boolean isBusy() {
        return this.isBusy;
    }

    public boolean processFileMessage(FileMessage fileMessage) {
        boolean isFinished = false;
        isBusy = true;
        try {
            if(numOfReceivedMsg == 0 && mOnReceiveCallBacks != null) {
                chatPosition = mOnReceiveCallBacks.onPreReceive(fileMessage);
            }
            numOfReceivedMsg++;
            final boolean isContinue = fileMessage.getCurrentChunkNo()< fileMessage.getTotalChunkCount();
            Log.e("TAG_LOG", "Current/Total, isContinue: " + fileMessage.getCurrentChunkNo() + "/" + fileMessage.getTotalChunkCount() + "," + isContinue);
            if(mOnReceiveCallBacks != null && isContinue) {
                mOnReceiveCallBacks.onProgressUpdate(fileMessage, chatPosition);
            }

            if(isContinue) {
                writeToFile(fileMessage, false);
            } else {
                writeToFile(fileMessage, true);
                isFinished = true;
            }
            if(isFirst) {
                isFirst = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            onError(e.toString());
        }
        isBusy = false;
        return isFinished;
    }

    private void writeToFile(FileMessage fileMessage, boolean isFinish) throws IOException {
        if(isFinish && numOfReceivedMsg != fileMessage.getTotalChunkCount()) {
            String errorMessage = "Data missing occurs,(Received/Sent) "+numOfReceivedMsg+"/"+ fileMessage.getTotalChunkCount();
            onError(errorMessage);
            fileOutputStream.flush();
            fileOutputStream.close();
            mFile.delete();
            return;
        }

        if(isFirst) {
            createFile(fileMessage);
            fileOutputStream = new FileOutputStream(mFile.getAbsolutePath());
        }

        if(mFile == null) {
            onError("File cannot be created");
            return;
        }
        byte[] filePart = Base64.decode(fileMessage.getVoiceMessage(), Base64.NO_WRAP);
        fileOutputStream.write(filePart);
        if(isFinish) {
            fileOutputStream.flush();
            fileOutputStream.close();
            if(mOnReceiveCallBacks != null) {
                mOnReceiveCallBacks.onPostReceive(fileMessage, mFile, chatPosition);
            }
            numOfReceivedMsg = 0;
        }
    }


    private void createFile(FileMessage fileMessage) {
        String path;
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
        mFile = Utils.createFile(path, fileMessage.getFileName());
    }

    private void onError(String errorMessage) {
        Log.e("TAG_LOG", errorMessage);
        if(mOnReceiveCallBacks != null) {
            mOnReceiveCallBacks.onErrorOccur(errorMessage, chatPosition);
        }
        numOfReceivedMsg = 0;
    }
}
