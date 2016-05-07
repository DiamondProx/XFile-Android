package com.huangjiang.activity;

import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.huangjiang.XFileApplication;
import com.huangjiang.config.Config;
import com.huangjiang.config.SysConstant;
import com.huangjiang.xfile.R;
import com.huangjiang.manager.IMClientMessageManager;
import com.huangjiang.manager.IMDeviceServerManager;
import com.huangjiang.manager.event.ClientFileSocketEvent;
import com.huangjiang.manager.event.ServerFileSocketEvent;
import com.huangjiang.message.event.ScanDeviceInfo;
import com.huangjiang.utils.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ConnectActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Logger logger = Logger.getLogger(ConnectActivity.class);

    ImageView close1, refresh1, refresh2, iv_connecting;
    Button search_join, search_cancel, search_back, connect_back, connecting_cancel, create_ap;
    LinearLayout layout1, layout2, layout3, layout4, layout5;
    TextView connect_hint1, connect_hint2;
    String connecting, connect_success, create_connect, let_friend_join;

    int progress_type;


    int scan_time = 3000;//设备扫描时间
    ListView lv_device;
    ScanDeviceAdapter deviceAdapter;
    private AnimationDrawable animationDrawable;


    private List<ScanResult> wifiList;
    private WifiManager wifiManager;
    private WifiReceiver wifiReceiver;
    private boolean isConnected = false;


    Handler ScanDeviceHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            IMDeviceServerManager.getInstance().cancelBonjour();
            /*销毁时注销广播*/
            unregisterReceiver(wifiReceiver);
            int scanCount = msg.what;
            if (scanCount > 0) {
                // 显示设备信息
                layout1.setVisibility(View.INVISIBLE);
                layout2.setVisibility(View.INVISIBLE);
                layout3.setVisibility(View.INVISIBLE);
                layout4.setVisibility(View.VISIBLE);
                deviceAdapter.notifyDataSetChanged();
            } else {
                // 没找到设备
                layout1.setVisibility(View.INVISIBLE);
                layout2.setVisibility(View.INVISIBLE);
                layout3.setVisibility(View.VISIBLE);
                layout4.setVisibility(View.INVISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    void init() {
        EventBus.getDefault().register(this);
        close1 = (ImageView) findViewById(R.id.close1);
        close1.setOnClickListener(this);
        search_join = (Button) findViewById(R.id.search_join);
        search_join.setOnClickListener(this);
        search_cancel = (Button) findViewById(R.id.search_cancel);
        search_cancel.setOnClickListener(this);
        search_back = (Button) findViewById(R.id.search_back);
        search_back.setOnClickListener(this);
        connect_back = (Button) findViewById(R.id.connect_back);
        connect_back.setOnClickListener(this);
        connecting_cancel = (Button) findViewById(R.id.connecting_cancel);
        connecting_cancel.setOnClickListener(this);
        create_ap = (Button) findViewById(R.id.create_ap);
        create_ap.setOnClickListener(this);

        refresh1 = (ImageView) findViewById(R.id.refresh);
        refresh1.setOnClickListener(this);
        refresh2 = (ImageView) findViewById(R.id.refresh2);
        refresh2.setOnClickListener(this);
        layout1 = (LinearLayout) findViewById(R.id.layout1);
        layout2 = (LinearLayout) findViewById(R.id.layout2);
        layout3 = (LinearLayout) findViewById(R.id.layout3);
        layout4 = (LinearLayout) findViewById(R.id.layout4);
        layout5 = (LinearLayout) findViewById(R.id.layout5);
        lv_device = (ListView) findViewById(R.id.lv_device);
        connect_hint1 = (TextView) findViewById(R.id.connect_hint1);
        connect_hint2 = (TextView) findViewById(R.id.connect_hint2);

        connecting = getString(R.string.connecting);
        connect_success = getString(R.string.connect_success);
        create_connect = getString(R.string.create_connect);
        let_friend_join = getString(R.string.let_friend_join);

        deviceAdapter = new ScanDeviceAdapter(ConnectActivity.this);
        lv_device.setAdapter(deviceAdapter);
        lv_device.setOnItemClickListener(this);
        iv_connecting = (ImageView) findViewById(R.id.iv_connecting);
        iv_connecting.setImageResource(R.drawable.progress_connect);
        animationDrawable = (AnimationDrawable) iv_connecting.getDrawable();

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close1:
                finish();
                break;
            case R.id.search_join:
                scanningDevice();
                break;
            case R.id.search_cancel:
                scanCancel();
                break;
            case R.id.search_back:
                backFirstView();
                break;
            case R.id.refresh:
                scanningDevice();
                break;
            case R.id.refresh2:
                scanningDevice();
                break;
            case R.id.connect_back:
                backFirstView();
                break;
            case R.id.connecting_cancel:
                cancelConnect();
                closeAP();
                break;
            case R.id.create_ap:
                createAP();
                break;
        }
    }

    void backFirstView() {
        layout1.setVisibility(View.VISIBLE);
        layout2.setVisibility(View.INVISIBLE);
        layout3.setVisibility(View.INVISIBLE);
        layout4.setVisibility(View.INVISIBLE);
        layout5.setVisibility(View.INVISIBLE);
    }

    // 开始扫描设备
    void scanningDevice() {
        if (IMDeviceServerManager.getInstance() != null) {
            IMDeviceServerManager.getInstance().startBonjour();
            ScanDeviceHandler.postDelayed(scanRunnable, scan_time);
            deviceAdapter.clearDevices();
            layout1.setVisibility(View.INVISIBLE);
            layout2.setVisibility(View.VISIBLE);
            layout3.setVisibility(View.INVISIBLE);
            layout4.setVisibility(View.INVISIBLE);
            layout5.setVisibility(View.INVISIBLE);

        }
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }


    void connectDevice(ScanDeviceInfo deviceInfoEvent) {

        connect_hint1.setText(connecting);
        connect_hint2.setText(connect_success);
        layout1.setVisibility(View.INVISIBLE);
        layout2.setVisibility(View.INVISIBLE);
        layout3.setVisibility(View.INVISIBLE);
        layout4.setVisibility(View.INVISIBLE);
        layout5.setVisibility(View.VISIBLE);
        animationDrawable.start();
        // 开始连接服务器
        IMClientMessageManager messageClientManager = IMClientMessageManager.getInstance();
        messageClientManager.setHost(deviceInfoEvent.getIp());
        messageClientManager.setPort(deviceInfoEvent.getMessage_port());
        messageClientManager.start();

    }

    void createAP() {
        connect_hint1.setText(create_connect);
        connect_hint2.setText(String.format(let_friend_join, android.os.Build.MODEL));
        layout1.setVisibility(View.INVISIBLE);
        layout2.setVisibility(View.INVISIBLE);
        layout3.setVisibility(View.INVISIBLE);
        layout4.setVisibility(View.INVISIBLE);
        layout5.setVisibility(View.VISIBLE);
        animationDrawable.start();
        // 打开wifi
        if (setWifiAp(true)) {
            Config.is_ap = true;
        }
    }

    void closeAP() {
        if (setWifiAp(false)) {
            Config.is_ap = false;
        }
    }

    // wifi热点开关
    public boolean setWifiAp(boolean state) {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(!state);
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称(MD5加密名称XFILE-机子型号)
            apConfig.SSID = "XFile-Test";
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, state);
        } catch (Exception e) {
            return false;
        }
    }


    void cancelConnect() {
        layout1.setVisibility(View.VISIBLE);
        layout2.setVisibility(View.INVISIBLE);
        layout3.setVisibility(View.INVISIBLE);
        layout4.setVisibility(View.INVISIBLE);
        layout5.setVisibility(View.INVISIBLE);
        animationDrawable.stop();
    }

    Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            int scanCount = deviceAdapter.getCount();
            ScanDeviceHandler.sendEmptyMessage(scanCount);
        }
    };

    void scanCancel() {
        try {
            layout1.setVisibility(View.VISIBLE);
            layout2.setVisibility(View.INVISIBLE);
            layout3.setVisibility(View.INVISIBLE);
            layout4.setVisibility(View.INVISIBLE);
            layout5.setVisibility(View.INVISIBLE);
            ScanDeviceHandler.removeCallbacks(scanRunnable);
        } catch (Exception e) {
            Logger.getLogger(HomeActivity.class).d("scanCancel", e.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ScanDeviceInfo event) {
        if (event != null && !event.getDevice_id().equals(XFileApplication.device_id)) {
            if (!deviceAdapter.containDevice(event)) {
                event.setType(0);
                deviceAdapter.addDevice(event);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ScanDeviceInfo deviceInfoEvent = (ScanDeviceInfo) deviceAdapter.getItem(position);
        if (deviceInfoEvent.getType() == 0) {
            connectDevice(deviceInfoEvent);
        } else {
            connectToHotpot(deviceInfoEvent);
        }
    }

    class ScanDeviceAdapter extends BaseAdapter {

        private List<ScanDeviceInfo> mList = null;

        private LayoutInflater inflater;

        public ScanDeviceAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            mList = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return mList == null ? 0 : mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.listview_scan_device, null);
                holder.name = (TextView) convertView.findViewById(R.id.device_name);
                holder.connectType = (ImageView) convertView.findViewById(R.id.ivConnectType);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ScanDeviceInfo device = mList.get(position);
            holder.name.setText(device.getName());
            if (device.getType() == 0) {
                holder.connectType.setBackgroundResource(R.mipmap.connect_wifi);
            } else {
                holder.connectType.setBackgroundResource(R.mipmap.connect_hotspot);
            }
            return convertView;
        }

        void clearDevices() {
            if (mList != null) {
                mList.clear();
            }
        }

        void addDevice(ScanDeviceInfo device) {
            if (mList != null) {
                mList.add(device);
            }
        }

        boolean containDevice(ScanDeviceInfo device) {
            for (ScanDeviceInfo d : mList) {
                if (d.getDevice_id().equals(device.getDevice_id())) {
                    return true;
                }
            }
            return false;
        }

        final class ViewHolder {
            TextView name;
            ImageView connectType;
        }

        public void setList(List<ScanDeviceInfo> mList) {
            this.mList = mList;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ClientFileSocketEvent event) {
        switch (event.getEvent()) {
            case SHAKE_INPUT_PASSWORD:
                IMClientMessageManager.getInstance().sendShakeHandStepT("123456");
                break;
            case SHAKE_HAND_SUCCESS:
                ConnectActivity.this.finish();
                break;
            case CONNECT_CLOSE:
            case SHAKE_HAND_FAILE:
                // 没找到设备/连接失败
                layout1.setVisibility(View.INVISIBLE);
                layout2.setVisibility(View.INVISIBLE);
                layout3.setVisibility(View.VISIBLE);
                layout4.setVisibility(View.INVISIBLE);
                layout5.setVisibility(View.INVISIBLE);
                break;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ServerFileSocketEvent event) {
        switch (event.getEvent()) {
            case SHAKE_HAND_SUCCESS:
                finish();
                break;
        }
    }

    /* 监听热点变化 */
    private final class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            wifiList = wifiManager.getScanResults();
            if (wifiList == null || wifiList.size() == 0 || isConnected)
                return;
            onReceiveNewNetworks(wifiList);
        }
    }

    /*当搜索到新的wifi热点时判断该热点是否符合规格*/
    public void onReceiveNewNetworks(List<ScanResult> wifiList) {
        for (ScanResult result : wifiList) {
            System.out.println(result.SSID);
            if ((result.SSID).startsWith("XFile")) {
                ScanDeviceInfo d = new ScanDeviceInfo();
                d.setIp("192.168.43.1");
                d.setName(result.SSID);
                d.setDevice_id(result.BSSID);
                d.setType(1);
                d.setFile_port(SysConstant.FILE_SERVER_PORT);
                d.setMessage_port(SysConstant.MESSAGE_PORT);
                if (!deviceAdapter.containDevice(d)) {
                    deviceAdapter.addDevice(d);
                }
            }

        }
    }

    /*连接到热点*/
    public void connectToHotpot(ScanDeviceInfo deviceInfo) {
        WifiConfiguration wifiConfig = this.setWifiParams(deviceInfo.getName());
        int wcgID = wifiManager.addNetwork(wifiConfig);
        boolean flag = wifiManager.enableNetwork(wcgID, true);
        isConnected = flag;
        System.out.println("connect success? " + flag);
        if (flag) {
            connectDevice(deviceInfo);
        }
    }

    /*设置要连接的热点的参数*/
    public WifiConfiguration setWifiParams(String ssid) {
        WifiConfiguration apConfig = new WifiConfiguration();
        apConfig.SSID = "\"" + ssid + "\"";
        apConfig.hiddenSSID = true;
        apConfig.status = WifiConfiguration.Status.ENABLED;
        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        return apConfig;
    }


}
