package com.huangjiang.apkinfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;

import com.huangjiang.model.AppVO;

public class AppScanner {
	private Context mContext;

	public AppScanner(Context context) {
		this.mContext = context;
	}

	@SuppressLint("HandlerLeak")
	public void scanImages(final ScanAppsCompleteCallBack callback) {
		final Handler mHandler = new Handler() {

			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				callback.scanComplete((ArrayList<AppVO>) msg.obj);
			}
		};

		new Thread(new Runnable() {

			@Override
			public void run() {

				List<AppVO> list = new ArrayList<AppVO>();
				PackageManager pm = mContext.getPackageManager();
				//	获取读取已安装程序列表服务
				List<PackageInfo> packs = pm.getInstalledPackages(0);

				for (PackageInfo pi : packs) {
					AppVO appInfo = new AppVO();
					appInfo.setAppIcon(pi.applicationInfo.loadIcon(pm));//程序图标
					appInfo.setAppName(pi.applicationInfo.loadLabel(pm).toString());//程序名称
					appInfo.setPackageName(pi.applicationInfo.packageName);//包名
					int appSize= Integer.valueOf((int) new File(pi.applicationInfo.publicSourceDir).length()); 
					appInfo.setAppSize(appSize);
					list.add(appInfo);
				}
				if (list.size() > 0) {
					Message msg = mHandler.obtainMessage();
					msg.obj = list;
					mHandler.sendMessage(msg);
				}

			}
		}).start();

	}

	public static interface ScanAppsCompleteCallBack {
		public void scanComplete(List<AppVO> list);
	}
}
