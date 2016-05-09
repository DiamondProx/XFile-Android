package com.huangjiang.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huangjiang.utils.XFileUtils;
import com.huangjiang.xfile.R;
import com.umeng.analytics.MobclickAgent;

public class VersionFeedBackActivity extends BaseActivity implements View.OnClickListener {

    private final String mPageName = "VersionFeedBackActivity";
    Button feedBack;
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
        checkVersion.setOnClickListener(this);
        feedBack.setOnClickListener(this);
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
                openEmail();
                break;
        }
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

    void openEmail() {
        Intent data = new Intent(Intent.ACTION_SENDTO);
        data.setData(Uri.parse("mailto:26666302@qq.com"));
        data.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feed_back_title));
        data.putExtra(Intent.EXTRA_TEXT, getString(R.string.feed_back_hint));
        startActivity(data);
    }


}
