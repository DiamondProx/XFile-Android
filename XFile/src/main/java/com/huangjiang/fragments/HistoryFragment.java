package com.huangjiang.fragments;

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

import com.huangjiang.activity.HomeActivity;
import com.huangjiang.adapter.TransmitAdapter;
import com.huangjiang.business.event.FindResEvent;
import com.huangjiang.business.history.HistoryLogic;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.dao.DFileDao;
import com.huangjiang.dao.DaoMaster;
import com.huangjiang.filetransfer.R;
import com.huangjiang.manager.IMFileManager;
import com.huangjiang.manager.event.FileEvent;
import com.huangjiang.view.MenuHelper;
import com.huangjiang.view.MenuItem;
import com.huangjiang.view.OpenFileHelper;
import com.huangjiang.view.PopupMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 历史消息
 */
public class HistoryFragment extends Fragment implements AdapterView.OnItemClickListener, PopupMenu.MenuCallback {

    List<TFileInfo> list_message;
    TransmitAdapter adapter;
    ListView lv_message;
    HistoryLogic history_logic;
    DFileDao dFileDao;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_fragment, null);
        init(view);
        return view;
    }

    void init(View view) {
        EventBus.getDefault().register(this);
        lv_message = (ListView) view.findViewById(R.id.lv_message);
        adapter = new TransmitAdapter(getActivity());
        lv_message.setAdapter(adapter);
        lv_message.setOnItemClickListener(this);
        list_message = new ArrayList<>();
        history_logic = new HistoryLogic(getActivity());
        history_logic.getHistory();
        dFileDao = DaoMaster.getInstance().newSession().getDFileDao();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TFileInfo tFileInfo = (TFileInfo) adapter.getItem(position);
        MenuHelper.showMenu(getActivity(), view, position, tFileInfo, HistoryFragment.this);
    }

    @Override
    public void onMenuClick(PopupMenu menu, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_transfer:
                ImageView image = (ImageView) lv_message.getChildAt(menu.getItemPosition()).findViewById(R.id.img);
                if (image != null) {
                    Drawable drawable = image.getDrawable();
                    HomeActivity homeActivity = (HomeActivity) getActivity();
                    int[] location = new int[2];
                    image.getLocationOnScreen(location);
                    homeActivity.initFileThumbView(drawable, image.getWidth(), image.getHeight(), location[0], location[1]);
                }
                break;
            case R.id.menu_open:
                OpenFileHelper.openFile(getActivity(), menu.getTFileInfo());
                break;
            case R.id.menu_cancel:
                IMFileManager.getInstance().cancelTask(menu.getTFileInfo());
                break;
            case R.id.menu_delete:
                dFileDao.deleteByTaskId(menu.getTFileInfo().getTaskId());
                adapter.cancelTask(menu.getTFileInfo());
                adapter.notifyDataSetChanged();
                break;
            case R.id.menu_resume:
                IMFileManager.getInstance().resumeReceive(menu.getTFileInfo());
                break;
            case R.id.menu_stop:
                IMFileManager.getInstance().stopReceive();
                break;
        }
    }

    void updateTransmitState(TFileInfo tFileInfo) {
        int firstVisible = lv_message.getFirstVisiblePosition();
        int lastVisible = lv_message.getLastVisiblePosition();
        int position = adapter.getPosition(tFileInfo);
        if (position != -1) {
            TFileInfo currentFile = (TFileInfo) adapter.getItem(position);
            if (currentFile != null && position >= firstVisible && position <= lastVisible) {
                TransmitAdapter.ViewHolder holder = (TransmitAdapter.ViewHolder) (lv_message.getChildAt(position - firstVisible).getTag());
                adapter.updateTransmitState(holder, currentFile);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FindResEvent searchEvent) {
        switch (searchEvent.getMimeType()) {
            case HISTORY:
                List<TFileInfo> list = searchEvent.getFileInfoList();
                adapter.setList(list);
                adapter.notifyDataSetChanged();
                break;
        }
    }

    /*
     * 文件传输状态
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(TFileInfo tFileInfo) {

        FileEvent fileEvent = tFileInfo.getFileEvent();
        switch (fileEvent) {
            case CREATE_FILE_SUCCESS: {
                if (tFileInfo.isSend()) {
                    tFileInfo.setFrom(getString(R.string.send_to, tFileInfo.getFrom()));
                } else {
                    tFileInfo.setFrom(getString(R.string.receive_from, tFileInfo.getFrom()));
                }
                adapter.addTFileInfo(tFileInfo);
                adapter.notifyDataSetChanged();
            }
            break;
            case SET_FILE_SUCCESS: {
                adapter.updateTFileInfo(tFileInfo);
                updateTransmitState(tFileInfo);
            }
            break;
            case SET_FILE_FAILED:
            case SET_FILE:
            case SET_FILE_STOP:
            case WAITING:
                adapter.updateTFileInfo(tFileInfo);
                updateTransmitState(tFileInfo);
                break;
            case CANCEL_FILE:
                adapter.cancelTask(tFileInfo);
                adapter.notifyDataSetChanged();
                break;
        }
    }
}
