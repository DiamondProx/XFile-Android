package com.huangjiang.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.huangjiang.adapter.TransmitAdapter;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.filetransfer.R;
import com.huangjiang.manager.IMFileManager;
import com.huangjiang.manager.event.FileEvent;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.XFileUtils;
import com.huangjiang.view.MenuItem;
import com.huangjiang.view.PopupMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 历史消息
 */
public class HistoryFragment extends Fragment implements AdapterView.OnItemClickListener, PopupMenu.OnItemSelectedListener {

    List<TFileInfo> listMessage;
    TransmitAdapter adapter;
    ListView lv_message;

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
        listMessage = new ArrayList<>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TFileInfo tFileInfo = listMessage.get(position);
        PopupMenu menu = new PopupMenu(getActivity());
        // Set Listener
        menu.setOnItemSelectedListener(HistoryFragment.this);
        menu.setTFileInfo(tFileInfo);
        // Add Menu (Android menu like style)
        menu.add(0, R.string.transfer).setIcon(getResources().getDrawable(R.mipmap.data_downmenu_send));
        menu.add(1, R.string.multi_select).setIcon(getResources().getDrawable(R.mipmap.data_downmenu_check));
        menu.add(2, R.string.play).setIcon(getResources().getDrawable(R.mipmap.data_downmenu_open));
        menu.add(3, R.string.more).setIcon(getResources().getDrawable(R.mipmap.data_downmenu_more));
        menu.show(view);
    }

    @Override
    public void onItemSelected(int position, MenuItem item, TFileInfo tFileInfo) {
        if (tFileInfo != null) {
            Toast.makeText(getActivity(), "clickMemu", Toast.LENGTH_SHORT).show();
            IMFileManager.getInstance().cancelTask(tFileInfo);
        }
    }

    void updateTransmitState(TFileInfo tFileInfo) {
        int firstVisible = lv_message.getFirstVisiblePosition();
        int lastVisible = lv_message.getLastVisiblePosition();
        int position = adapter.getPosition(tFileInfo);
        TFileInfo currentFile = (TFileInfo) adapter.getItem(position);
        if (currentFile != null && position >= firstVisible && position <= lastVisible) {
            TransmitAdapter.ViewHolder holder = (TransmitAdapter.ViewHolder) (lv_message.getChildAt(position - firstVisible).getTag());
            adapter.updateTransmitState(holder, currentFile);
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
                if (tFileInfo.isSend()) {
                    // 发送完成之后查看是否有等待的任务
                    TFileInfo waitFile = adapter.getWaitFile();
                    if (waitFile != null) {
                        XFileProtocol.File reqFile = XFileUtils.buildSendFile(waitFile);
                        IMFileManager.getInstance().checkTask(reqFile);
                    }
                }

            }
            break;
            case SET_FILE:
            case SET_FILE_STOP:
            case WAITING:
                adapter.updateTFileInfo(tFileInfo);
                updateTransmitState(tFileInfo);
                break;
            case CANCEL_FILE:
                adapter.cancelTask(tFileInfo);
                updateTransmitState(tFileInfo);
                break;
        }
    }
}
