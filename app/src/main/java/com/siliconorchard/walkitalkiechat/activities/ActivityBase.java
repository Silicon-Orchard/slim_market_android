package com.siliconorchard.walkitalkiechat.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.siliconorchard.walkitalkiechat.AppController;
import com.siliconorchard.walkitalkiechat.dialog.DialogChatRequest;
import com.siliconorchard.walkitalkiechat.model.HostInfo;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

/**
 * Created by adminsiriconorchard on 4/18/16.
 */
public class ActivityBase extends Activity {

    protected void onResume() {
        super.onResume();
        AppController.getInstance().setCurrentActivity(this);
        registerReceiver(receiverChatReq, new IntentFilter(Constant.RECEIVER_NOTIFICATION_CHAT_REQUEST));
    }
    protected void onPause() {
        clearReferences();
        super.onPause();
        unregisterReceiver(receiverChatReq);
    }
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences(){
        Activity currActivity = AppController.getInstance().getCurrentActivity();
        if (this.equals(currActivity)) {
            AppController.getInstance().setCurrentActivity(null);
        }
    }

    private void showChatRequestDialog(Bundle bundle) {
        HostInfo hostInfo = (HostInfo) bundle.getParcelable(Constant.KEY_HOST_INFO);
        DialogChatRequest dialogChatRequest = new DialogChatRequest();
        Bundle args = new Bundle();
        args.putParcelable(Constant.KEY_HOST_INFO, hostInfo);
        args.putString(Constant.KEY_MY_IP_ADDRESS, Utils.getDeviceIpAddress());
        dialogChatRequest.setArguments(args);
        dialogChatRequest.show(getFragmentManager(),"Test");
    }

    private BroadcastReceiver receiverChatReq = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                showChatRequestDialog(bundle);
            }
        }
    };
}
