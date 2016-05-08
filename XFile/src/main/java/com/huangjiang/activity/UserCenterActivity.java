package com.huangjiang.activity;

import android.os.Bundle;

import com.huangjiang.xfile.R;
import com.umeng.analytics.MobclickAgent;

public class UserCenterActivity extends BaseActivity {

    private final String mPageName = "UserCenterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_user_center, R.layout.activity_user_center);
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
