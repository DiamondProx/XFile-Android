package com.huangjiang.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.huangjiang.XFileApp;
import com.huangjiang.activity.ConnectActivity;
import com.huangjiang.activity.HomeActivity;
import com.huangjiang.adapter.SearchAdapter;
import com.huangjiang.business.event.FindResEvent;
import com.huangjiang.business.event.OpFileEvent;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.business.opfile.OpLogic;
import com.huangjiang.business.video.VideoLogic;
import com.huangjiang.xfile.R;
import com.huangjiang.manager.IMFileManager;
import com.huangjiang.view.CustomDialog;
import com.huangjiang.view.DialogHelper;
import com.huangjiang.view.MenuHelper;
import com.huangjiang.view.MenuItem;
import com.huangjiang.view.OpenFileHelper;
import com.huangjiang.view.PopupMenu;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 视频
 */
public class VideoFragment extends Fragment implements PopupMenu.MenuCallback, CustomDialog.DialogCallback, AdapterView.OnItemClickListener {
    private final String mPageName = "VideoFragment";
    ListView listView;
    TextView titleName;
    SearchAdapter adapter;
    VideoLogic videoLogic;
    OpLogic opLogic;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.video_fragment, null);
        init(view);
        return view;
    }

    void init(View view) {
        EventBus.getDefault().register(this);
        titleName = (TextView) view.findViewById(R.id.headerName);
        titleName.setText(String.format(getString(R.string.local_video), "0"));
        listView = (ListView) view.findViewById(R.id.listview);
        adapter = new SearchAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        videoLogic = new VideoLogic(getActivity());
        opLogic = new OpLogic(getActivity());
        videoLogic.searchVideo();
    }

    /**
     * 弹出菜单选择
     */
    @Override
    public void onMenuClick(PopupMenu menu, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_transfer:
                if (XFileApp.connect_type == 0) {
                    startActivity(new Intent(getActivity(), ConnectActivity.class));
                    return;
                }
                ImageView image = (ImageView) listView.getChildAt(menu.getItemPosition()).findViewById(R.id.img);
                if (image != null) {
                    Drawable drawable = image.getDrawable();
                    HomeActivity homeActivity = (HomeActivity) getActivity();
                    int[] location = new int[2];
                    image.getLocationOnScreen(location);
                    homeActivity.initFileThumbView(drawable, image.getWidth(), image.getHeight(), location[0], location[1]);
                    TFileInfo tFileInfo = menu.getTFileInfo();
                    IMFileManager.getInstance().createTask(tFileInfo);
                }
                break;
            case R.id.menu_open:
                OpenFileHelper.openFile(getActivity(), menu.getTFileInfo());
                break;
            case R.id.menu_property:
                DialogHelper.showProperty(getActivity(), menu.getTFileInfo());
                break;
            case R.id.menu_more:
                DialogHelper.showMore(getActivity(), menu.getTFileInfo(), VideoFragment.this);
                break;
        }
    }

    /**
     * 弹出对话框选择事件
     */
    @Override
    public void onDialogClick(int id, TFileInfo tFileInfo, Object... param) {
        switch (id) {
            case R.id.more_del:
                DialogHelper.showDel(getActivity(), tFileInfo, VideoFragment.this);
                break;
            case R.id.more_rename:
                DialogHelper.showRename(getActivity(), tFileInfo, VideoFragment.this);
                break;
            case R.id.more_uninstall:
                opLogic.unInstall(tFileInfo);
                break;
            case R.id.more_back:
                opLogic.backUpApk(tFileInfo);
                break;
            case R.id.more_property:
                DialogHelper.showProperty(getActivity(), tFileInfo);
                break;
            case R.id.dialog_del_ok:
                opLogic.deleteFile(tFileInfo);
                break;
            case R.id.dialog_rename_ok:
                opLogic.renameFile(tFileInfo, (String) param[0]);
                break;
        }
    }

    /**
     * 查询数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FindResEvent searchEvent) {
        switch (searchEvent.getMimeType()) {
            case VIDEO:
                List<TFileInfo> list = searchEvent.getFileInfoList();
                if (list != null) {
                    adapter.setList(null);
                    adapter.setList(list);
                    adapter.notifyDataSetChanged();
                    titleName.setText(String.format(getString(R.string.local_video), String.valueOf(list.size())));
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
        if (!opFileEvent.isSuccess()) {
            return;
        }
        switch (opFileEvent.getOpType()) {
            case DELETE:
            case UNINSTALL:
                adapter.removeFile(opFileEvent.getTFileInfo());
                break;
            case RENAME:
                adapter.updateFile(opFileEvent.getTFileInfo());
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
        MenuHelper.showMenu(getActivity(), view, position, tFileInfo, VideoFragment.this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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
