package com.siliconorchard.walkitalkiechat.asynctasks;

import android.os.AsyncTask;
import android.util.Log;

import com.siliconorchard.walkitalkiechat.discovery.Network.HostBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adminsiriconorchard on 4/20/16.
 */
public abstract class AbstractHostAsync extends AsyncTask<Void, HostBean, Void> {

    protected int hosts_done = 0;

    protected long ip;
    protected long start = 0;
    protected long end = 0;
    protected long size = 0;

    protected List<HostBean> mListHostBean;

    private OnPublishProgress mOnPublishProgress;
    private OnFinishExecution mOnFinishExecution;


    public void setNetwork(long ip, long start, long end) {
        this.ip = ip;
        this.start = start;
        this.end = end;
    }

    abstract protected Void doInBackground(Void... params);

    @Override
    protected void onPreExecute() {
        mListHostBean = new ArrayList<>();
        size = (int) (end - start + 1);
    }

    @Override
    protected void onProgressUpdate(HostBean... host) {
        if(host[0] != null) {
            mListHostBean.add(host[0]);
            if(mOnPublishProgress != null) {
                mOnPublishProgress.onPublishProgress(host[0]);
            }
        }
    }

    @Override
    protected void onPostExecute(Void unused) {
        Log.e("TAG_LOG", "Total host found: " + mListHostBean.size());
        if(mOnFinishExecution != null) {
            mOnFinishExecution.onFinishExecution();
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }


    public void setOnPublishProgress(OnPublishProgress onPublishProgress) {
        this.mOnPublishProgress = onPublishProgress;
    }

    public void setOnFinishExecution(OnFinishExecution onFinishExecution) {
        this.mOnFinishExecution = onFinishExecution;
    }
    public static interface OnPublishProgress {
        public abstract void onPublishProgress(HostBean hostBean);
    }
    public static interface OnFinishExecution {
        public abstract void onFinishExecution();
    }
}