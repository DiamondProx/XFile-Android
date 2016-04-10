package com.huangjiang.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
    ImageView is_new, icon;

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
        icon = (ImageView) findViewById(R.id.icon);
//        String path=Environment.getExternalStorageDirectory().getAbsolutePath() + "/ES.apk";
//        icon.setImageDrawable(getApkIcon(this, path));
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

    /*
     * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过
     * appInfo.publicSourceDir = apkPath;来修正这个问题，详情参见:
     * http://code.google.com/p/android/issues/detail?id=9151
     */
    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                Log.e("ApkIconLoader", e.toString());
            }
        }
        return null;
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
