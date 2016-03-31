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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import com.huangjiang.XFileApplication;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.config.SysConstant;
import com.huangjiang.filetransfer.R;
import com.huangjiang.manager.IMFileManager;
import com.huangjiang.manager.event.FileEvent;
import com.huangjiang.manager.event.FileReceiveEvent;
import com.huangjiang.manager.event.FileSendEvent;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.view.TabBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TabMessageFragment extends Fragment implements TabBar.OnTabListener, ViewPager.OnPageChangeListener, View.OnClickListener {


    List<View> list;
    ViewPager viewPager;
    TabBar tabBar;
    TextView txt_disk_status, txt_clear;

    LinearLayout video_layout, picture_layout, music_layout, other_layout, folder_layout, install_layout;

    ListView lv_message;

    List<TFileInfo> listMessage;

    TransferMessageAdpater adpater;

    Button btn_sendfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, null);

        EventBus.getDefault().register(this);

        txt_disk_status = (TextView) view.findViewById(R.id.txt_dis_status);
        txt_clear = (TextView) view.findViewById(R.id.txt_clear);
        tabBar = (TabBar) view.findViewById(R.id.tab_mobile);
        tabBar.setListener(this);
        tabBar.setTabBackground(R.color.tab_green);
        tabBar.setTabSelectBackground(R.color.tab_green_select);
        View view_message = inflater.inflate(R.layout.page_message, null);
        lv_message = (ListView) view_message.findViewById(R.id.lv_message);
        btn_sendfile = (Button) view_message.findViewById(R.id.btn_sendfile);
        btn_sendfile.setOnClickListener(this);
        adpater = new TransferMessageAdpater(getActivity());
        lv_message.setAdapter(adpater);

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


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    void testMessageData() {
        listMessage = new ArrayList<>();


//        TransferMessageInfo message = new TransferMessageInfo();
//        message.setMessageType(2);
//        message.setFrom("R815T");
//        message.setFileName("4dazxcvbng");
//        message.setFileSize("12.53kb");
//        listMessage.add(message);
//
//
//        message = new TransferMessageInfo();
//        message.setMessageType(1);
//        message.setFrom("R815T");
//        message.setFileName("指南针.apk");
//        message.setFileSize("682kb");
//        listMessage.add(message);
//
//
//        message = new TransferMessageInfo();
//        message.setMessageType(1);
//        message.setFrom("R815T");
//        message.setFileName("头文字D.mp3");
//        message.setFileSize("1.65MB");
//        listMessage.add(message);
//
//        message = new TransferMessageInfo();
//        message.setMessageType(1);
//        message.setFrom("R815T");
//        message.setFileName("975124556321");
//        message.setFileSize("3.65MB");
//        listMessage.add(message);

        adpater.notifyDataSetChanged();

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
            case R.id.btn_sendfile:
                testSendFile();
                break;
        }
    }

    void testSendFile() {

//        TFileInfo tFileInfo = new TFileInfo();
//        tFileInfo.setIs_send(true);
//        tFileInfo.setFrom("R815T");
//        tFileInfo.setLength(1024 * 10);
//        tFileInfo.setFull_name("指南针.apk");
//        listMessage.add(tFileInfo);
//        adpater.notifyDataSetChanged();

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
            sendFile.setTaskId(SysConstant.TEMP_TASK_ID);
            sendFile.setData(ByteString.copyFrom("1".getBytes()));
            sendFile.setLength(file.length());
            IMFileManager.getInstance().startTransferFile(sendFile.build());
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


    class TransferMessageAdpater extends BaseAdapter {

        private LayoutInflater mInflater;


        public TransferMessageAdpater(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return listMessage == null ? 0 : listMessage.size();
        }

        @Override
        public Object getItem(int position) {
            return listMessage == null ? null : listMessage.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        int REVICE_MESSAGE;
        int SEND_MESSAGE;

        @Override
        public int getItemViewType(int position) {
            TFileInfo message = listMessage.get(position);
            if (!message.is_send()) {
                return REVICE_MESSAGE;
            } else {
                return SEND_MESSAGE;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final TFileInfo message = listMessage.get(position);
            boolean isSend = message.is_send();
            VideoViewHoler videoHolder = null;
            if (convertView == null) {
                videoHolder = new VideoViewHoler();
                if (!isSend) {
                    convertView = mInflater.inflate(R.layout.listview_transfer_message_revice, null);
                } else {
                    convertView = mInflater.inflate(R.layout.listview_transfer_message_send, null);
                }

                videoHolder.headImg = (ImageView) convertView.findViewById(R.id.head);
                videoHolder.fileImg = (ImageView) convertView.findViewById(R.id.fileImg);
                videoHolder.from = (TextView) convertView.findViewById(R.id.from);
                videoHolder.name = (TextView) convertView.findViewById(R.id.fileName);
                videoHolder.size = (TextView) convertView.findViewById(R.id.fileSize);
                convertView.setTag(videoHolder);
            } else {
                videoHolder = (VideoViewHoler) convertView.getTag();
            }
            if (message != null) {
                videoHolder.headImg.setImageResource(R.mipmap.avatar_default);
                videoHolder.fileImg.setImageResource(R.mipmap.data_folder_documents_placeholder);
                videoHolder.from.setText(String.format(getString(R.string.from), message.getFrom()));
                videoHolder.name.setText(message.getFull_name());
                videoHolder.size.setText("1024kb");

            }
            return convertView;
        }

        final class VideoViewHoler {
            ImageView headImg;
            ImageView fileImg;
            TextView from;
            TextView name;
            TextView size;
        }

    }

//    @Subscribe
//    public void onEventMainThread(MyEvent event) {
//        if (event != null) {
//            TransferMessageInfo message = new TransferMessageInfo();
//            message.setMessageType(1);
//            message.setFrom("R815T");
//            message.setFileName("event.apkkkkkkk");
//            message.setFileSize("682kb");
//            listMessage.add(message);
//            adpater.notifyDataSetChanged();
//        }
//    }


    @Override
    public void onTabSelect(int index) {
        viewPager.setCurrentItem(index);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FileReceiveEvent fileEvent) {
        // 接收文件状态
        FileEvent e = fileEvent.getEvent();
        switch (e) {
            case CREATE_FILE_SUCCESS:
                TFileInfo receiveFile = fileEvent.getFileInfo();
                receiveFile.setIs_send(false);
                listMessage.add(receiveFile);
                adpater.notifyDataSetChanged();
                break;
            case SET_FILE_SUCCESS:
                Toast.makeText(getActivity(), "receive_success", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FileSendEvent fileEvent) {
        // 发送文件状态
        FileEvent e = fileEvent.getEvent();
        switch (e) {
            case CREATE_FILE_SUCCESS:
                TFileInfo receiveFile = fileEvent.getFileInfo();
                receiveFile.setIs_send(true);
                listMessage.add(receiveFile);
                adpater.notifyDataSetChanged();
                break;
            case CHECK_TASK_FAILED:
                Toast.makeText(getActivity(), "check_failed", Toast.LENGTH_SHORT).show();
                break;
            case SET_FILE_SUCCESS:
                Toast.makeText(getActivity(), "send_success", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
