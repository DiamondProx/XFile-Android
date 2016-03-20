package com.huangjiang.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
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

import com.huangjiang.config.SysConstant;
import com.huangjiang.filetransfer.R;
import com.huangjiang.manager.IMDeviceServerManager;
import com.huangjiang.message.DeviceClient;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.event.DeviceInfoEvent;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.NetStateUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

public class ConnectActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    ImageView close1, refresh1, refresh2, iv_connecting;
    Button search_join, search_cancel, search_back, connect_back, connecting_cancel;
    LinearLayout layout1, layout2, layout3, layout4, layout5;

    int scan_time = 5000;//设备扫描时间
    ListView lv_device;
    ScanDeviceAdapter deviceAdapter;
    private AnimationDrawable animationDrawable;


    Handler ScanDeviceHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
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
        deviceAdapter = new ScanDeviceAdapter(ConnectActivity.this);
        lv_device.setAdapter(deviceAdapter);
        lv_device.setOnItemClickListener(this);
        iv_connecting = (ImageView) findViewById(R.id.iv_connecting);
        iv_connecting.setImageResource(R.drawable.progress_connect);
        animationDrawable = (AnimationDrawable) iv_connecting.getDrawable();

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
            String ipAddress = NetStateUtil.getIPv4(ConnectActivity.this);
            IMDeviceServerManager.getInstance().DeviceBroCast(ipAddress, SysConstant.BROADCASE_PORT);
            ScanDeviceHandler.postDelayed(scanRunnable, scan_time);
            deviceAdapter.clearDevices();
            layout1.setVisibility(View.INVISIBLE);
            layout2.setVisibility(View.VISIBLE);
            layout3.setVisibility(View.INVISIBLE);
            layout4.setVisibility(View.INVISIBLE);
            layout5.setVisibility(View.INVISIBLE);
        }
    }


    void connectDevice() {
        layout1.setVisibility(View.INVISIBLE);
        layout2.setVisibility(View.INVISIBLE);
        layout3.setVisibility(View.INVISIBLE);
        layout4.setVisibility(View.INVISIBLE);
        layout5.setVisibility(View.VISIBLE);
        animationDrawable.start();

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
    public void onEventMainThread(DeviceInfoEvent event) {
        if (event != null) {
            deviceAdapter.addDevice(event);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        connectDevice();

    }

    class ScanDeviceAdapter extends BaseAdapter {

        private List<DeviceInfoEvent> mList = null;

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
            return null;
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
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            DeviceInfoEvent device = mList.get(position);
            holder.name.setText(device.getName());
            return convertView;
        }

        void clearDevices() {
            if (mList != null) {
                mList.clear();
            }
        }

        void addDevice(DeviceInfoEvent device) {
            if (mList != null) {
                mList.add(device);
            }
        }

        final class ViewHolder {
            TextView name;
        }

        public void setList(List<DeviceInfoEvent> mList) {
            this.mList = mList;
        }
    }


}
