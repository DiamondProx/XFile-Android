package com.huangjiang.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huangjiang.activity.HomeActivity;
import com.huangjiang.adapter.InstallAdapter;
import com.huangjiang.business.app.AppLogic;
import com.huangjiang.business.event.FindResEvent;
import com.huangjiang.business.event.OpFileEvent;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.business.opfile.OpLogic;
import com.huangjiang.view.CustomDialog;
import com.huangjiang.view.DialogHelper;
import com.huangjiang.view.MenuHelper;
import com.huangjiang.view.MenuItem;
import com.huangjiang.view.OpenFileHelper;
import com.huangjiang.view.PopupMenu;
import com.huangjiang.xfile.R;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 安装程序
 */
public class AppFragment extends Fragment implements PopupMenu.MenuCallback, CustomDialog.DialogCallback, AdapterView.OnItemClickListener {

    private final String mPageName = "AppFragment";

    InstallAdapter adapter;
    TextView titleName;
    GridView gridView;
    AppLogic appLogic;
    OpLogic opLogic;
    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_fragment, null);
        init(view);
        return view;
    }

    void init(View view) {
        titleName = (TextView) view.findViewById(R.id.headerName);
        titleName.setText(String.format(getString(R.string.local_app), "0"));
        gridView = (GridView) view.findViewById(R.id.gridView);
        adapter = new InstallAdapter(getActivity());
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
        EventBus.getDefault().register(this);
        appLogic = new AppLogic(getActivity());
        opLogic = new OpLogic(getActivity());
        appLogic.searchApp();
    }

    /**
     * 弹出菜单选择
     */
    @Override
    public void onMenuClick(PopupMenu menu, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_transfer:
                ImageView image = (ImageView) gridView.getChildAt(menu.getItemPosition() - gridView.getFirstVisiblePosition()).findViewById(R.id.image);
                if (image != null && getActivity() instanceof HomeActivity) {
                    HomeActivity homeActivity = (HomeActivity) getActivity();
                    homeActivity.sendTFile(menu.getTFileInfo(), image);
                }
                break;
            case R.id.menu_property:
                DialogHelper.showProperty(getActivity(), menu.getTFileInfo());
                break;
            case R.id.menu_backup:
                backUpApk(menu.getTFileInfo());
                break;
            case R.id.menu_open:
                OpenFileHelper.openFile(getActivity(), menu.getTFileInfo());
                break;
            case R.id.menu_more:
                DialogHelper.showMore(getActivity(), menu.getTFileInfo(), AppFragment.this);
                break;
        }
    }

    /**
     * 弹出对话框选择事件
     */
    @Override
    public void onDialogClick(int id, TFileInfo tFileInfo, Object... param) {
        switch (id) {
            case R.id.more_uninstall:
                opLogic.unInstall(tFileInfo);
                break;
            case R.id.more_back:
                backUpApk(tFileInfo);
                break;
            case R.id.more_property:
                DialogHelper.showProperty(getActivity(), tFileInfo);
                break;
        }
    }

    void backUpApk(TFileInfo tFileInfo) {
        progressDialog = ProgressDialog.show(getActivity(), getString(R.string.progress_title), getString(R.string.backup_apk));
        progressDialog.show();
        opLogic.backUpApk(tFileInfo);
    }

    /**
     * 查询数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FindResEvent searchEvent) {
        switch (searchEvent.getMimeType()) {
            case APK:
                List<TFileInfo> list = searchEvent.getFileInfoList();
                if (list != null) {
                    adapter.setList(list);
                    adapter.notifyDataSetChanged();
                    titleName.setText(String.format(getString(R.string.local_app), String.valueOf(list.size())));
                } else {
                    adapter.setList(null);
                    adapter.notifyDataSetChanged();
                }
                break;
        }

    }

    /**
     * 文件操作
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OpFileEvent opFileEvent) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (!opFileEvent.isSuccess()) {
            return;
        }
        switch (opFileEvent.getOpType()) {
            case DELETE:
            case UNINSTALL:
                adapter.removeFile(opFileEvent.getTFileInfo());
                titleName.setText(String.format(getString(R.string.local_app), String.valueOf(adapter.getCount())));
                break;
            case BACKUP:
                Toast.makeText(getActivity(), R.string.backup_success, Toast.LENGTH_SHORT).show();
                break;
        }
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TFileInfo tFileInfo = (TFileInfo) adapter.getItem(position);
        MenuHelper.showMenu(getActivity(), view, position, tFileInfo, AppFragment.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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
