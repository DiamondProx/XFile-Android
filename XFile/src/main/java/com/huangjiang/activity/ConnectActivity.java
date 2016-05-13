package com.huangjiang.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.huangjiang.XFileApp;
import com.huangjiang.adapter.ScanAdapter;
import com.huangjiang.business.event.WIFIEvent;
import com.huangjiang.business.model.ScanInfo;
import com.huangjiang.config.Config;
import com.huangjiang.config.SysConstant;
import com.huangjiang.core.ThreadPoolManager;
import com.huangjiang.manager.IMClientMessageManager;
import com.huangjiang.manager.IMDeviceServerManager;
import com.huangjiang.manager.event.ClientFileSocketEvent;
import com.huangjiang.manager.event.ServerFileSocketEvent;
import com.huangjiang.utils.MobileDataUtils;
import com.huangjiang.utils.StringUtils;
import com.huangjiang.utils.WifiHelper;
import com.huangjiang.xfile.R;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private final String mPageName = "ConnectActivity";
    private LinearLayout guide_layout, scan_layout, failed_layout, device_layout, link_layout;
    private TextView link_hint1, link_hint2;
    private String connecting, connect_success, creating_connect, create_connect, let_friend_join;
    private ScanAdapter scanAdapter;
    private AnimationDrawable animationDrawable;
    private WifiManager wifiManager;
    private WifiReceiver wifiReceiver;
    private boolean isLinkHotspot = false;
    private ScanInfo linkInfo = null;
    private Timer timer = null;
    private TimerTask timerTask = null;
    private int ScanWhat = 1, HotspotWhat = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        init();
    }

    void init() {

        findViewById(R.id.guide_close).setOnClickListener(this);
        findViewById(R.id.failed_refresh).setOnClickListener(this);
        findViewById(R.id.device_refresh).setOnClickListener(this);
        findViewById(R.id.search_join).setOnClickListener(this);
        findViewById(R.id.scan_cancel).setOnClickListener(this);
        findViewById(R.id.failed_back).setOnClickListener(this);
        findViewById(R.id.device_back).setOnClickListener(this);
        findViewById(R.id.link_cancel).setOnClickListener(this);
        findViewById(R.id.create_ap).setOnClickListener(this);

        guide_layout = (LinearLayout) findViewById(R.id.guide_layout);
        scan_layout = (LinearLayout) findViewById(R.id.scan_layout);
        failed_layout = (LinearLayout) findViewById(R.id.failed_layout);
        device_layout = (LinearLayout) findViewById(R.id.device_layout);
        link_layout = (LinearLayout) findViewById(R.id.link_layout);

        link_hint1 = (TextView) findViewById(R.id.link_hint1);
        link_hint2 = (TextView) findViewById(R.id.link_hint2);

        connecting = getString(R.string.connecting);
        connect_success = getString(R.string.connect_success);
        create_connect = getString(R.string.create_connect);
        let_friend_join = getString(R.string.let_friend_join);
        creating_connect = getString(R.string.creating_connect);

        ListView lv_device = (ListView) findViewById(R.id.lv_device);
        scanAdapter = new ScanAdapter(ConnectActivity.this);
        lv_device.setAdapter(scanAdapter);
        lv_device.setOnItemClickListener(this);
        ImageView iv_connecting = (ImageView) findViewById(R.id.iv_linking);
        iv_connecting.setImageResource(R.drawable.progress_connect);
        animationDrawable = (AnimationDrawable) iv_connecting.getDrawable();

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        EventBus.getDefault().register(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.guide_close:
                XFileActivityManager.create().finishActivity();
                break;
            case R.id.create_ap:
                createHotspot();
                break;
            case R.id.search_join:
            case R.id.failed_refresh:
            case R.id.device_refresh:
                scanDevice();
                break;
            case R.id.scan_cancel:
                scanCancel();
                break;
            case R.id.failed_back:
            case R.id.device_back:
                showGuideView();
                break;
            case R.id.link_cancel:
                linkCancel();
                break;
        }
    }


    /**
     * 创建热点
     */
    void createHotspot() {
        if (WifiHelper.setWifiAp(true)) {
            link_hint1.setText(creating_connect);
            link_hint2.setText(String.format(let_friend_join, android.os.Build.MODEL));
            setLayoutVisibility(link_layout);
            animationDrawable.start();
            Config.is_ap = true;
            if (!Config.getMobileData()) {
                MobileDataUtils.setMobileData(this, false);
            } else {
                MobileDataUtils.setMobileData(this, true);
            }
            startHotspotTimer();
        } else {
            Toast.makeText(ConnectActivity.this, R.string.create_hotspot_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 扫描设备
     */
    void scanDevice() {
        IMDeviceServerManager.getInstance().startBonjour();
        ScanHandler.postDelayed(scanRunnable, 3200);
        scanAdapter.clear();
        setLayoutVisibility(scan_layout);
        registerWIFIReceiver();
        wifiManager.startScan();
    }

    /**
     * 显示引导界面
     */
    void showGuideView() {
        setLayoutVisibility(guide_layout);
        scanAdapter.clear();
        scanAdapter.notifyDataSetChanged();
    }

    /**
     * 连接设备
     */
    void connectToDevice(ScanInfo scanInfo) {
        showLinking();
        IMClientMessageManager messageClientManager = IMClientMessageManager.getInstance();
        messageClientManager.setHost(scanInfo.getIp());
        messageClientManager.setPort(scanInfo.getMsgPort());
        System.out.println("****Host:" + scanInfo.getIp());
        System.out.println("****Port:" + scanInfo.getMsgPort());
        messageClientManager.start();
    }

    /**
     * 连接热点
     */
    public void connectToHotpot(final ScanInfo scanInfo) {
        showLinking();
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\"" + scanInfo.getName() + "\"";
        wifiConfig.hiddenSSID = true;
        wifiConfig.status = WifiConfiguration.Status.ENABLED;
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        int wcgID = wifiManager.addNetwork(wifiConfig);
        boolean flag = wifiManager.enableNetwork(wcgID, true);
        System.out.println("****flag:" + flag);
        if (flag) {
            isLinkHotspot = true;
            linkInfo = scanInfo;
            Config.is_ap = true;
        }
    }


    void linkCancel() {
        setLayoutVisibility(guide_layout);
        animationDrawable.stop();
        if (WifiHelper.setWifiAp(false)) {
            Config.is_ap = false;
        }
        cancelHotspotTimer();
    }


    void scanCancel() {
        setLayoutVisibility(guide_layout);
        ScanHandler.removeCallbacks(scanRunnable);
        IMDeviceServerManager.getInstance().cancelBonjour();
        unregisterWIFIReceiver();
    }

    void showLinking() {
        link_hint1.setText(connecting);
        link_hint2.setText(connect_success);
        setLayoutVisibility(link_layout);
        animationDrawable.start();
    }

    /**
     * 注册热点扫描广播
     */
    void registerWIFIReceiver() {
        if (wifiReceiver == null) {
            wifiReceiver = new WifiReceiver();
            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
    }

    /**
     * 取消热点扫描广播
     */
    void unregisterWIFIReceiver() {
        if (wifiReceiver != null) {
            unregisterReceiver(wifiReceiver);
            wifiReceiver = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ScanInfo event) {
        if (event != null && !event.getDeviceId().equals(XFileApp.device_id)) {
            if (!scanAdapter.contains(event)) {
                event.setLinkType(ScanInfo.LinkType.WIFI);
                scanAdapter.add(event);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ScanInfo scanInfo = (ScanInfo) scanAdapter.getItem(position);
        if (scanInfo.getLinkType() == ScanInfo.LinkType.WIFI) {
            connectToDevice(scanInfo);
        } else {
            connectToHotpot(scanInfo);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ClientFileSocketEvent event) {
        switch (event.getEvent()) {
            case SHAKE_INPUT_PASSWORD:
                IMClientMessageManager.getInstance().sendShakeHandStepT("123456");
                break;
            case SHAKE_HAND_SUCCESS:
                XFileActivityManager.create().finishActivity();
                break;
            case CONNECT_CLOSE:
            case SHAKE_HAND_FAILE:
                setLayoutVisibility(failed_layout);
                Config.is_ap = false;
                break;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ServerFileSocketEvent event) {
        switch (event.getEvent()) {
            case SHAKE_HAND_SUCCESS:
                XFileActivityManager.create().finishActivity();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(WIFIEvent event) {
        if (event.isConnected() && isLinkHotspot && linkInfo != null) {
            connectToDevice(linkInfo);
            linkInfo = null;
            isLinkHotspot = false;
        }
    }


    /* 监听热点变化 */
    private final class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> wifiList = wifiManager.getScanResults();
            if (wifiList == null || wifiList.size() == 0)
                return;
            onReceiveNewNetworks(wifiList);
        }
    }

    /*当搜索到新的wifi热点时判断该热点是否符合规格*/
    public void onReceiveNewNetworks(List<ScanResult> wifiList) {
        String currentSsid = WifiHelper.getConnectWifiSsid();
        currentSsid = currentSsid.replace("\"", "");
        for (ScanResult result : wifiList) {
            if ((result.SSID).startsWith(SysConstant.SEARCH_KEY) && ((!StringUtils.isEmpty(currentSsid) && !currentSsid.contains(result.SSID)) || StringUtils.isEmpty(currentSsid))) {
                ScanInfo scanInfo = new ScanInfo();
                scanInfo.setIp(SysConstant.DEFAULT_AP_IP);
                scanInfo.setName(result.SSID);
                scanInfo.setDeviceId(result.BSSID);
                scanInfo.setLinkType(ScanInfo.LinkType.HOTSPOT);
                scanInfo.setFilePort(SysConstant.FILE_SERVER_PORT);
                scanInfo.setMsgPort(SysConstant.MESSAGE_PORT);
                if (!scanAdapter.contains(scanInfo)) {
                    scanAdapter.add(scanInfo);
                }
            }

        }
    }


    /**
     * 显示布局
     */
    void setLayoutVisibility(View view) {
        View[] viewArray = {guide_layout, scan_layout, failed_layout, device_layout, link_layout};
        for (View v : viewArray) {
            if (v.getId() == view.getId()) {
                v.setVisibility(View.VISIBLE);
            } else {
                v.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 轮训热点是否创建成功
     */
    public void startHotspotTimer() {
        cancelHotspotTimer();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                ThreadPoolManager.getInstance(ConnectActivity.class.getName()).startTaskThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(250);
                            boolean b = WifiHelper.isWifiApEnabled();
//                            System.out.println("****isWifiApEnabled:" + b);
                            if (b) {
                                cancelHotspotTimer();
                                Message msg = Message.obtain();
                                msg.what = HotspotWhat;
                                ScanHandler.sendMessage(msg);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(timerTask, 200, 200);
    }

    /**
     * 取消轮训
     */
    public void cancelHotspotTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            int scanCount = scanAdapter.getCount();
            Message msg = Message.obtain();
            msg.what = ScanWhat;
            msg.arg1 = scanCount;
            ScanHandler.sendMessage(msg);
        }
    };

    @SuppressLint("HandlerLeak")
    Handler ScanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ScanWhat) {
                IMDeviceServerManager.getInstance().cancelBonjour();
                unregisterWIFIReceiver();
                int scanCount = msg.arg1;
                if (scanCount > 0) {
                    setLayoutVisibility(device_layout);
                    scanAdapter.notifyDataSetChanged();
                } else {
                    setLayoutVisibility(failed_layout);
                }
            } else if (msg.what == HotspotWhat) {
                link_hint1.setText(create_connect);
            }
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mPageName);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mPageName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        ScanHandler.removeCallbacks(scanRunnable);
        IMDeviceServerManager.getInstance().cancelBonjour();
        unregisterWIFIReceiver();
        cancelHotspotTimer();
    }

}
