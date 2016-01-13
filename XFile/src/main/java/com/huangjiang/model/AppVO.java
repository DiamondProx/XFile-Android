package com.huangjiang.model;

import com.huangjiang.utils.Utils;

import android.graphics.drawable.Drawable;

public class AppVO {

	public String appName;
	public Drawable appIcon;
	public String packageName;
	public int appSize;

	public int getAppSize() {
		return appSize;
	}

	public void setAppSize(int appSize) {
		this.appSize = appSize;
	}

	public String getAppSizeStr() {
		return Utils.getFolderSizeString(appSize);
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Drawable getAppIcon() {
		return appIcon;
	}

	public void setAppIcon(Drawable appIcon) {
		this.appIcon = appIcon;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}
