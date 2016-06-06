package com.siliconorchard.walkitalkiechat.runnable;

/**
 * Created by adminsiriconorchard on 4/11/16.
 */
public abstract class RunnableBase implements Runnable{
    private volatile boolean isRunThread;

    public RunnableBase(boolean runThread) {
        this.isRunThread = runThread;
    }

    public boolean isRunThread() {
        return isRunThread;
    }


    public void setIsRunThread(boolean isRunThread) {
        this.isRunThread = isRunThread;
    }

    public void terminate() {
        this.isRunThread = false;
    }
}
