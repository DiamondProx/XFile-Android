package com.huangjiang.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huangjiang.filetransfer.R;

public class TabMessageFragment extends Fragment {

	ViewPager viewPager;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_message, null);
		viewPager=(ViewPager)view.findViewById(R.id.viewPager);
		return view;
	}
	

}
