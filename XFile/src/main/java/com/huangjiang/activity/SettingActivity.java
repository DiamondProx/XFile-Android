package com.huangjiang.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.huangjiang.business.app.AppLogic;
import com.huangjiang.business.event.RootEvent;
import com.huangjiang.config.Config;
import com.huangjiang.view.SwitchButton;
import com.huangjiang.xfile.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SettingActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    private final String mPageName = "SettingActivity";

    RelativeLayout rtl_clear;
    SwitchButton sb_sound, sb_vibration, sb_update, sb_mobile_data, sb_hidden, sb_need_pwd, sb_install_mode;
    AppLogic appLogic;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_setting, R.layout.activity_setting);
        init();
    }

    void init() {
        appLogic = new AppLogic(SettingActivity.this);
        EventBus.getDefault().register(this);
        rtl_clear = (RelativeLayout) findViewById(R.id.rtl_clear);
        rtl_clear.setOnClickListener(this);
        sb_sound = (SwitchButton) findViewById(R.id.sb_sound);
        sb_vibration = (SwitchButton) findViewById(R.id.sb_vibration);
        sb_update = (SwitchButton) findViewById(R.id.sb_update);
        sb_mobile_data = (SwitchButton) findViewById(R.id.sb_mobile_data);
        sb_hidden = (SwitchButton) findViewById(R.id.sb_hidden);
        sb_need_pwd = (SwitchButton) findViewById(R.id.sb_need_pwd);
        sb_install_mode = (SwitchButton) findViewById(R.id.sb_install_mode);

        sb_sound.setChecked(Config.getSound());
        sb_vibration.setChecked(Config.getVibration());
        sb_update.setChecked(Config.getUpdate());
        sb_mobile_data.setChecked(Config.getMobileData());
        sb_hidden.setChecked(Config.getHidden());
        sb_need_pwd.setChecked(Config.getNeedPWD());
        sb_install_mode.setChecked(Config.getInstallSilent());

        sb_sound.setOnCheckedChangeListener(this);
        sb_vibration.setOnCheckedChangeListener(this);
        sb_update.setOnCheckedChangeListener(this);
        sb_mobile_data.setOnCheckedChangeListener(this);
        sb_hidden.setOnCheckedChangeListener(this);
        sb_need_pwd.setOnCheckedChangeListener(this);
        sb_install_mode.setOnCheckedChangeListener(this);

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.rtl_clear:
                ImageLoader.getInstance().clearDiskCache();
                Toast.makeText(SettingActivity.this, R.string.clear_success, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sb_sound:
                Config.setSound(isChecked);
                break;
            case R.id.sb_vibration:
                Config.setVibration(isChecked);
                break;
            case R.id.sb_update:
                setMessageEnable(isChecked);
                break;
            case R.id.sb_mobile_data:
                Config.setMobileData(isChecked);
                break;
            case R.id.sb_hidden:
                Config.setHidden(isChecked);
                break;
            case R.id.sb_need_pwd:
                Config.setNeedPWD(isChecked);
                break;
            case R.id.sb_install_mode:
                setInstallSilent(isChecked);
                break;
        }
    }

    /**
     * 设置是否接受新消息
     */
    void setMessageEnable(boolean isChecked) {
        Config.setUpdate(isChecked);
        PushAgent mPushAgent = PushAgent.getInstance(this);
        if (isChecked) {
            mPushAgent.enable();
        } else {
            mPushAgent.disable();
        }
    }

    /**
     * 设置安装模式(root权限)
     */
    void setInstallSilent(boolean isChecked) {
        if (isChecked) {
            progressDialog = ProgressDialog.show(SettingActivity.this, getString(R.string.progress_title), getString(R.string.upgrade_root_permission));
            progressDialog.show();
            appLogic.upgradeRootPermission(getPackageCodePath());
        } else {
            Config.setInstallSilent(false);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RootEvent event) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (event.isResult()) {
            Config.setInstallSilent(true);
            Toast.makeText(SettingActivity.this, R.string.upgrade_root_permission_success, Toast.LENGTH_SHORT).show();
        } else {
            sb_install_mode.setChecked(false);
            Toast.makeText(SettingActivity.this, R.string.upgrade_root_permission_failure, Toast.LENGTH_SHORT).show();
        }
    }


}
