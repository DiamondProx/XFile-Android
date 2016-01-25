package com.huangjiang.setting;

import android.os.Bundle;
import android.app.Activity;
import android.widget.Toast;

import com.huangjiang.activity.BaseActivity;
import com.huangjiang.filetransfer.R;
import com.huangjiang.tools.XFileActivityManager;

public class FeedBackActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_feed_back, R.layout.activity_feed_back, R.mipmap.common_title_ok);
    }

    @Override
    protected void rightClick(int index) {
        Toast.makeText(FeedBackActivity.this, "意见已经提交,谢谢你的支持", Toast.LENGTH_SHORT).show();
        XFileActivityManager.create().finishActivity();
    }

}
