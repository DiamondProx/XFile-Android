package com.huangjiang.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huangjiang.XFileApplication;
import com.huangjiang.config.SysConstant;
import com.huangjiang.filetransfer.R;
import com.huangjiang.fragments.TabMessageFragment;
import com.huangjiang.fragments.TabMobileFragment;
import com.huangjiang.manager.IMClientFileManager;
import com.huangjiang.manager.IMClientMessageManager;
import com.huangjiang.manager.IMServerFileManager;
import com.huangjiang.manager.IMServerMessageManager;
import com.huangjiang.manager.event.ClientFileSocketEvent;
import com.huangjiang.manager.event.ServerFileSocketEvent;
import com.huangjiang.service.IMService;
import com.huangjiang.view.AnimationHelper;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends FragmentActivity implements OnClickListener, OnCheckedChangeListener {

    private int cursorWidth; // 游标的长度
    private int offset; // 间隔
    private ImageView cursor;
    private Animation animation = null;
    private int originalIndex;

    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private RadioGroup radioGroup;

    TabMobileFragment tabMobileFragment;
    TabMessageFragment tabMessageFragment;

    private SlidingMenu slidingMenu = null;

    private int mTabIndex;
    RadioButton rdb_home, rdb_message;
    CheckBox cb;
    TextView tvPersonNumber, tvCountNumber, tvFileNumber;

    public List<Fragment> fragments = new ArrayList<Fragment>();

    private TextView device_name, connect_device_name;

    ColorStateList gray_color, blue_color, green_color;

    Button btn_share, btn_close;

    RelativeLayout top_main_layout, top_connect_layout;
    FrameLayout head_layout;
    ImageView fileThumb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        EventBus.getDefault().register(this);
        initializeView();
        initData();
    }

    void initializeView() {

        gray_color = this.getResources().getColorStateList(R.color.gray_font);
        blue_color = this.getResources().getColorStateList(R.color.tab_select);
        green_color = this.getResources().getColorStateList(R.color.tab_green_select);

        // 设置抽屉菜单
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.RIGHT);
        // 触摸边界拖出菜单
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        // 将抽屉菜单与主页面关联起来
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setShadowWidth(5);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        slidingMenu.setMenu(R.layout.slidingmenu_right);


        // 测试代码
        findViewById(R.id.btn_right).setOnClickListener(this);
        findViewById(R.id.help_layout).setOnClickListener(this);
        findViewById(R.id.feedback_layout).setOnClickListener(this);
        findViewById(R.id.setting_layout).setOnClickListener(this);
        findViewById(R.id.share_app_layout).setOnClickListener(this);
        findViewById(R.id.share_pc_layout).setOnClickListener(this);
        top_main_layout = (RelativeLayout) findViewById(R.id.top_main_layout);
        top_connect_layout = (RelativeLayout) findViewById(R.id.top_connect_layout);
        btn_close = (Button) findViewById(R.id.btn_close);
        btn_close.setOnClickListener(this);
        connect_device_name = (TextView) findViewById(R.id.txt_connect_name);
        device_name = (TextView) findViewById(R.id.mobile_name);
        tvPersonNumber = (TextView) slidingMenu.findViewById(R.id.person_number);
        tvCountNumber = (TextView) slidingMenu.findViewById(R.id.count_number);
        tvFileNumber = (TextView) slidingMenu.findViewById(R.id.file_number);
        slidingMenu.findViewById(R.id.edit_user_layout).setOnClickListener(this);
        head_layout = (FrameLayout) findViewById(R.id.head_layout);


        // 选择按钮列表
        radioGroup = (RadioGroup) findViewById(R.id.rg_tab);
        radioGroup.setOnCheckedChangeListener(this);
        // 单选按钮
        rdb_home = (RadioButton) findViewById(R.id.rdb_mobile);
        rdb_message = (RadioButton) findViewById(R.id.rdb_message);

        // 初始化标签
        fragmentManager = getSupportFragmentManager();
        tabMobileFragment = new TabMobileFragment();
        tabMessageFragment = new TabMessageFragment();
        fragments.add(tabMobileFragment);
        fragments.add(tabMessageFragment);

        initCursor(fragments.size());

        rdb_home.setChecked(true);

        btn_share = (Button) findViewById(R.id.btn_share);
        btn_share.setOnClickListener(this);

        fileThumb = (ImageView) findViewById(R.id.fileThumb);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_right:
                slidingMenu.showSecondaryMenu();
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
            case R.id.share_app_layout:
                startActivity(new Intent(HomeActivity.this, ShareAppActivity.class));
                break;
            case R.id.share_pc_layout:
                startActivity(new Intent(HomeActivity.this, SharePCActivity.class));
                break;
            case R.id.edit_user_layout:
                Intent intent = new Intent(HomeActivity.this, UserCenterActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_share:
                showConnect();
                break;
            case R.id.btn_close:
                closeConnect();
                break;
            default:
                break;
        }

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkId) {
        // 切换标签
        for (int i = 0; i < group.getChildCount(); i++) {
            if (group.getChildAt(i).getId() == checkId) {
                Fragment fragment = fragments.get(i);
                transaction = fragmentManager.beginTransaction();
                getCurrentFragment().onResume();
                if (fragment.isAdded()) {
                    fragment.onResume(); // 启动目标tab的onResume()
                } else {
                    transaction.add(R.id.content, fragment);
                }
                showTab(i, transaction);
                transaction.commit();
                mTabIndex = i;
            }

        }

        // 箭头切换

        int one = 2 * offset + cursorWidth;
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

            default:
                break;
        }

    }

    public Fragment getCurrentFragment() {
        return fragments.get(mTabIndex);
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
        mTabIndex = idx; // 更新目标tab为当前tab
    }

    /*
     * 根据tag的数量初始化游标的位置
     *
     * @param tagNum
     */
    public void initCursor(int tagNum) {
        cursorWidth = BitmapFactory.decodeResource(getResources(), R.mipmap.tab_mobile_arrow_down_blue).getWidth();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        offset = ((dm.widthPixels / tagNum) - cursorWidth) / 2;

        cursor = (ImageView) findViewById(R.id.ivCursor);
        Matrix matrix = new Matrix();
        matrix.setTranslate(offset, 0);
        cursor.setImageMatrix(matrix);
    }

    void initData() {
        tvPersonNumber.setText(String.format(getString(R.string.person_number), "0"));
        tvCountNumber.setText(String.format(getString(R.string.count_number), "0"));
        tvFileNumber.setText(String.format(getString(R.string.file_total_b), "0.00"));
        device_name.setText(android.os.Build.MODEL);
        startService(new Intent(this, IMService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        stopService(new Intent(HomeActivity.this, IMService.class));
    }

    public void initFileThumbView(Drawable drawable, int width, int height, int locationX, int locationY) {
        // 修改坐标位置,图标大小
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) fileThumb.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        layoutParams.setMargins(locationX, locationY - height / 2, 0, 0);
        fileThumb.setLayoutParams(layoutParams);
        fileThumb.setImageDrawable(drawable);
        fileThumb.setVisibility(View.VISIBLE);
        // 更新图标大小
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        fileThumb.measure(w, h);
        // 读取头像坐标
        int[] endLocation = new int[2];
        head_layout.getLocationOnScreen(endLocation);

        AnimationHelper.startSendFileAnimation(fileThumb, head_layout, locationX, locationY, endLocation[0], endLocation[1]);
        if (XFileApplication.connect_type == 1) {

        } else {

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ClientFileSocketEvent event) {

        switch (event.getEvent()) {
            case SHAKE_HAND_SUCCESS:
                XFileApplication.connect_type = 1;
                // 客户端连接成功
                top_main_layout.setVisibility(View.INVISIBLE);
                top_connect_layout.setVisibility(View.VISIBLE);
                connect_device_name.setText(event.getDevice_name());
                break;
            case CONNECT_CLOSE:
                XFileApplication.connect_type = 0;
                // 客户端关闭
                top_main_layout.setVisibility(View.VISIBLE);
                top_connect_layout.setVisibility(View.INVISIBLE);
                break;
            case SHAKE_INPUT_PASSWORD:
                // 要求输入密码
                Toast.makeText(HomeActivity.this, "要求输入密码", Toast.LENGTH_SHORT).show();
                IMClientMessageManager.getInstance().sendShakeHandStepT("123456");
                break;
        }


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ServerFileSocketEvent event) {
        switch (event.getEvent()) {
            case SHAKE_HAND_SUCCESS:
                XFileApplication.connect_type = 2;
                // 被连接成功
                top_main_layout.setVisibility(View.INVISIBLE);
                top_connect_layout.setVisibility(View.VISIBLE);
                connect_device_name.setText(event.getDevice_name());
                break;
            case CONNECT_CLOSE:
                XFileApplication.connect_type = 0;
                // 服务端关闭
                top_main_layout.setVisibility(View.VISIBLE);
                top_connect_layout.setVisibility(View.INVISIBLE);
                break;
        }
    }

    //---------------------TEST---------------------------

    // 发送广播发现设备
    void sendBonjour() {
//        if (DeviceClient.getInstance().getChannel() != null) {
//            try {
//                Channel channel = DeviceClient.getInstance().getChannel();
//                Header header = new Header();
//                header.setCommandId(SysConstant.CMD_Bonjour);
//                String ip = NetStateUtils.getIPv4(HomeActivity.this);
//                XFileProtocol.Bonjour.Builder bonjour = XFileProtocol.Bonjour.newBuilder();
//                bonjour.setIp(ip);
//                bonjour.setBrocast_port(SysConstant.BROADCASE_PORT);
//                byte[] body = bonjour.build().toByteArray();
//                header.setLength(SysConstant.HEADER_LENGTH + body.length);
//                byte[] data = new byte[SysConstant.HEADER_LENGTH + body.length];
//                System.arraycopy(header.toByteArray(), 0, data, 0, SysConstant.HEADER_LENGTH);
//                System.arraycopy(body, 0, data, SysConstant.HEADER_LENGTH, body.length);
//                channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(data), new InetSocketAddress(SysConstant.BROADCASE_ADDRESS, SysConstant.BROADCASE_PORT))).sync();
//            } catch (Exception e) {
//                Logger.getLogger(HomeActivity.class).d("sendBonjourMessage", e.getMessage());
//            }
//        }
    }

    void tranferFile() {
//        FileClient client = new FileClient();
//        client.connect();
//        byte[] req = "t".getBytes();
//        ByteBuf byteBuf = Unpooled.buffer(req.length);
//        byteBuf.writeBytes(req);
//        client.write(byteBuf);
    }

    void sendMessage() {
//        MessageClient client = new MessageClient();
//        client.connect();
    }

    void testConnect() {
        IMClientMessageManager messageClientManager = IMClientMessageManager.getInstance();
        messageClientManager.setHost("172.16.88.208");
        messageClientManager.setPort(SysConstant.MESSAGE_PORT);
        messageClientManager.start();
    }

    void testFile() {
        IMClientFileManager fileClientManager = IMClientFileManager.getInstance();
        fileClientManager.setHost("127.0.0.1");
        fileClientManager.setPort(SysConstant.FILE_SERVER_PORT);
        fileClientManager.start();
    }

    void closeConnect() {
        switch (XFileApplication.connect_type) {
            case 1://0 无连接,2 服务端,1 客户端:
                IMClientMessageManager.getInstance().stop();
                IMClientFileManager.getInstance().stop();
                break;
            case 2:
                // TODO 服务器关闭连接
                IMServerMessageManager.getInstance().stop();
                IMServerFileManager.getInstance().stop();
                break;
        }
    }

    void testReadFile() {
        //MappedByteBuffer
        //RandomAccessFile
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File path = Environment.getExternalStorageDirectory();
                String sendTextPath = path.getAbsolutePath() + "/send.txt";
                File sendFile = new File(sendTextPath);
                System.out.println("*****length:" + sendFile.length());
                if (sendFile.exists()) {
                    RandomAccessFile rafi = new RandomAccessFile(sendTextPath, "r");
                    byte[] readData = new byte[19];
//                    rafi.seek(2);
//                    rafi.read(readData);
//                    rafi.readFully(readData);
                    rafi.read(readData, 1, 2);
                    String str = new String(readData, "UTF-8");
                    System.out.println("*****readData:" + str);
                }
                System.out.println("*****sdPath:" + path.getAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("*****testReadFile.error:" + e.getMessage());
        }


    }

    void sendFile() {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File path = Environment.getExternalStorageDirectory();
                File sendFile = new File(path.getAbsolutePath() + "/send.txt");
                System.out.println("*****length:" + sendFile.length());
                if (sendFile.exists()) {

                }
            }
        } catch (Exception e) {
            System.out.println("*****sendFile.error:" + e.getMessage());
        }
    }

    void showConnect() {
        Intent connectActivity = new Intent(HomeActivity.this, ConnectActivity.class);
        startActivity(connectActivity);
    }


}
