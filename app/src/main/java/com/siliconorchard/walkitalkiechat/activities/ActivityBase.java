package com.siliconorchard.walkitalkiechat.activities;

import android.app.Activity;

import com.siliconorchard.walkitalkiechat.AppController;

/**
 * Created by adminsiriconorchard on 4/18/16.
 */
public class ActivityBase extends Activity {

    protected void onResume() {
        super.onResume();
        AppController.getInstance().setCurrentActivity(this);
    }
    protected void onPause() {
        clearReferences();
        super.onPause();
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
}
