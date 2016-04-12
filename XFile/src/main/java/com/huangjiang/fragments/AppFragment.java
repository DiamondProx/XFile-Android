package com.huangjiang.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huangjiang.filetransfer.R;
import com.huangjiang.view.AppBrowserControl;

/**
 * 安装程序
 */
public class AppFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_fragment, null);
        AppBrowserControl sgvApps = (AppBrowserControl) view.findViewById(R.id.sgv);
        sgvApps.loadApps();
        return view;
    }
}
