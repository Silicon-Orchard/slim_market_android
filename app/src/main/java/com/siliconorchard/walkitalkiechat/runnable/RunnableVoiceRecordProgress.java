package com.siliconorchard.walkitalkiechat.runnable;

import android.widget.TextView;

import com.siliconorchard.walkitalkiechat.R;

/**
 * Created by adminsiriconorchard on 6/9/16.
 */
public class RunnableVoiceRecordProgress extends RunnableBase {

    private int[] backgroundDrawables = {
            R.drawable.image_record_time_line_1,
            R.drawable.image_record_time_line_2,
            R.drawable.image_record_time_line_3
    };
    private TextView mTvProgress;

    private int miliSec100Count;

    public RunnableVoiceRecordProgress(boolean runThread) {
        super(runThread);
    }

    public TextView getTvProgress() {
        return mTvProgress;
    }

    public void setTvProgress(TextView tvProgress) {
        this.mTvProgress = tvProgress;
    }

    @Override
    public void run() {
        try {
            miliSec100Count = 0;
            while(isRunThread()) {
                Thread.sleep(100);
                miliSec100Count+=100;
                mTvProgress.post(new Runnable() {
                    @Override
                    public void run() {
                        int imageIndex = miliSec100Count%3;
                        int sec = miliSec100Count / 1000;
                        int min = sec/60;
                        sec = sec%60;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(min);
                        stringBuilder.append(':');
                        if(sec<10) {
                            stringBuilder.append('0');
                        }
                        stringBuilder.append(sec);
                        mTvProgress.setText(stringBuilder.toString());
                        mTvProgress.setBackgroundResource(backgroundDrawables[imageIndex]);
                    }
                });
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
