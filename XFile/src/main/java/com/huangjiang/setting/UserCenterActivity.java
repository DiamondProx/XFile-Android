package com.huangjiang.setting;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

import com.huangjiang.activity.BaseActivity;
import com.huangjiang.filetransfer.R;

public class UserCenterActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.user_center_title, R.layout.activity_user_center);
    }

}
