package com.siliconorchard.walkitalkiechat.asynctasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.model.ChatMessage;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by adminsiriconorchard on 4/12/16.
 */
public class ServerAsyncTask extends AsyncTask<Object, Void, String> {
    private TextView mTvClientMsg;
    //Background task which serve for the client
    @Override
    protected String doInBackground(Object... params) {
        String result = null;
        //Get the accepted socket object
        Socket mySocket = (Socket) params[0];
        mTvClientMsg = (TextView) params[1];
        try {
            Log.e("TAG_LOG","Starting message receiving");
            InputStream is = mySocket.getInputStream();
            //Buffer the data input stream
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(is));
            //Read the contents of the data buffer
            result = br.readLine();
            //Close the client connection
            mySocket.close();
            Log.e("TAG_LOG", "Ending message receiving");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String message) {
        //After finishing the execution of background task data will be write the text view
        if(message != null) {
            Log.e("TAG_LOG", "Message Received: " + message);
            try {
                ChatMessage chatMessage = new ChatMessage(message);
                mTvClientMsg.append("\n"+chatMessage.getDeviceName()+": " + chatMessage.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("TAG_LOG","Null Message Received");
        }
    }
}
