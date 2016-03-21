package com.huangjiang;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.huangjiang.service.IMService;

public class XFileApplication extends Application {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        startService(new Intent(this, IMService.class));
    }

}
