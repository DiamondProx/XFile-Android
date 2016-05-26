package com.huangjiang.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.huangjiang.business.event.WifiEvent;
import com.huangjiang.config.Config;
import com.huangjiang.service.IMService;
import com.huangjiang.utils.Logger;

import org.greenrobot.eventbus.EventBus;

/**
 * 网络变化
 */
public class NetWorkReceiver extends BroadcastReceiver {

    Logger logger = Logger.getLogger(NetWorkReceiver.class);
    private static boolean isWifiAvailable = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        logger.e("****NetworkChange");
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (!isWifiAvailable && wifiNetInfo != null && wifiNetInfo.isConnected()) {
            isWifiAvailable = true;
            logger.e("****isWifiAvailable11:" + isWifiAvailable);
            context.stopService(new Intent(context, IMService.class));
            context.startService(new Intent(context, IMService.class));
            logger.e("****RestartServer");
            if (Config.is_ap) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        WifiEvent wifiEvent = new WifiEvent();
                        wifiEvent.setConnected(true);
                        EventBus.getDefault().post(wifiEvent);
                    }
                }, 500);
            }
        } else {
            isWifiAvailable = false;
            logger.e("****isWifiAvailable22:" + isWifiAvailable);
        }
    }
}
