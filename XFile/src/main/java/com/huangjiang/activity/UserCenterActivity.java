package com.huangjiang.activity;

import android.os.Bundle;

import com.huangjiang.activity.BaseActivity;
import com.huangjiang.filetransfer.R;

public class UserCenterActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_user_center, R.layout.activity_user_center);
    }

}
