package com.huangjiang.utils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.huangjiang.XFileApp;

import java.lang.reflect.Method;

/**
 * WIFI热点帮助类
 */
public class WifiHelper {


    /**
     * wifi热点开关
     */
    public static boolean setWifiAp(boolean state, String... sid) {
        WifiManager wifiManager = (WifiManager) XFileApp.context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(!state);
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称(XFILE-机子型号)
            apConfig.SSID = ((sid != null && sid.length > 0) ? sid[0] : "XFile-" + android.os.Build.MODEL);
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, state);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 读取当前连接w
     */
    public static String getConnectWifiSsid() {
        WifiManager wifiManager = (WifiManager) XFileApp.context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo != null ? wifiInfo.getSSID() : "";

    }
}
