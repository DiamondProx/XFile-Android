package com.huangjiang.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.huangjiang.manager.IMDeviceServerManager;

/**
 *
 */
public class IMService extends Service {

    private IMDeviceServerManager deviceServerMgr = IMDeviceServerManager.getInstance();


    /**binder*/
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
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deviceServerMgr.stop();
    }
}
