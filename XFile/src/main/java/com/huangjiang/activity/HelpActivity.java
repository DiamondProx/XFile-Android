package com.huangjiang.activity;

import android.os.Bundle;
import android.webkit.WebView;

import com.huangjiang.activity.BaseActivity;
import com.huangjiang.filetransfer.R;

public class HelpActivity extends BaseActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_help, R.layout.activity_help);
        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("file:///android_asset/faq/faq-zh/faq.html");
    }

}
