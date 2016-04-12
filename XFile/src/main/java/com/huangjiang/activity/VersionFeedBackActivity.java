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
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
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
    ImageView is_new, icon, ivTest;

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
        icon.setOnClickListener(this);
//        String path=Environment.getExternalStorageDirectory().getAbsolutePath() + "/ES.apk";
//        icon.setImageDrawable(getApkIcon(this, path));
        ivTest = (ImageView) findViewById(R.id.ivTest);
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
            case R.id.icon:
                testAnimSet(ivTest);
                break;
        }
    }


    void testMove() {

//        Animation translateAnimation=AnimationHelper.loadAnimation(this, R.anim.transmit_move);
        Animation translateAnimation = new TranslateAnimation(0, 100, 0, 100);
        translateAnimation.setDuration(1000);//设置动画持续时间为3秒
        translateAnimation.setFillAfter(true);
        icon.startAnimation(translateAnimation);
    }

    void testRotate() {
        Animation rotate = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(1000);//设置动画持续时间为3秒
        rotate.setFillAfter(true);
        icon.startAnimation(rotate);
    }

    void testScale() {

        Animation rotate = new ScaleAnimation(1.0f, 0f, 1.0f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(1000);//设置动画持续时间为3秒
        rotate.setFillAfter(true);
        icon.startAnimation(rotate);
    }

    void testAnimSet(View targetView) {
        int[] endLocation = new int[2];
        targetView.getLocationOnScreen(endLocation);
        int endX = endLocation[0] + targetView.getWidth() / 2;
        int endY = endLocation[1] + targetView.getHeight() / 2;
        int[] startLocation = new int[2];
        icon.getLocationOnScreen(startLocation);
        int startX = startLocation[0] + icon.getWidth() / 2;
        int startY = startLocation[1] + icon.getHeight() / 2;
        int moveX = endX - startX;
        int moveY = endY - startY;
        AnimationSet animationSet = new AnimationSet(false);
        Animation translateAnimation = new TranslateAnimation(0, moveX, 0, moveY);
        Animation rotateAnimation = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        Animation scaleAnimation = new ScaleAnimation(1.0f, 0.3f, 1.0f, 0.3f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animationSet.addAnimation(rotateAnimation);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(translateAnimation);
        animationSet.setDuration(500);
        animationSet.setFillAfter(false);
        animationSet.setAnimationListener(new SendAnimationListener(icon));
        icon.startAnimation(animationSet);
    }

    public class SendAnimationListener implements Animation.AnimationListener {

        View view;

        public SendAnimationListener(View view) {
            this.view = view;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            this.view.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    void testLocation(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        Toast.makeText(VersionFeedBackActivity.this, "x:" + x + ",y:" + y + ",width:" + view.getWidth() + ",height:" + view.getHeight(), Toast.LENGTH_SHORT).show();
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


}
