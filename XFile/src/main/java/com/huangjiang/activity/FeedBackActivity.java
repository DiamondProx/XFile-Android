package com.huangjiang.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.huangjiang.xfile.R;
import com.huangjiang.utils.StringUtils;
import com.umeng.analytics.MobclickAgent;

public class FeedBackActivity extends BaseActivity {

    private final String mPageName = "FeedBackActivity";

    EditText edt_feedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_feed_back, R.layout.activity_feed_back, R.mipmap.common_title_ok);
        edt_feedback = (EditText) findViewById(R.id.edt_feedback);
    }

    @Override
    protected void rightClick(int index) {
        String feedBack = edt_feedback.getText().toString();
        if (StringUtils.isEmpty(feedBack)) {
            Toast.makeText(this, R.string.feedback_tip, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(FeedBackActivity.this, R.string.thanks_feedback, Toast.LENGTH_SHORT).show();
        XFileActivityManager.create().finishActivity();
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

}
