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
import com.huangjiang.model.StorageRootVO;
import com.huangjiang.utils.Utils;
import com.huangjiang.widgets.AppBrowserControl;
import com.huangjiang.widgets.FileBrowserControl;
import com.huangjiang.widgets.FileBrowserControl.FileBrowserListener;
import com.huangjiang.widgets.PictureBrowserControl;

import huangjiang.com.xfile_android.R;

public class TabMobileFragment extends Fragment implements OnPageChangeListener, OnItemClickListener, FileBrowserListener {

	List<View> list;
	ViewPager viewPager;
	StorageRootAdapter rootAdapter;
	FileBrowserControl fileBrowser;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_mobile, null);
		// LayoutInflater inflate = LayoutInflater.from(this.getActivity());
		View page1 = inflater.inflate(R.layout.page_search, null);
		View page2 = inflater.inflate(R.layout.page_root, null);
		View page3 = inflater.inflate(R.layout.page_picture, null);
		View page4 = inflater.inflate(R.layout.page_video, null);
		View page5 = inflater.inflate(R.layout.page_app, null);

		initializeRootView(page2);
		initializePictureView(page3);
		initializeAppView(page5);

		list = new ArrayList<View>();
		list.add(page1);
		list.add(page2);
		list.add(page3);
		list.add(page4);
		list.add(page5);

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
		List<StorageRootVO> list = new ArrayList<StorageRootVO>();

		// 根目录
		StorageRootVO rootMemory = new StorageRootVO();
		rootMemory.setImg(R.mipmap.data_folder_memory_default);
		rootMemory.setDesc(this.getString(R.string.root_memory_storage));
		rootMemory.setFilePath("/");
		list.add(rootMemory);

		// 内存
		if (Utils.ExistSDCard()) {
			StorageRootVO rootSdCard = new StorageRootVO();
			rootSdCard.setImg(R.mipmap.data_folder_sdcard_default);
			rootSdCard.setDesc(this.getString(R.string.root_sdcard_storage));
			rootSdCard.setFilePath(Utils.getStorageCardPath());
			list.add(rootSdCard);
		}

		// TF存储
		List<String> sdPaths = Utils.getSdCardPaths();
		for (String string : sdPaths) {
			StorageRootVO rootSdCardExten = new StorageRootVO();
			rootSdCardExten.setImg(R.mipmap.data_folder_sdcard_default);
			rootSdCardExten.setDesc(this.getString(R.string.root_sdcard_storage_exten));
			rootSdCardExten.setFilePath(string);
			list.add(rootSdCardExten);
		}

		rootAdapter.getRoots().addAll(list);
		rootAdapter.notifyDataSetChanged();

		fileBrowser = (FileBrowserControl) view.findViewById(R.id.fileBrowser);
		fileBrowser.setFileBrowserListener(this);
		fileBrowser.initFiles(new File(Utils.getStorageCardPath()));
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
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
		// TODO Auto-generated method stub
		StorageRootVO storage = (StorageRootVO) rootAdapter.getItem(index);
		if (storage != null) {
			fileBrowser.initRootPath(storage.getFilePath());
			fileBrowser.initFiles(new File(storage.getFilePath()));
			fileBrowser.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void rootDir() {
		// TODO Auto-generated method stub
		fileBrowser.setVisibility(View.GONE);
	}

}
