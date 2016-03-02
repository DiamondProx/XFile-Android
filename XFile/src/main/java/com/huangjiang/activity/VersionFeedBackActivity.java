package com.huangjiang.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huangjiang.filetransfer.R;
import com.huangjiang.utils.XFileUtils;

public class VersionFeedBackActivity extends BaseActivity implements View.OnClickListener {

    Button feedBack, contactUS;
    TextView txtVersion;
    RelativeLayout checkVersion;
    ImageView is_new;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_version, R.layout.activity_version_feedback);
        init();


    }

    void init() {
        checkVersion = (RelativeLayout) findViewById(R.id.check_version);
        feedBack = (Button) findViewById(R.id.feed_back);
        contactUS = (Button) findViewById(R.id.contact_us);
        checkVersion.setOnClickListener(this);
        feedBack.setOnClickListener(this);
        contactUS.setOnClickListener(this);
        txtVersion = (TextView) findViewById(R.id.version);
        txtVersion.setText(String.format(getString(R.string.app_version), XFileUtils.getVersion(this)));
        is_new = (ImageView) findViewById(R.id.is_new);
        is_new.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.check_version:
                Toast.makeText(this, R.string.is_new, Toast.LENGTH_SHORT).show();
                break;
            case R.id.feed_back:
                startActivity(new Intent(this, FeedBackActivity.class));
                break;
            case R.id.contact_us:
                startActivity(new Intent(this, ContactUsActivity.class));
                break;
        }
    }



//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.check_version:
//                Toast.makeText(this, R.string.is_new, Toast.LENGTH_SHORT).show();
//                break;
//            case R.id.feed_back:
//                startActivity(new Intent(this, FeedBackActivity.class));
//                break;
//            case R.id.contact_us:
//                startActivity(new Intent(this, ContactUsActivity.class));
//                break;
//        }
//    }

}
