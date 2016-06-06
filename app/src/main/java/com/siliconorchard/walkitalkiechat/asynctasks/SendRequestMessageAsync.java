package com.siliconorchard.walkitalkiechat.asynctasks;

import com.siliconorchard.walkitalkiechat.activities.DiscoveryActivityAbstract;

import java.lang.ref.WeakReference;
import java.net.DatagramSocket;

/**
 * Created by adminsiriconorchard on 4/25/16.
 */
public class SendRequestMessageAsync extends SendMessageAsync{

    private WeakReference<DiscoveryActivityAbstract> mDiscoveryWeak;

    public void setDiscoveryActivity(DiscoveryActivityAbstract discoveryActivity) {
        mDiscoveryWeak = new WeakReference<DiscoveryActivityAbstract>(discoveryActivity);
    }

    @Override
    protected void onPostExecute(Integer result) {
        if(mDiscoveryWeak != null) {
            final DiscoveryActivityAbstract discoveryActivity = mDiscoveryWeak.get();
            if(discoveryActivity != null) {
                discoveryActivity.increaseResponse();
            }
        }
    }
}
