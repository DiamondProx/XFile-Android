package com.huangjiang.setting;

import android.os.Bundle;
import android.app.Activity;
import android.widget.Toast;

import com.huangjiang.activity.BaseActivity;
import com.huangjiang.filetransfer.R;

public class FeedBackActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);
        initView(R.string.title_activity_feed_back, R.layout.activity_feed_back, R.mipmap.common_title_ok);
    }

    @Override
    protected void rightClick(int index) {
        Toast.makeText(FeedBackActivity.this, "rightClick", Toast.LENGTH_SHORT).show();
    }

}
