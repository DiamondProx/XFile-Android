package com.huangjiang.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.huangjiang.config.Config;
import com.huangjiang.service.IMService;
import com.huangjiang.utils.Logger;

/**
 * 网络变化
 */
public class NetWorkReceiver extends BroadcastReceiver {

    Logger logger = Logger.getLogger(NetWorkReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        logger.e("****NetworkChange");
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (Config.is_ap || (wifiNetInfo != null && wifiNetInfo.isConnected())) {
            context.stopService(new Intent(context, IMService.class));
            context.startService(new Intent(context, IMService.class));
            logger.e("****ResetServer");
        }
    }
}
