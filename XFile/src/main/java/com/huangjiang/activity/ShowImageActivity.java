package com.huangjiang.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.huangjiang.xfile.R;
import com.polites.android.GestureImageView;
import com.umeng.analytics.MobclickAgent;

/**
 * 图片查看
 */
public class ShowImageActivity extends BaseActivity {

    private final String mPageName = "ShowImageActivity";
    GestureImageView dmImageView;
    public static final String URL = "url";
    Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_show_image, R.layout.activity_show_image);
        dmImageView = (GestureImageView) findViewById(R.id.dmImageView);
        if (getIntent().hasExtra(URL)) {
            String url = getIntent().getStringExtra(URL);
            bitmap = BitmapFactory.decodeFile(url);
            dmImageView.setImageBitmap(bitmap);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bitmap != null) {
            bitmap.recycle();
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
