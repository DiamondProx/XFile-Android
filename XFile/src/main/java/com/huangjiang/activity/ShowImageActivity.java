package com.huangjiang.activity;

import android.os.Bundle;

import com.huangjiang.xfile.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.polites.android.GestureImageView;
import com.umeng.analytics.MobclickAgent;

/**
 * 图片查看
 */
public class ShowImageActivity extends BaseActivity {

    private final String mPageName = "ShowImageActivity";
    GestureImageView dmImageView;
    public static final String URL = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_show_image, R.layout.activity_show_image);
        dmImageView = (GestureImageView) findViewById(R.id.dmImageView);

        if (getIntent().hasExtra(URL)) {
            String url = getIntent().getStringExtra(URL);
            if (!url.startsWith("file:///")) {
                url = "file:///" + url;
            }
            ImageLoader.getInstance().displayImage(url, dmImageView);
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
}
