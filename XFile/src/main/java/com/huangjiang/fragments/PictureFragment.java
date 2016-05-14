package com.huangjiang.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.huangjiang.XFileApp;
import com.huangjiang.activity.ConnectActivity;
import com.huangjiang.activity.HomeActivity;
import com.huangjiang.adapter.PictureAdapter;
import com.huangjiang.business.event.FindResEvent;
import com.huangjiang.business.event.OpFileEvent;
import com.huangjiang.business.image.ImageLogic;
import com.huangjiang.business.model.LinkType;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.business.opfile.OpLogic;
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
 * 图片资源
 */
public class PictureFragment extends Fragment implements PictureAdapter.CallBack, PopupMenu.MenuCallback, CustomDialog.DialogCallback {
    private final String mPageName = "PictureFragment";
    RecyclerView recycler_view;
    PictureAdapter adapter;
    ImageLogic imageLogic;
    OpLogic opLogic;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.picture_fragment, null);
        EventBus.getDefault().register(this);
        recycler_view = (RecyclerView) view.findViewById(R.id.recycler_view);
        final GridLayoutManager manager = new GridLayoutManager(getActivity(), 4);
        recycler_view.setLayoutManager(manager);
        recycler_view.setHasFixedSize(true);
        adapter = new PictureAdapter(getActivity());
        adapter.setCallBack(PictureFragment.this);
        recycler_view.setAdapter(adapter);
        recycler_view.setItemAnimator(new DefaultItemAnimator());
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemViewType(position) == PictureAdapter.ITEM_VIEW_TYPE_HEADER ? manager.getSpanCount() : 1;
            }
        });
        imageLogic = new ImageLogic(getActivity());
        imageLogic.searchImage();
        opLogic = new OpLogic(getActivity());
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 查询数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FindResEvent searchEvent) {
        switch (searchEvent.getMimeType()) {
            case IMAGE:
                List<TFileInfo> list = searchEvent.getFileInfoList();
                if (list != null) {
                    adapter.setPictures(list);
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.setPictures(null);
                    adapter.notifyDataSetChanged();
                }
                break;
        }

    }

    @Override
    public void onGridItemClick(View view,int position ,TFileInfo tFileInfo) {
        MenuHelper.showMenu(getActivity(), view, position, tFileInfo, PictureFragment.this);
    }

    @Override
    public void onDialogClick(int id, TFileInfo tFileInfo, Object... params) {
        switch (id) {
            case R.id.more_del:
                DialogHelper.showDel(getActivity(), tFileInfo, PictureFragment.this);
                break;
            case R.id.more_rename:
                DialogHelper.showRename(getActivity(), tFileInfo, PictureFragment.this);
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
                opLogic.renameFile(tFileInfo, (String) params[0]);
                break;
        }
    }

    @Override
    public void onMenuClick(PopupMenu menu, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_transfer:
                if (XFileApp.mLinkType == LinkType.NONE) {
                    startActivity(new Intent(getActivity(), ConnectActivity.class));
                    return;
                }
                ImageView image = (ImageView) recycler_view.getChildAt(menu.getItemPosition()).findViewById(R.id.item_image);
                if (image != null) {
                    Drawable drawable = image.getDrawable();
                    HomeActivity homeActivity = (HomeActivity) getActivity();
                    int[] location = new int[2];
                    image.getLocationOnScreen(location);
                    homeActivity.setThrowView(drawable, image.getWidth(), image.getHeight(), location[0], location[1]);
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
                DialogHelper.showMore(getActivity(), menu.getTFileInfo(), PictureFragment.this);
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
