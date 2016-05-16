package com.huangjiang.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.huangjiang.fragments.HistoryFragment;
import com.huangjiang.fragments.InboxFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息Fragment适配器
 */
public class MessagePagerAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragments = new ArrayList<>();

    public MessagePagerAdapter(FragmentManager fm) {
        super(fm);
        fragments.add(new HistoryFragment());
        fragments.add(new InboxFragment());
    }


    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
