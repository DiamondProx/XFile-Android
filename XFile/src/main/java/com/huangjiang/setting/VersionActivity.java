package com.huangjiang.setting;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.huangjiang.activity.BaseActivity;
import com.huangjiang.filetransfer.R;

public class VersionActivity extends BaseActivity implements View.OnClickListener {

    Button checkVersion, feedBack, contactUS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_version, R.layout.activity_version);
        init();
    }

    void init() {
        checkVersion = (Button) findViewById(R.id.check_version);
        feedBack = (Button) findViewById(R.id.feed_back);
        contactUS = (Button) findViewById(R.id.contact_us);
        checkVersion.setOnClickListener(this);
        feedBack.setOnClickListener(this);
        contactUS.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_version:
                Toast.makeText(this, "已经是最新版本", Toast.LENGTH_SHORT).show();
                break;
            case R.id.feed_back:
                startActivity(new Intent(this, FeedBackActivity.class));
                break;
            case R.id.contact_us:
                startActivity(new Intent(this, ContactUsActivity.class));
                break;
        }
    }
}
