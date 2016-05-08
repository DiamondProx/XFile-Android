package com.huangjiang.activity;

import android.os.Bundle;
import android.webkit.WebView;

import com.huangjiang.xfile.R;
import com.umeng.analytics.MobclickAgent;

public class HelpActivity extends BaseActivity {

    private final String mPageName = "HelpActivity";
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_help, R.layout.activity_help);
        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("file:///android_asset/faq/faq-zh/faq.html");
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
