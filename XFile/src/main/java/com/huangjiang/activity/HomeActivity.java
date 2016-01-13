package com.huangjiang.activity;

import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.huangjiang.filetransfer.R;
import com.huangjiang.fragments.TabMessageFragment;
import com.huangjiang.fragments.TabMobileFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends FragmentActivity implements OnClickListener, OnCheckedChangeListener {

	private int cursorWidth; // 游标的长度
	private int offset; // 间隔
	private ImageView cursor;
	private Animation animation = null;
	private int originalIndex;

	private FragmentManager fragmentManager;
	private FragmentTransaction transaction;
	private RadioGroup radioGroup;

	// TabComputerFragment tabComputerFragment;
	TabMobileFragment tabMobileFragment;
	TabMessageFragment tabMessageFragment;

	private SlidingMenu slidingMenu = null;

	private int mTabindex;
	RadioButton rdb_home, rdb_userinfo;
	CheckBox cb;

	public List<Fragment> fragments = new ArrayList<Fragment>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		initializeView();
	}

	void initializeView() {
		// 设置抽屉菜单
		slidingMenu = new SlidingMenu(this);
		slidingMenu.setMode(SlidingMenu.RIGHT);
		// 触摸边界拖出菜单
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setMenu(R.layout.slidingmenu_right);
		slidingMenu.setSecondaryMenu(R.layout.slidingmenu_right);
		slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		// 将抽屉菜单与主页面关联起来
		slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		slidingMenu.setShadowWidth(10);
		// 测试代码
		findViewById(R.id.btn_right).setOnClickListener(this);

		// 选择按钮列表
		radioGroup = (RadioGroup) findViewById(R.id.rg_tab);
		radioGroup.setOnCheckedChangeListener(this);
		// 单选按钮
		// rdb_curriculum = (RadioButton) findViewById(R.id.rdb_tabcurriculum);
		rdb_home = (RadioButton) findViewById(R.id.rdb_tabhome);
		rdb_userinfo = (RadioButton) findViewById(R.id.rdb_tabuserinfo);

		// 初始化标签
		fragmentManager = getSupportFragmentManager();

		// tabComputerFragment = new TabComputerFragment();
		tabMobileFragment = new TabMobileFragment();
		tabMessageFragment = new TabMessageFragment();

		// fragments.add(tabComputerFragment);
		fragments.add(tabMobileFragment);
		fragments.add(tabMessageFragment);

		initCursor(fragments.size());

		rdb_home.setChecked(true);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_right:
			slidingMenu.showSecondaryMenu();
			break;
		default:
			break;
		}

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkId) {
		// TODO Auto-generated method stub
		// 切换标签
		for (int i = 0; i < group.getChildCount(); i++) {
			if (group.getChildAt(i).getId() == checkId) {
				Fragment fragment = fragments.get(i);
				transaction = fragmentManager.beginTransaction();
				getCurrentFragment().onResume();
				if (fragment.isAdded()) {
					fragment.onResume(); // 启动目标tab的onResume()
				} else {
					transaction.add(R.id.content, fragment);
				}
				showTab(i, transaction);
				transaction.commit();
				mTabindex = i;
			}

		}

		// 箭头切换

		int one = 2 * offset + cursorWidth;
		switch (originalIndex) {
		case 0:
			if (mTabindex == 1) {
				animation = new TranslateAnimation(0, one, 0, 0);
			}
			break;
		case 1:
			if (mTabindex == 0) {
				animation = new TranslateAnimation(one, 0, 0, 0);
			}
			break;
		}
		if (originalIndex != mTabindex) {
			animation.setFillAfter(true);
			animation.setDuration(100);
			cursor.startAnimation(animation);
			originalIndex = mTabindex;
		}
		switch (mTabindex) {
		case 0:
			cursor.setImageResource(R.mipmap.tab_mobile_arrow_down_blue);
			break;
		case 1:
			cursor.setImageResource(R.mipmap.tab_computer_arrow_down_green);
			break;

		default:
			break;
		}

	}

	public Fragment getCurrentFragment() {
		return fragments.get(mTabindex);
	}

	private void showTab(int idx, FragmentTransaction ft) {
		for (int i = 0; i < fragments.size(); i++) {
			Fragment fragment = fragments.get(i);
			if (idx == i) {
				ft.show(fragment);
			} else {
				ft.hide(fragment);
			}
		}
		mTabindex = idx; // 更新目标tab为当前tab
	}

	/**
	 * 根据tagd的数量初始化游标的位置
	 * 
	 * @param tagNum
	 */
	public void initCursor(int tagNum) {
		cursorWidth = BitmapFactory.decodeResource(getResources(), R.mipmap.tab_mobile_arrow_down_blue).getWidth();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		offset = ((dm.widthPixels / tagNum) - cursorWidth) / 2;

		cursor = (ImageView) findViewById(R.id.ivCursor);
		Matrix matrix = new Matrix();
		matrix.setTranslate(offset, 0);
		cursor.setImageMatrix(matrix);
	}

}
