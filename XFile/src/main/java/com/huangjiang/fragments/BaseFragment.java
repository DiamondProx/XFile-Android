package com.huangjiang.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.util.List;


public class BaseFragment extends Fragment {

    private boolean waitingShowToUser;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 如果自己是显示状态，但父Fragment却是隐藏状态，就把自己也改为隐藏状态，并且设置一个等待显示标记
        if (getUserVisibleHint()) {
            Fragment parentFragment = getParentFragment();
            if (parentFragment != null && !parentFragment.getUserVisibleHint()) {
                waitingShowToUser = true;
                super.setUserVisibleHint(false);
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (getActivity() != null) {
            List<Fragment> childFragmentList = getChildFragmentManager().getFragments();
            if (isVisibleToUser) {
                // 将所有正等待显示的子Fragment设置为显示状态，并取消等待显示标记
                if (childFragmentList != null && childFragmentList.size() > 0) {
                    for (Fragment childFragment : childFragmentList) {
                        if (childFragment instanceof BaseFragment) {
                            BaseFragment childBaseFragment = (BaseFragment) childFragment;
                            if (childBaseFragment.isWaitingShowToUser()) {
                                childBaseFragment.setWaitingShowToUser(false);
                                childFragment.setUserVisibleHint(true);
                            }
                        }
                    }
                }
            } else {
                // 将所有正在显示的子Fragment设置为隐藏状态，并设置一个等待显示标记
                if (childFragmentList != null && childFragmentList.size() > 0) {
                    for (Fragment childFragment : childFragmentList) {
                        if (childFragment instanceof BaseFragment) {
                            BaseFragment childBaseFragment = (BaseFragment) childFragment;
                            if (childFragment.getUserVisibleHint()) {
                                childBaseFragment.setWaitingShowToUser(true);
                                childFragment.setUserVisibleHint(false);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isWaitingShowToUser() {
        return waitingShowToUser;
    }

    public void setWaitingShowToUser(boolean waitingShowToUser) {
        this.waitingShowToUser = waitingShowToUser;
    }
}
