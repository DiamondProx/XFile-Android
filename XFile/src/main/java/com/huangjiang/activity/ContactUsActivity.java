package com.huangjiang.activity;

import android.os.Bundle;

import com.huangjiang.xfile.R;
import com.umeng.analytics.MobclickAgent;

public class ContactUsActivity extends BaseActivity {

    private final String mPageName = "ContactUsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_contact_us, R.layout.activity_contact_us);
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
