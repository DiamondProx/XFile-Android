package com.huangjiang.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.huangjiang.filetransfer.R;

import com.huangjiang.wfs.WebService;
import com.huangjiang.wfs.CopyUtil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by King on 2016-04-30.
 */
public class SharePCActivity  extends BaseActivity  implements CompoundButton.OnCheckedChangeListener {

    private ToggleButton toggleBtn;
    private TextView urlText;

    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_share_pc, R.layout.activity_share_pc);
        initViews();
        initFiles();

        intent = new Intent(this, WebService.class);
    }

    private void initViews() {
        toggleBtn = (ToggleButton) findViewById(R.id.toggleBtn);
        toggleBtn.setOnCheckedChangeListener(this);
        urlText = (TextView) findViewById(R.id.urlText);
    }

    private void initFiles() {
        new CopyUtil(this).assetsCopy();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            String ip = getLocalIpAddress();
            if (ip == null) {
                Toast.makeText(this, R.string.msg_net_off, Toast.LENGTH_SHORT)
                        .show();
                urlText.setText("");
            } else {
                startService(intent);
                urlText.setText("http://" + ip + ":" + WebService.PORT + "/");
            }
        } else {
            stopService(intent);
            urlText.setText("");
        }
    }

    /** 获取当前IP地址 */
    private String getLocalIpAddress() {
        try {
            // 遍历网络接口
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                // 遍历IP地址
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    // 非回传地址时返回
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        if (intent != null) {
            stopService(intent);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
