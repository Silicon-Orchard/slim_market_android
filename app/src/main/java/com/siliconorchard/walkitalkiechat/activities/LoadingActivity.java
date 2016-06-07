package com.siliconorchard.walkitalkiechat.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.R;

/**
 * Created by adminsiriconorchard on 6/7/16.
 */
public class LoadingActivity extends ActivityBase {

    private static final int PROGRESS = 10;

    private ProgressBar mProgress;

    private static int MAX_PROGRESS_BAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        mProgress = (ProgressBar) findViewById(R.id.loading_bar);

        showFakeProgress();

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
}
