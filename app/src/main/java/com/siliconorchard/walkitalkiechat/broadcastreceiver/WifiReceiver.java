package com.siliconorchard.walkitalkiechat.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.siliconorchard.walkitalkiechat.service.ServiceServer;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

/**
 * Created by adminsiriconorchard on 4/12/16.
 */
public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        ServiceServer.closeSocket();

        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            Log.d("WifiReceiver", "Have Wifi Connection");
            Utils.startServerService(context);
        } else{
            Log.d("WifiReceiver", "Don't have Wifi Connection");
        }
    }
}
