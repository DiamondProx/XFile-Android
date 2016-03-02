package com.huangjiang.activity;

import android.os.Bundle;

import com.huangjiang.activity.BaseActivity;
import com.huangjiang.filetransfer.R;

public class ContactUsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_contact_us, R.layout.activity_contact_us);
    }

}
