package com.huangjiang.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.huangjiang.business.model.StorageRootInfo;
import com.huangjiang.config.SysConstant;
import com.huangjiang.filetransfer.R;
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

public class ConnectActivity extends Activity implements View.OnClickListener {

    ImageView close1, refresh;
    Button search_join, search_cancel, search_back;
    LinearLayout layout1, layout2, layout3, layout4;

    int scan_time = 5000;//设备扫描时间
    ListView lv_device;
    ScanDeviceAdapter deviceAdapter;


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
        refresh = (ImageView) findViewById(R.id.refresh);
        refresh.setOnClickListener(this);
        layout1 = (LinearLayout) findViewById(R.id.layout1);
        layout2 = (LinearLayout) findViewById(R.id.layout2);
        layout3 = (LinearLayout) findViewById(R.id.layout3);
        layout4 = (LinearLayout) findViewById(R.id.layout4);
        lv_device = (ListView) findViewById(R.id.lv_device);
        deviceAdapter = new ScanDeviceAdapter(ConnectActivity.this);
        lv_device.setAdapter(deviceAdapter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close1:
                finish();
                break;
            case R.id.search_join:
                layout1.setVisibility(View.INVISIBLE);
                layout2.setVisibility(View.VISIBLE);
                layout3.setVisibility(View.INVISIBLE);
                scanningDevice();
                break;
            case R.id.search_cancel:
                layout1.setVisibility(View.VISIBLE);
                layout2.setVisibility(View.INVISIBLE);
                layout3.setVisibility(View.VISIBLE);
                break;
            case R.id.search_back:
                layout1.setVisibility(View.INVISIBLE);
                layout2.setVisibility(View.VISIBLE);
                layout3.setVisibility(View.INVISIBLE);
                break;
            case R.id.refresh:
                layout1.setVisibility(View.VISIBLE);
                layout2.setVisibility(View.INVISIBLE);
                layout3.setVisibility(View.INVISIBLE);
                break;
        }
    }

    void scanningDevice() {
        if (DeviceClient.getInstance().getChannel() != null) {
            try {
                Channel channel = DeviceClient.getInstance().getChannel();
                Header header = new Header();
                header.setCommandId(SysConstant.CMD_Bonjour);
                String ip = NetStateUtil.getIPv4(ConnectActivity.this);
                XFileProtocol.Bonjour.Builder bonjour = XFileProtocol.Bonjour.newBuilder();
                bonjour.setIp(ip);
                bonjour.setPort(8081);
                byte[] body = bonjour.build().toByteArray();
                header.setLength(SysConstant.HEADER_LENGTH + body.length);
                byte[] data = new byte[SysConstant.HEADER_LENGTH + body.length];
                System.arraycopy(header.toByteArray(), 0, data, 0, SysConstant.HEADER_LENGTH);
                System.arraycopy(body, 0, data, SysConstant.HEADER_LENGTH, body.length);
                channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(data), new InetSocketAddress(SysConstant.BROADCASE_ADDRESS, SysConstant.BROADCASE_PORT))).sync();
            } catch (Exception e) {
                Logger.getLogger(HomeActivity.class).d("sendBonjourMessage", e.getMessage());
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DeviceInfoEvent event) {
        if (event != null) {
            Toast.makeText(ConnectActivity.this, "FindDevice.Ip:" + event.getName(), Toast.LENGTH_SHORT).show();
            System.out.println("*****device.Ip:" + event.getName());
            long threadId = Thread.currentThread().getId();
            System.out.println("*****currentThreadId33333:" + threadId);
        }
    }

    class ScanDeviceAdapter extends BaseAdapter {

        private List<DeviceInfoEvent> mList = null;

        private LayoutInflater inflater;

        public ScanDeviceAdapter(Context context) {
            inflater = LayoutInflater.from(context);
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
                convertView = inflater.inflate(R.layout.listview_storage_root, null);
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
