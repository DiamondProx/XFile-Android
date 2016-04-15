package com.huangjiang.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.huangjiang.filetransfer.R;
import com.polites.android.GestureImageView;

/**
 * 图片查看
 */
public class ShowImageActivity extends BaseActivity {

    GestureImageView dmImageView;
    public static final String URL = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_show_image, R.layout.activity_show_image);
        dmImageView = (GestureImageView) findViewById(R.id.dmImageView);
        if (getIntent().hasExtra(URL)) {
            String url = getIntent().getStringExtra(URL);
            Bitmap bitmap = BitmapFactory.decodeFile(url);
            dmImageView.setImageBitmap(bitmap);
        }
    }
}
