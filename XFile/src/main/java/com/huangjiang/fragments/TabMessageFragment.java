package com.huangjiang.fragments;


import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.huangjiang.XFileApplication;
import com.huangjiang.adapter.MessagePagerAdapter;
import com.huangjiang.business.model.FileType;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.xfile.R;
import com.huangjiang.manager.IMFileManager;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.XFileUtils;
import com.huangjiang.view.TabBar;
import com.umeng.analytics.MobclickAgent;

import java.io.File;

public class TabMessageFragment extends Fragment implements TabBar.OnTabListener, ViewPager.OnPageChangeListener, View.OnClickListener {

    private Logger logger = Logger.getLogger(IMFileManager.class);
    private final String mPageName = "TabMessageFragment";
    ViewPager viewPager;
    TabBar tabBar;
    TextView txt_disk_status, txt_clear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_message, null);
        txt_disk_status = (TextView) view.findViewById(R.id.txt_dis_status);
        txt_clear = (TextView) view.findViewById(R.id.txt_clear);
        txt_clear.setOnClickListener(this);
        tabBar = (TabBar) view.findViewById(R.id.tab_mobile);
        tabBar.setListener(this);
        tabBar.setTabBackground(R.color.tab_green);
        tabBar.setTabSelectBackground(R.color.tab_green_select);
        tabBar.setMenu(R.string.history_message, R.string.inbox);
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new MessagePagerAdapter(getChildFragmentManager()));
        viewPager.setOnPageChangeListener(this);
        viewPager.setCurrentItem(1);
        txt_disk_status.setText(String.format(getString(R.string.disk_status), "10.79GB", "12.34BG"));
        return view;

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
        String testFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "XFile.mp3";
        File file = new File(testFile);
        if (file.exists()) {
            TFileInfo sendFile = new TFileInfo();
            sendFile.setName("XFile");
            sendFile.setFullName("XFile.mp3");
            sendFile.setPosition(0);
            sendFile.setPath(file.getAbsolutePath());
            sendFile.setExtension("mp3");
            sendFile.setTaskId(XFileUtils.buildTaskId());
            sendFile.setLength(file.length());
            sendFile.setFrom(android.os.Build.MODEL);
            sendFile.setIsSend(true);
            sendFile.setFileType(FileType.Audio);
            logger.e("****taskId:" + sendFile.getTaskId());
            IMFileManager.getInstance().createTask(sendFile);
        }


    }


    @Override
    public void onTabSelect(int index) {
        viewPager.setCurrentItem(index);
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
