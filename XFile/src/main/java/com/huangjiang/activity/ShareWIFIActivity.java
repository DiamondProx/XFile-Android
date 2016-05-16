package com.huangjiang.activity;

import android.content.Intent;
import android.os.Bundle;

import com.huangjiang.config.Config;
import com.huangjiang.utils.MobileDataUtils;
import com.huangjiang.utils.WifiHelper;
import com.huangjiang.wfs.CopyUtil;
import com.huangjiang.wfs.WebService;
import com.huangjiang.xfile.R;
import com.umeng.analytics.MobclickAgent;

/**
 * 分享当前程序
 */
public class ShareWIFIActivity extends BaseActivity {

    private final String mPageName = "ShareWIFIActivity";
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
        if (WifiHelper.setWifiAp(true, "DM-JoinMe")) {
            if (!Config.getMobileData()) {
                MobileDataUtils.setMobileData(this, false);
            } else {
                MobileDataUtils.setMobileData(this, true);
            }
            intent = new Intent(this, WebService.class);
            startService(intent);

        }
    }

    void stopService() {
        if (intent != null) {
            WifiHelper.setWifiAp(false);
            stopService(intent);
            intent = null;
        }
    }

    @Override
    protected void onDestroy() {
        stopService();
        super.onDestroy();
    }


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


}
