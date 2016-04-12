package com.huangjiang.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.huangjiang.fragments.AppFragment;
import com.huangjiang.fragments.ExplorerFragment;
import com.huangjiang.fragments.MusicFragment;
import com.huangjiang.fragments.PictureFragment;
import com.huangjiang.fragments.SearchFragment;
import com.huangjiang.fragments.VideoFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息Fragment适配器
 */
public class MobilePagerAdapter extends FragmentStatePagerAdapter {


    private List<Fragment> fragments = new ArrayList<>();

    public MobilePagerAdapter(FragmentManager fm) {
        super(fm);
        fragments.add(new SearchFragment());
        fragments.add(new ExplorerFragment());
        fragments.add(new PictureFragment());
        fragments.add(new MusicFragment());
        fragments.add(new VideoFragment());
        fragments.add(new AppFragment());
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
