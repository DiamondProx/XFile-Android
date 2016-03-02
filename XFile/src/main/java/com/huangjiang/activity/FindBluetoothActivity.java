package com.huangjiang.activity;

import android.os.Bundle;

import com.huangjiang.activity.BaseActivity;
import com.huangjiang.filetransfer.R;

public class FindBluetoothActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_find_bluethooth, R.layout.activity_find_bluetooth);
    }

}
