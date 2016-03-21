package com.huangjiang.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.huangjiang.manager.IMDeviceServerManager;
import com.huangjiang.manager.IMFileClientManager;
import com.huangjiang.manager.IMFileServerManager;
import com.huangjiang.manager.IMMessageClientManager;
import com.huangjiang.manager.IMMessageServerManager;

/**
 *
 */
public class IMService extends Service {

    private IMDeviceServerManager deviceServerMgr = IMDeviceServerManager.getInstance();
    private IMMessageServerManager messageServerManager = IMMessageServerManager.getInstance();
    private IMMessageClientManager messageClientManager = IMMessageClientManager.getInstance();
//    private IMFileServerManager fileServerManager = IMFileServerManager.getInstance();
//    private IMFileClientManager fileClientManager = IMFileClientManager.getInstance();


    /**
     * binder
     */
    private IMServiceBinder binder = new IMServiceBinder();

    public class IMServiceBinder extends Binder {
        public IMService getService() {
            return IMService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        deviceServerMgr.start();
        messageServerManager.start();
//        fileServerManager.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deviceServerMgr.stop();
        messageServerManager.stop();
        messageClientManager.stop();
//        fileServerManager.stop();
//        fileClientManager.stop();
    }
}
