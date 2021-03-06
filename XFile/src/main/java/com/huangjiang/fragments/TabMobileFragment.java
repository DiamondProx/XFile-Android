package com.huangjiang.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huangjiang.adapter.MobilePagerAdapter;
import com.huangjiang.view.TabBar;
import com.huangjiang.xfile.R;
import com.umeng.analytics.MobclickAgent;

public class TabMobileFragment extends Fragment implements OnPageChangeListener, TabBar.OnTabListener, View.OnClickListener {
    private final String mPageName = "TabMobileFragment";
    ViewPager viewPager;
    TabBar tabBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mobile, null);
        tabBar = (TabBar) view.findViewById(R.id.tab_mobile);
        tabBar.setListener(this);
        tabBar.setMenu(R.mipmap.common_tab_refresh_white, R.string.mobile_all, R.string.picture, R.string.music, R.string.video, R.string.application);
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new MobilePagerAdapter(getChildFragmentManager()));
        viewPager.setOffscreenPageLimit(5);
        viewPager.setOnPageChangeListener(this);
        viewPager.setCurrentItem(1);
        return view;
    }

    @Override
    public void onTabSelect(int index) {
        viewPager.setCurrentItem(index);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int index) {
        tabBar.setCurrentTab(index);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

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
