package com.huangjiang;

import android.app.Application;
import android.content.Context;

import com.huangjiang.utils.XFileUtils;

import java.util.LinkedList;

public class XFileApplication extends Application {

    public static Context context;

    public static int broadcast_port;

    public static int message_port;

    public static int file_port;

    public static String device_id;

    public static int connect_type = 1;//0 未连接,1 客户端连接,2 服务端连接


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        device_id = XFileUtils.getDeviceId();
        LinkedList<String> linkedList=new LinkedList<>();

    }

}
