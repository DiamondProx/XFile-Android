package com.huangjiang.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.huangjiang.xfile.R;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.XFileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ShareAppActivity extends BaseActivity {

    Button btn_bluetooth_invite, btn_zero_invite;
    Logger logger = Logger.getLogger(ShareAppActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_share_app, R.layout.activity_share_app);
        init();
    }

    void init() {
        btn_bluetooth_invite = (Button) findViewById(R.id.bluetooth_invite);
        btn_bluetooth_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFile(ShareAppActivity.this);
            }
        });
        btn_zero_invite = (Button) findViewById(R.id.zero_invite);
        btn_zero_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShareAppActivity.this, ShareWIFIActivity.class));
            }
        });

    }

    /**
     * 通过蓝牙发送文件
     */
    private void sendFile(Activity activity) {
        PackageManager localPackageManager = activity.getPackageManager();
        Intent localIntent = null;
        HashMap<String, ActivityInfo> localHashMap = null;
        try {
            localIntent = new Intent();
            localIntent.setAction(Intent.ACTION_SEND);
            File file = new File(XFileUtils.getProgramPath(activity));
            localIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            localIntent.setType("*/*");
            List<ResolveInfo> localList = localPackageManager.queryIntentActivities(localIntent, 0);
            localHashMap = new HashMap<>();
            for (ResolveInfo resolveInfo : localList) {
                ActivityInfo localActivityInfo2 = resolveInfo.activityInfo;
                String str = localActivityInfo2.applicationInfo.processName;
                if (str.contains("bluetooth"))
                    localHashMap.put(str, localActivityInfo2);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
        if (localHashMap != null && localHashMap.size() > 0) {
            ActivityInfo localActivityInfo1 = localHashMap.get("com.android.bluetooth");
            if (localActivityInfo1 == null) {
                localActivityInfo1 = localHashMap.get("com.mediatek.bluetooth");
            }
            if (localActivityInfo1 == null) {
                Iterator<ActivityInfo> localIterator2 = localHashMap.values().iterator();
                if (localIterator2.hasNext())
                    localActivityInfo1 = localIterator2.next();
            }
            if (localActivityInfo1 != null) {
                localIntent.setComponent(new ComponentName(localActivityInfo1.packageName, localActivityInfo1.name));
                activity.startActivityForResult(localIntent, 4098);
            }
        }


    }


}
