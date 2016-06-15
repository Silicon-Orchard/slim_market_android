package com.siliconorchard.walkitalkiechat.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.siliconorchard.walkitalkiechat.R;
import com.siliconorchard.walkitalkiechat.model.ChatMessage;
import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

import org.json.JSONException;

/**
 * Created by adminsiriconorchard on 6/7/16.
 */
public class LoadingActivity extends ActivityBase {

    private static final int PROGRESS = 10;

    private ProgressBar mProgress;

    private static int MAX_PROGRESS_BAR;


    private AlertDialog mAlertDialog;

    private String ipAddress;
    private SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        mSharedPref = getSharedPreferences(Constant.SHARED_PREF_NAME, MODE_PRIVATE);
        ipAddress = Utils.getDeviceIpAddress();
        mProgress = (ProgressBar) findViewById(R.id.loading_bar);

    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isPermissionRequested = Utils.findDeviceID(this, mSharedPref);
        if(ipAddress == null || ipAddress.length()<5) {
            showWifiNotEnabledDialog();
        } else {
            if(!isPermissionRequested) {
                sendBroadcastRequestInfo();
                showFakeProgress();
            }
        }
    }

    private void showFakeProgress() {
        MAX_PROGRESS_BAR = PROGRESS * PROGRESS;
        mProgress.setMax(MAX_PROGRESS_BAR);
        new AsyncTask<Void, Integer, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for(int i = 0; i<=MAX_PROGRESS_BAR; i++) {
                    try {
                        Thread.sleep(20L);
                        publishProgress(i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                int progress = values[0];
                mProgress.setProgress(progress);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Intent main_intent = new Intent(LoadingActivity.this, MainActivity.class);
                startActivity(main_intent);
            }
        }.execute();
    }



    private void sendBroadcastRequestInfo() {
        if(Utils.isInfoRequestSent()) {
            return;
        }
        try {
            ChatMessage chatMessage = generateChatMessageBasics();
            chatMessage.setType(ChatMessage.TYPE_REQUEST_INFO);
            Utils.sendBroadCastMessage(chatMessage);
            Utils.setIsInfoRequestSent(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constant.READ_PHONE_STATE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Utils.getDeviceIdFromTelephonyManager(this, mSharedPref);

                } else {
                    Utils.setDeviceId(Constant.DEVICE_ID_UNKNOWN);
                    mSharedPref.edit().putString(Constant.KEY_DEVICE_ID, Constant.DEVICE_ID_UNKNOWN).commit();
                }
                sendBroadcastRequestInfo();
                showFakeProgress();
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showWifiNotEnabledDialog() {
        AlertDialog.Builder  builder = Utils.createAlertDialog(this, R.string.wifi_not_enabled,
                R.string.error_wifi_not_enabled_please_enable);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                    LoadingActivity.this.finish();
                }
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.show();
        mAlertDialog.getWindow().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.shape_voice_activity_bg));
    }

    private ChatMessage generateChatMessageBasics() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setDeviceId(Utils.getDeviceId(this, mSharedPref));
        chatMessage.setIpAddress(ipAddress);
        chatMessage.setDeviceName(Utils.getDeviceName(mSharedPref));
        return chatMessage;
    }
}
