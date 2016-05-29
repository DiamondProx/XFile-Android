package com.huangjiang.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huangjiang.XFileApp;
import com.huangjiang.broadcast.NetWorkReceiver;
import com.huangjiang.business.event.DiskEvent;
import com.huangjiang.business.event.RecordEvent;
import com.huangjiang.business.history.HistoryLogic;
import com.huangjiang.business.model.LinkType;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.config.Config;
import com.huangjiang.fragments.HistoryFragment;
import com.huangjiang.fragments.TabMessageFragment;
import com.huangjiang.fragments.TabMobileFragment;
import com.huangjiang.manager.IMClientFileManager;
import com.huangjiang.manager.IMClientMessageManager;
import com.huangjiang.manager.IMFileManager;
import com.huangjiang.manager.IMServerFileManager;
import com.huangjiang.manager.IMServerMessageManager;
import com.huangjiang.manager.event.ClientFileSocketEvent;
import com.huangjiang.manager.event.FileEvent;
import com.huangjiang.manager.event.ServerFileSocketEvent;
import com.huangjiang.service.IMService;
import com.huangjiang.utils.DisplayUtils;
import com.huangjiang.utils.SoundHelper;
import com.huangjiang.utils.VibratorUtils;
import com.huangjiang.utils.WifiHelper;
import com.huangjiang.utils.XFileUtils;
import com.huangjiang.view.AnimationHelper;
import com.huangjiang.view.CustomDialog;
import com.huangjiang.view.DialogHelper;
import com.huangjiang.xfile.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends BaseActivity implements OnClickListener, OnCheckedChangeListener {

    private final String mPageName = "HomeActivity";

    private int cursorWidth;
    private int offsetWidth;
    private ImageView cursor;
    private Animation animation;
    private int originalIndex;
    private FragmentManager fragmentManager;
    TabMobileFragment tabMobileFragment;
    TabMessageFragment tabMessageFragment;
    private SlidingMenu slidingMenu;
    private int mTabIndex;
    RadioButton rdb_home, rdb_message;
    TextView tv_person_count, tv_link_count, tv_total_size;
    public List<Fragment> fragments = new ArrayList<>();
    private TextView connect_device_name;
    ColorStateList gray_color, blue_color, green_color;
    RelativeLayout top_main_layout, top_connect_layout;
    FrameLayout head_layout;
    ImageView iv_throw;
    HistoryLogic historyLogic;
    private long firstTime = 0;
    NetWorkReceiver mNetWorkReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();

    }

    void init() {

        Resources resources = getResources();
        gray_color = resources.getColorStateList(R.color.gray_font);
        blue_color = resources.getColorStateList(R.color.tab_blue);
        green_color = resources.getColorStateList(R.color.tab_green);

        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.RIGHT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setShadowWidth(5);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        slidingMenu.setMenu(R.layout.slidingmenu_right);


        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_menu).setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);
        findViewById(R.id.invite_layout).setOnClickListener(this);
        findViewById(R.id.help_layout).setOnClickListener(this);
        findViewById(R.id.feedback_layout).setOnClickListener(this);
        findViewById(R.id.setting_layout).setOnClickListener(this);
        findViewById(R.id.contact_layout).setOnClickListener(this);
        top_main_layout = (RelativeLayout) findViewById(R.id.top_main_layout);
        top_connect_layout = (RelativeLayout) findViewById(R.id.top_connect_layout);
        connect_device_name = (TextView) findViewById(R.id.txt_connect_name);
        head_layout = (FrameLayout) findViewById(R.id.head_layout);
        TextView tv_device_name = (TextView) findViewById(R.id.mobile_name);
        tv_person_count = (TextView) slidingMenu.findViewById(R.id.person_count);
        tv_link_count = (TextView) slidingMenu.findViewById(R.id.link_count);
        tv_total_size = (TextView) slidingMenu.findViewById(R.id.total_size);
        slidingMenu.findViewById(R.id.edit_user_layout).setOnClickListener(this);

        ((RadioGroup) findViewById(R.id.rg_tab)).setOnCheckedChangeListener(this);
        rdb_home = (RadioButton) findViewById(R.id.rdb_mobile);
        rdb_message = (RadioButton) findViewById(R.id.rdb_message);

        fragmentManager = getSupportFragmentManager();
        tabMobileFragment = new TabMobileFragment();
        tabMessageFragment = new TabMessageFragment();
        fragments.add(tabMobileFragment);
        fragments.add(tabMessageFragment);

        cursorWidth = BitmapFactory.decodeResource(getResources(), R.mipmap.tab_mobile_arrow_down_blue).getWidth();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        offsetWidth = ((dm.widthPixels / fragments.size()) - cursorWidth) / 2;

        cursor = (ImageView) findViewById(R.id.iv_cursor);
        Matrix matrix = new Matrix();
        matrix.setTranslate(offsetWidth, 0);
        cursor.setImageMatrix(matrix);

        rdb_home.setChecked(true);
        iv_throw = (ImageView) findViewById(R.id.iv_throw);
        historyLogic = new HistoryLogic(HomeActivity.this);
        tv_device_name.setText(android.os.Build.MODEL);

        EventBus.getDefault().register(this);
        startService(new Intent(this, IMService.class));
        registerNetWorkReceiver();
        historyLogic.getRecordInfo();


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_menu:
                slidingMenu.showSecondaryMenu();
                historyLogic.getRecordInfo();
                break;
            case R.id.help_layout:
                startActivity(new Intent(HomeActivity.this, HelpActivity.class));
                break;
            case R.id.feedback_layout:
                startActivity(new Intent(HomeActivity.this, VersionFeedBackActivity.class));
                break;
            case R.id.setting_layout:
                startActivity(new Intent(HomeActivity.this, SettingActivity.class));
                break;
            case R.id.invite_layout:
                startActivity(new Intent(HomeActivity.this, ShareAppActivity.class));
                break;
            case R.id.contact_layout:
                startActivity(new Intent(HomeActivity.this, ContactUsActivity.class));
                break;
            case R.id.edit_user_layout:
                startActivity(new Intent(HomeActivity.this, UserCenterActivity.class));
                break;
            case R.id.btn_share:
                startActivity(new Intent(HomeActivity.this, ConnectActivity.class));
                break;
            case R.id.btn_close:
                shutDownLink();
                break;
        }

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkId) {
        // 页面切换
        for (int i = 0; i < group.getChildCount(); i++) {
            if (group.getChildAt(i).getId() == checkId) {
                Fragment fragment = fragments.get(i);
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                fragments.get(mTabIndex).onResume();
                if (fragment.isAdded()) {
                    fragment.onResume();
                } else {
                    transaction.add(R.id.content, fragment);
                }
                showTab(i, transaction);
                transaction.commit();
                mTabIndex = i;
            }

        }

        // 箭头切换
        int one = 2 * offsetWidth + cursorWidth;
        switch (originalIndex) {
            case 0:
                if (mTabIndex == 1) {
                    animation = new TranslateAnimation(0, one, 0, 0);
                }
                break;
            case 1:
                if (mTabIndex == 0) {
                    animation = new TranslateAnimation(one, 0, 0, 0);
                }
                break;
        }
        if (originalIndex != mTabIndex) {
            animation.setFillAfter(true);
            animation.setDuration(100);
            cursor.startAnimation(animation);
            originalIndex = mTabIndex;
        }
        switch (mTabIndex) {
            case 0:
                cursor.setImageResource(R.mipmap.tab_mobile_arrow_down_blue);
                rdb_home.setTextColor(blue_color);
                rdb_message.setTextColor(gray_color);
                break;
            case 1:
                cursor.setImageResource(R.mipmap.tab_computer_arrow_down_green);
                rdb_home.setTextColor(gray_color);
                rdb_message.setTextColor(green_color);
                break;
        }

    }


    private void showTab(int idx, FragmentTransaction ft) {
        for (int i = 0; i < fragments.size(); i++) {
            Fragment fragment = fragments.get(i);
            if (idx == i) {
                ft.show(fragment);
            } else {
                ft.hide(fragment);
            }
        }
        mTabIndex = idx;
    }

    /**
     * 发送文件
     */
    public void sendTFile(TFileInfo tFileInfo, ImageView originImage) {
        if (XFileApp.mLinkType == LinkType.NONE) {
            startActivity(new Intent(HomeActivity.this, ConnectActivity.class));
            return;
        }
        setThrowView(originImage);
        IMFileManager.getInstance().createTask(tFileInfo.newInstance());
    }


    /**
     * 设置甩图标
     */
    private void setThrowView(ImageView originImage) {

        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        // 原始图标信息
        int[] startLocation = new int[2];
        originImage.getLocationOnScreen(startLocation);
        originImage.measure(w, h);
        Drawable drawable = originImage.getDrawable();
        if (drawable != null) {
            // 修改坐标位置,图标大小
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) iv_throw.getLayoutParams();
            layoutParams.width = originImage.getWidth();
            layoutParams.height = originImage.getHeight();
            layoutParams.setMargins(startLocation[0], startLocation[1] - XFileApp.mAndroidTitleBar, 0, 0);
            iv_throw.setLayoutParams(layoutParams);
            iv_throw.setImageDrawable(drawable.getConstantState().newDrawable());
            iv_throw.setVisibility(View.VISIBLE);

            iv_throw.measure(w, h);
            // 读取头像坐标
            int[] endLocation = new int[2];
            head_layout.getLocationOnScreen(endLocation);
            if (Config.getSound()) {
                SoundHelper.playDragThrow();
            }
            // 执行动画
            AnimationHelper.startSendFileAnimation(iv_throw, head_layout, startLocation[0], startLocation[1], endLocation[0], endLocation[1]);
        }
    }

    /**
     * 客户端事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ClientFileSocketEvent event) {
        switch (event.getEvent()) {
            case SHAKE_HAND_SUCCESS:
                XFileApp.mLinkType = LinkType.CLIENT;
                top_main_layout.setVisibility(View.INVISIBLE);
                top_connect_layout.setVisibility(View.VISIBLE);
                connect_device_name.setText(event.getDevice_name());
                if (Config.getSound())
                if (Config.getVibration()) {
                    VibratorUtils.Vibrate();
                }
                historyLogic.addOneConnect(event.getDevice_name());
                break;
            case CONNECT_CLOSE:
                XFileApp.mLinkType = LinkType.NONE;
                top_main_layout.setVisibility(View.VISIBLE);
                top_connect_layout.setVisibility(View.INVISIBLE);
                if (Config.is_ap) {
                    WifiHelper.removeWifi();
                    Config.is_ap = false;
                }
                IMClientMessageManager.getInstance().stop();
                IMClientFileManager.getInstance().stop();
                break;
            case SHAKE_INPUT_PASSWORD:
                IMClientMessageManager.getInstance().sendShakeHandStepT("123456");
                break;
        }

    }

    /**
     * 服务端事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ServerFileSocketEvent event) {
        switch (event.getEvent()) {
            case SHAKE_HAND_SUCCESS:
                XFileApp.mLinkType = LinkType.SERVER;
                top_main_layout.setVisibility(View.INVISIBLE);
                top_connect_layout.setVisibility(View.VISIBLE);
                connect_device_name.setText(event.getDevice_name());
                if (Config.getSound()) {
                    SoundHelper.plaOnline();
                }
                if (Config.getVibration()) {
                    VibratorUtils.Vibrate();
                }
                historyLogic.addOneConnect(event.getDevice_name());
                break;
            case CONNECT_CLOSE:
                XFileApp.mLinkType = LinkType.NONE;
                top_main_layout.setVisibility(View.VISIBLE);
                top_connect_layout.setVisibility(View.INVISIBLE);
                if (Config.is_ap) {
                    WifiHelper.setWifiAp(false);
                    Config.is_ap = false;
                }
                break;
        }
    }


    /**
     * 关闭连接(服务端暂时重启)
     */
    void shutDownLink() {
        DialogHelper.confirmClose(HomeActivity.this, new CustomDialog.DialogCallback() {
            @Override
            public void onDialogClick(int id, TFileInfo tFileInfo, Object... params) {
                if (id == R.id.dialog_confirm_ok) {
                    switch (XFileApp.mLinkType) {
                        case CLIENT:
                            IMClientMessageManager.getInstance().stop();
                            IMClientFileManager.getInstance().stop();
                            break;
                        case SERVER:
                            IMServerMessageManager.getInstance().start();
                            IMServerFileManager.getInstance().start();
                            break;
                    }
                }
            }
        });
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

    /**
     * 连接历史记录
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RecordEvent recordEvent) {
        tv_person_count.setText(String.format(getString(R.string.person_number), recordEvent.getDeviceCount()));
        tv_link_count.setText(String.format(getString(R.string.count_number), recordEvent.getConnectCount()));
        tv_total_size.setText(XFileUtils.parseSize(recordEvent.getTotalSize()));
    }

    /*
     * 切换到历史消息页面
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(TFileInfo tFileInfo) {
        FileEvent fileEvent = tFileInfo.getFileEvent();
        switch (fileEvent) {
            case CREATE_FILE_SUCCESS:
                if (!HistoryFragment.isInit) {
                    rdb_message.setChecked(true);
                }
                break;
            case CREATE_FILE_FAILED:
                if (tFileInfo.isSend()) {
                    Toast.makeText(HomeActivity.this, String.format(getString(R.string.file_send_failed), tFileInfo.getFullName()), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 读写错误:磁盘空间不足/权限拒绝
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DiskEvent diskEvent) {
        switch (diskEvent.getDiskState()) {
            case ENOUGH:
                Toast.makeText(HomeActivity.this, R.string.disk_enough, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * 点两次返回
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (XFileApp.mLinkType != LinkType.NONE) {
                shutDownLink();
                return true;
            }
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 1200) {
                Toast.makeText(HomeActivity.this, R.string.exit_confirm, Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
                return true;
            } else {
                XFileActivityManager.create().finishAllActivity();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 获取屏幕状态栏高度
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            XFileApp.mAndroidTitleBar = DisplayUtils.getAndroidTitleBar(HomeActivity.this);
        }
    }

    void registerNetWorkReceiver() {
        mNetWorkReceiver = new NetWorkReceiver();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetWorkReceiver, mFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unregisterReceiver(mNetWorkReceiver);
        stopService(new Intent(HomeActivity.this, IMService.class));
    }
}
