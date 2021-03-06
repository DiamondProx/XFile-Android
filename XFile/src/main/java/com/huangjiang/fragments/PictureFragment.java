package com.huangjiang.fragments;

import android.os.Bundle;
import android.os.Handler;
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

import com.huangjiang.activity.HomeActivity;
import com.huangjiang.adapter.PictureAdapter;
import com.huangjiang.business.event.FindResEvent;
import com.huangjiang.business.event.OpFileEvent;
import com.huangjiang.business.image.ImageLogic;
import com.huangjiang.business.model.FileType;
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
 * 图片资源
 */
public class PictureFragment extends Fragment implements PictureAdapter.CallBack, PopupMenu.MenuCallback, CustomDialog.DialogCallback {
    private final String mPageName = "PictureFragment";
    RecyclerView recycler_view;
    PictureAdapter adapter;
    ImageLogic imageLogic;
    OpLogic opLogic;
    GridLayoutManager manager;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.picture_fragment, null);
        EventBus.getDefault().register(this);
        recycler_view = (RecyclerView) view.findViewById(R.id.recycler_view);
        manager = new GridLayoutManager(getActivity(), 4);
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
        opLogic = new OpLogic(getActivity());
        imageLogic.searchImage();
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
    public void onGridItemClick(View view, int position, TFileInfo tFileInfo) {
        MenuHelper.showMenu(getActivity(), view, position, tFileInfo, PictureFragment.this);
    }

    @Override
    public void onDialogClick(int id, TFileInfo tFileInfo, Object... params) {
        switch (id) {
            case R.id.more_del:
                DialogHelper.confirmDel(getActivity(), tFileInfo, PictureFragment.this);
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
            case R.id.dialog_confirm_ok:
                opLogic.deleteFile(tFileInfo);
                break;
            case R.id.dialog_rename_ok:
                opLogic.renameFile(tFileInfo, (String) params[0], OpFileEvent.Target.PICTURE_FRAGMENT);
                break;
        }
    }

    @Override
    public void onMenuClick(PopupMenu menu, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_transfer:
                int firstPos = manager.findFirstVisibleItemPosition();
                int lastPos = manager.findLastVisibleItemPosition();
                if (menu.getItemPosition() >= firstPos && menu.getItemPosition() <= lastPos) {
                    View view = recycler_view.getChildAt(menu.getItemPosition() - firstPos);
                    if (view != null) {
                        ImageView thumb = (ImageView) view.findViewById(R.id.item_image);
                        if (thumb != null && getActivity() instanceof HomeActivity) {
                            HomeActivity homeActivity = (HomeActivity) getActivity();
                            homeActivity.sendTFile(menu.getTFileInfo(), thumb);
                        }
                    }
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
        switch (opFileEvent.getOpType()) {
            case DELETE:
                if (opFileEvent.isSuccess()) {
                    adapter.removeFile(opFileEvent.getTFileInfo());
                    adapter.notifyDataSetChanged();
                }
                break;
            case RENAME:
                if (opFileEvent.getTarget() == OpFileEvent.Target.PICTURE_FRAGMENT) {
                    if (!opFileEvent.isSuccess()) {
                        Toast.makeText(getActivity(), opFileEvent.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    adapter.updateFile(opFileEvent.getTFileInfo());
                    adapter.notifyDataSetChanged();
                }
                break;
            case CHANGE:
                if (opFileEvent.getFileType() == FileType.Image && opFileEvent.getTarget() != OpFileEvent.Target.PICTURE_FRAGMENT) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imageLogic.searchImage();
                        }
                    }, 500);
                }
                break;
        }

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
