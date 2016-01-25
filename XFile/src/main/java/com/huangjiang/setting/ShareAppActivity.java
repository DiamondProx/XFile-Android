package com.huangjiang.setting;

import android.os.Bundle;

import com.huangjiang.activity.BaseActivity;
import com.huangjiang.filetransfer.R;

public class ShareAppActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_share_app, R.layout.activity_share_app);
    }

}
