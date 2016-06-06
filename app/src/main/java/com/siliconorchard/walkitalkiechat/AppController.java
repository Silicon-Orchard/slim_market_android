package com.siliconorchard.walkitalkiechat;

import android.app.Activity;
import android.app.Application;

import com.siliconorchard.walkitalkiechat.utilities.Constant;
import com.siliconorchard.walkitalkiechat.utilities.Utils;

/**
 * Created by adminsiriconorchard on 4/13/16.
 */
public class AppController extends Application {

    private static AppController mInstance;

    private Activity mCurrentActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        initApp();
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    private void initApp() {
        mInstance = this;
        Utils.findDeviceName(getSharedPreferences(Constant.SHARED_PREF_NAME, MODE_PRIVATE));
        if(!Utils.isServerServiceRunning(this, Constant.SERVER_SERVICE_NAME)) {
            Utils.startServerService(this);
        }
    }

    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }

}
