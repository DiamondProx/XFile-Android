package com.huangjiang.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.huangjiang.adapter.StorageRootAdapter;
import com.huangjiang.manager.event.MyEvent;
import com.huangjiang.business.model.StorageRootInfo;
import com.huangjiang.utils.XFileUtils;
import com.huangjiang.view.AppBrowserControl;
import com.huangjiang.view.FileBrowserControl;
import com.huangjiang.view.FileBrowserControl.FileBrowserListener;
import com.huangjiang.view.PictureBrowserControl;

import com.huangjiang.filetransfer.R;
import com.huangjiang.view.TabBar;

import org.greenrobot.eventbus.EventBus;

public class TabMobileFragment extends Fragment implements OnPageChangeListener, OnItemClickListener, FileBrowserListener, TabBar.OnTabListener,View.OnClickListener {

    List<View> list;
    ViewPager viewPager;
    StorageRootAdapter rootAdapter;
    FileBrowserControl fileBrowser;
    TabBar tabBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mobile, null);
        tabBar = (TabBar) view.findViewById(R.id.tab_mobile);
        tabBar.setListener(this);
        View view_search = inflater.inflate(R.layout.page_search, null);
        view_search.findViewById(R.id.button).setOnClickListener(this);
        View view_root = inflater.inflate(R.layout.page_root, null);
        View view_picture = inflater.inflate(R.layout.page_picture, null);
        View view_music = inflater.inflate(R.layout.page_video, null);
        View view_video = inflater.inflate(R.layout.page_video, null);
        View view_app = inflater.inflate(R.layout.page_app, null);

        initializeRootView(view_root);
        initializePictureView(view_picture);
        initializeAppView(view_app);

        list = new ArrayList<>();
        list.add(view_search);
        list.add(view_root);
        list.add(view_picture);
        list.add(view_music);
        list.add(view_video);
        list.add(view_app);

        tabBar.setMenu(R.mipmap.common_tab_refresh_white, R.string.mobile_all, R.string.picture, R.string.music, R.string.video, R.string.application);

        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new MyPagerAdapter(list));
        viewPager.setOnPageChangeListener(this);
        viewPager.setCurrentItem(1);

        return view;
    }

    void initializeRootView(View view) {

        ListView listRoot = (ListView) view.findViewById(R.id.list_root);
        rootAdapter = new StorageRootAdapter(getActivity());
        listRoot.setAdapter(rootAdapter);
        listRoot.setOnItemClickListener(this);
        List<StorageRootInfo> list = new ArrayList<StorageRootInfo>();

        // 根目录
        StorageRootInfo rootMemory = new StorageRootInfo();
        rootMemory.setImg(R.mipmap.data_folder_memory_default);
        rootMemory.setDesc(this.getString(R.string.root_memory_storage));
        rootMemory.setFilePath("/");
        list.add(rootMemory);

        // 内存
        if (XFileUtils.ExistSDCard()) {
            StorageRootInfo rootSdCard = new StorageRootInfo();
            rootSdCard.setImg(R.mipmap.data_folder_sdcard_default);
            rootSdCard.setDesc(this.getString(R.string.root_sdcard_storage));
            rootSdCard.setFilePath(XFileUtils.getStorageCardPath());
            list.add(rootSdCard);
        }

        // TF存储
        List<String> sdPaths = XFileUtils.getSdCardPaths();
        for (String string : sdPaths) {
            StorageRootInfo rootSdCardExten = new StorageRootInfo();
            rootSdCardExten.setImg(R.mipmap.data_folder_sdcard_default);
            rootSdCardExten.setDesc(this.getString(R.string.root_sdcard_storage_exten));
            rootSdCardExten.setFilePath(string);
            list.add(rootSdCardExten);
        }

        rootAdapter.getRoots().addAll(list);
        rootAdapter.notifyDataSetChanged();

        fileBrowser = (FileBrowserControl) view.findViewById(R.id.fileBrowser);
        fileBrowser.setFileBrowserListener(this);
        fileBrowser.initFiles(new File(XFileUtils.getStorageCardPath()));
        // ListView listFiles = (ListView) view.findViewById(R.id.list_files);
    }

    void initializePictureView(View view) {
        PictureBrowserControl sgvPicture = (PictureBrowserControl) view.findViewById(R.id.sgv);
        sgvPicture.loadPicture();
    }

    void initializeAppView(View view) {
        AppBrowserControl sgvApps = (AppBrowserControl) view.findViewById(R.id.sgv);
        sgvApps.loadApps();
    }

    @Override
    public void onTabSelect(int index) {
        viewPager.setCurrentItem(index);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:
                EventBus.getDefault().post(new MyEvent("myevent"));
                break;
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
    public void onPageScrollStateChanged(int arg0) {


    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {


    }

    @Override
    public void onPageSelected(int index) {
        tabBar.setCurrentTab(index);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {

        StorageRootInfo storage = (StorageRootInfo) rootAdapter.getItem(index);
        if (storage != null) {
            fileBrowser.initRootPath(storage.getFilePath());
            fileBrowser.initFiles(new File(storage.getFilePath()));
            fileBrowser.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void rootDir() {

        fileBrowser.setVisibility(View.GONE);
    }

}
