package com.huangjiang.activity;

import android.os.Bundle;

import com.huangjiang.activity.BaseActivity;
import com.huangjiang.filetransfer.R;

public class SettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_setting, R.layout.activity_setting);
    }

}
