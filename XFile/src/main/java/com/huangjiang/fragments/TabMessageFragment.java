package com.huangjiang.fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import com.huangjiang.XFileApplication;
import com.huangjiang.adapter.TransmitAdapter;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.filetransfer.R;
import com.huangjiang.manager.IMFileManager;
import com.huangjiang.manager.event.FileEvent;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.XFileUtils;
import com.huangjiang.view.MenuItem;
import com.huangjiang.view.PopupMenu;
import com.huangjiang.view.TabBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TabMessageFragment extends Fragment implements TabBar.OnTabListener, ViewPager.OnPageChangeListener, View.OnClickListener, AdapterView.OnItemClickListener, PopupMenu.OnItemSelectedListener {

    private Logger logger = Logger.getLogger(IMFileManager.class);
    List<View> list;
    ViewPager viewPager;
    TabBar tabBar;
    TextView txt_disk_status, txt_clear;
    LinearLayout video_layout, picture_layout, music_layout, other_layout, folder_layout, install_layout;
    ListView lv_message;
    List<TFileInfo> listMessage;
    TransmitAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, null);

        EventBus.getDefault().register(this);

        txt_disk_status = (TextView) view.findViewById(R.id.txt_dis_status);
        txt_clear = (TextView) view.findViewById(R.id.txt_clear);
        txt_clear.setOnClickListener(this);
        tabBar = (TabBar) view.findViewById(R.id.tab_mobile);
        tabBar.setListener(this);
        tabBar.setTabBackground(R.color.tab_green);
        tabBar.setTabSelectBackground(R.color.tab_green_select);
        View view_message = inflater.inflate(R.layout.page_message, null);
        lv_message = (ListView) view_message.findViewById(R.id.lv_message);
        adapter = new TransmitAdapter(getActivity());
        lv_message.setAdapter(adapter);
        lv_message.setOnItemClickListener(this);

        View view_inbox = inflater.inflate(R.layout.page_inbox, null);
        video_layout = (LinearLayout) view_inbox.findViewById(R.id.video_layout);
        picture_layout = (LinearLayout) view_inbox.findViewById(R.id.picture_layout);
        music_layout = (LinearLayout) view_inbox.findViewById(R.id.music_layout);
        other_layout = (LinearLayout) view_inbox.findViewById(R.id.other_layout);
        folder_layout = (LinearLayout) view_inbox.findViewById(R.id.folder_layout);
        install_layout = (LinearLayout) view_inbox.findViewById(R.id.install_layout);

        video_layout.setOnClickListener(this);
        picture_layout.setOnClickListener(this);
        music_layout.setOnClickListener(this);
        other_layout.setOnClickListener(this);
        folder_layout.setOnClickListener(this);
        install_layout.setOnClickListener(this);


        list = new ArrayList<>();
        list.add(view_message);
        list.add(view_inbox);

        tabBar.setMenu(R.string.message, R.string.inbox);

        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new MyPagerAdapter(list));
        viewPager.setOnPageChangeListener(this);
        viewPager.setCurrentItem(1);

        txt_disk_status.setText(String.format(getString(R.string.disk_status), "10.79GB", "12.34BG"));

        testMessageData();

        Toast.makeText(getActivity(), "initView", Toast.LENGTH_SHORT).show();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    void testMessageData() {
        listMessage = new ArrayList<>();
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        tabBar.setCurrentTab(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_clear:
                testSendFile();
                break;
        }
    }

    void testSendFile() {

        if (XFileApplication.connect_type == 0) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        String testFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "doufu.mp3";
        File file = new File(testFile);
        if (file.exists()) {
            XFileProtocol.File.Builder sendFile = XFileProtocol.File.newBuilder();
            sendFile.setName("doufu");
            sendFile.setFullName("doufu.mp3");
            sendFile.setMd5("md5");
            sendFile.setPosition(0);
            sendFile.setPath(file.getAbsolutePath());
            sendFile.setExtension("mp3");
            sendFile.setTaskId(XFileUtils.buildTaskId());
            sendFile.setLength(file.length());
            sendFile.setFrom(android.os.Build.MODEL);
            sendFile.setIsSend(true);
            logger.e("****taskId:" + sendFile.getTaskId());
            IMFileManager.getInstance().createTask(sendFile.build());
        }


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TFileInfo tFileInfo = listMessage.get(position);
        PopupMenu menu = new PopupMenu(getActivity());
        // Set Listener
        menu.setOnItemSelectedListener(TabMessageFragment.this);
        menu.setTFileInfo(tFileInfo);
        // Add Menu (Android menu like style)
        menu.add(0, R.string.transfer).setIcon(getResources().getDrawable(R.mipmap.data_downmenu_send));
        menu.add(1, R.string.multi_select).setIcon(getResources().getDrawable(R.mipmap.data_downmenu_check));
        menu.add(2, R.string.play).setIcon(getResources().getDrawable(R.mipmap.data_downmenu_open));
        menu.add(3, R.string.more).setIcon(getResources().getDrawable(R.mipmap.data_downmenu_more));
        menu.show(view);
    }

    @Override
    public void onItemSelected(MenuItem item, TFileInfo tFileInfo) {
        if (tFileInfo != null) {
            Toast.makeText(getActivity(), "clickMemu", Toast.LENGTH_SHORT).show();
            IMFileManager.getInstance().cancelTask(tFileInfo);
        }
    }


    class MyPagerAdapter extends PagerAdapter {

        public List<View> mListViews;

        public MyPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
            return mListViews.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {

        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {

        }

    }


    @Override
    public void onTabSelect(int index) {
        viewPager.setCurrentItem(index);
    }


    void updateTransmitState(TFileInfo tFileInfo) {
        int firstVisible = lv_message.getFirstVisiblePosition();
        int lastVisible = lv_message.getLastVisiblePosition();
        int position = adapter.getPosition(tFileInfo);
        TFileInfo currentFile = (TFileInfo) adapter.getItem(position);
        if (currentFile != null && position >= firstVisible && position <= lastVisible) {
            TransmitAdapter.ViewHolder holder = (TransmitAdapter.ViewHolder) (lv_message.getChildAt(position - firstVisible).getTag());
            adapter.updateTransmitState(holder, currentFile);
        }
    }

    /*
     * 文件传输状态
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(TFileInfo tFileInfo) {

        FileEvent fileEvent = tFileInfo.getFileEvent();
        switch (fileEvent) {
            case CREATE_FILE_SUCCESS: {
                if (tFileInfo.isSend()) {
                    tFileInfo.setFrom(getString(R.string.send_to, tFileInfo.getFrom()));
                } else {
                    tFileInfo.setFrom(getString(R.string.receive_from, tFileInfo.getFrom()));
                }
                adapter.addTFileInfo(tFileInfo);
                adapter.notifyDataSetChanged();
            }
            break;
            case SET_FILE_SUCCESS: {
                adapter.updateTFileInfo(tFileInfo);
                updateTransmitState(tFileInfo);
                if (tFileInfo.isSend()) {
                    // 发送完成之后查看是否有等待的任务
                    TFileInfo waitFile = adapter.getWaitFile();
                    if (waitFile != null) {
                        XFileProtocol.File reqFile = XFileUtils.buildSendFile(waitFile);
                        IMFileManager.getInstance().checkTask(reqFile);
                    }
                }

            }
            break;
            case SET_FILE:
            case SET_FILE_STOP:
            case WAITING:
                adapter.updateTFileInfo(tFileInfo);
                updateTransmitState(tFileInfo);
                break;
            case CANCEL_FILE:
                adapter.cancelTask(tFileInfo);
                updateTransmitState(tFileInfo);
                break;
        }
    }

}
