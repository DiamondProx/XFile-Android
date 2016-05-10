package com.huangjiang.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huangjiang.adapter.MessagePagerAdapter;
import com.huangjiang.business.history.HistoryLogic;
import com.huangjiang.manager.IMFileManager;
import com.huangjiang.utils.Logger;
import com.huangjiang.view.TabBar;
import com.huangjiang.xfile.R;
import com.umeng.analytics.MobclickAgent;

public class TabMessageFragment extends Fragment implements TabBar.OnTabListener, ViewPager.OnPageChangeListener, View.OnClickListener {

    private Logger logger = Logger.getLogger(IMFileManager.class);
    private final String mPageName = "TabMessageFragment";
    ViewPager viewPager;
    MessagePagerAdapter pagerAdapter;
    TabBar tabBar;
    TextView txt_disk_status, txt_clear;
    HistoryLogic historyLogic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_message, null);
        txt_disk_status = (TextView) view.findViewById(R.id.txt_dis_status);
        txt_clear = (TextView) view.findViewById(R.id.txt_clear);
        txt_clear.setOnClickListener(this);
        txt_clear.setVisibility(View.GONE);
        tabBar = (TabBar) view.findViewById(R.id.tab_mobile);
        tabBar.setListener(this);
        tabBar.setTabBackground(R.color.tab_green);
        tabBar.setTabSelectBackground(R.color.tab_green_select);
        tabBar.setMenu(R.string.history_message, R.string.inbox);
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        pagerAdapter = new MessagePagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(this);
        viewPager.setCurrentItem(1);
        txt_disk_status.setText(String.format(getString(R.string.disk_status), "10.79GB", "12.34BG"));
        historyLogic = new HistoryLogic(getActivity());
        return view;

    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        tabBar.setCurrentTab(position);
        if (position == 0) {
            txt_clear.setVisibility(View.GONE);
        } else {
            txt_clear.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_clear:
                Fragment fragment = pagerAdapter.getItem(0);
                if (fragment != null && fragment instanceof HistoryFragment && ((HistoryFragment) fragment).getHistoryCount() > 0) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.progress_title)
                            .setMessage(R.string.clear_all_history_comfirm)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    historyLogic.delAllHistory();
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                }

                break;
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
