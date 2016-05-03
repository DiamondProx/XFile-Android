package com.huangjiang.activity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Toast;

import com.huangjiang.filetransfer.R;
import com.huangjiang.wfs.CopyUtil;
import com.huangjiang.wfs.WebService;

import java.lang.reflect.Method;

/**
 * 分享当前程序
 */
public class ShareWIFIActivity extends BaseActivity {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.zero_traffic_invite, R.layout.activity_share_wifi);
        init();
        startService();
    }

    void init() {
        //获取wifi管理服务
        new CopyUtil(this).assetsCopy();
    }

    void startService() {
        //如果是打开状态就关闭，如果是关闭就打开
        if (setWifiAp(true)) {
            intent = new Intent(this, WebService.class);
            startService(intent);
        }
    }

    void stopService() {
        if (intent != null) {
            setWifiAp(false);
            stopService(intent);
            intent = null;
        }
    }

    @Override
    protected void onDestroy() {
        stopService();
        super.onDestroy();
    }


    // wifi热点开关
    public boolean setWifiAp(boolean state) {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(!state);
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称
            apConfig.SSID = "DM-JoinMe";
            //配置热点的密码
            //apConfig.preSharedKey = "12122112";
            //设置加密方式
            //apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, state);
        } catch (Exception e) {
            return false;
        }
    }


}
