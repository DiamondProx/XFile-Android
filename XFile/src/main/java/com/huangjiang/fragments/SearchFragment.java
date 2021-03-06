package com.huangjiang.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.huangjiang.activity.HomeActivity;
import com.huangjiang.adapter.SearchAdapter;
import com.huangjiang.business.event.FindResEvent;
import com.huangjiang.business.event.OpFileEvent;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.business.opfile.OpLogic;
import com.huangjiang.business.search.SearchLogic;
import com.huangjiang.utils.SoftKeyboardUtils;
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
 * 查找-图片,音频,视频三种类型
 */
public class SearchFragment extends Fragment implements PopupMenu.MenuCallback, CustomDialog.DialogCallback, AdapterView.OnItemClickListener {
    private final String mPageName = "SearchFragment";
    EditText edtSearch;
    ListView listView;
    SearchAdapter searchAdapter;
    OpLogic opLogic;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, null);
        EventBus.getDefault().register(this);
        opLogic = new OpLogic(getActivity());
        edtSearch = (EditText) view.findViewById(R.id.edt_search);
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    SearchLogic audioLogic = new SearchLogic(getActivity());
                    audioLogic.searchResource(s.toString());
                } else {
                    searchAdapter.setList(null);
                    searchAdapter.notifyDataSetChanged();
                }
            }
        });
        listView = (ListView) view.findViewById(R.id.listview);
        searchAdapter = new SearchAdapter(getActivity());
        listView.setAdapter(searchAdapter);
        listView.setOnItemClickListener(this);
        return view;
    }


    /**
     * 弹出菜单选择
     */
    @Override
    public void onMenuClick(PopupMenu menu, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_transfer:
                ImageView image = (ImageView) listView.getChildAt(menu.getItemPosition() - listView.getFirstVisiblePosition()).findViewById(R.id.img);
                if (image != null && getActivity() instanceof HomeActivity) {
                    HomeActivity homeActivity = (HomeActivity) getActivity();
                    homeActivity.sendTFile(menu.getTFileInfo(), image);
                }
                break;
            case R.id.menu_open:
                OpenFileHelper.openFile(getActivity(), menu.getTFileInfo());
                break;
            case R.id.menu_property:
                DialogHelper.showProperty(getActivity(), menu.getTFileInfo());
                break;
            case R.id.menu_more:
                DialogHelper.showMore(getActivity(), menu.getTFileInfo(), SearchFragment.this);
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
                DialogHelper.confirmDel(getActivity(), tFileInfo, SearchFragment.this);
                break;
            case R.id.more_rename:
                DialogHelper.showRename(getActivity(), tFileInfo, SearchFragment.this);
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
                opLogic.renameFile(tFileInfo, (String) param[0], OpFileEvent.Target.SEARCH_FRAGMENT);
                break;
        }
    }

    /**
     * 查询数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FindResEvent searchEvent) {
        switch (searchEvent.getMimeType()) {
            case SEARCH:
                List<TFileInfo> list = searchEvent.getFileInfoList();
                if (list != null) {
                    searchAdapter.setList(null);
                    searchAdapter.setList(list);
                    searchAdapter.notifyDataSetChanged();
                } else {
                    searchAdapter.setList(null);
                    searchAdapter.notifyDataSetChanged();
                }
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
            case UNINSTALL:
                if (opFileEvent.isSuccess()) {
                    searchAdapter.removeFile(opFileEvent.getTFileInfo());
                    searchAdapter.notifyDataSetChanged();
                }
                break;
            case RENAME:
                if (opFileEvent.getTarget() == OpFileEvent.Target.SEARCH_FRAGMENT) {
                    if (!opFileEvent.isSuccess()) {
                        Toast.makeText(getActivity(), opFileEvent.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    searchAdapter.updateFile(opFileEvent.getTFileInfo());
                    searchAdapter.notifyDataSetChanged();
                }
                break;
            case BACKUP:
                Toast.makeText(getActivity(), R.string.backup_success, Toast.LENGTH_SHORT).show();
                break;
        }

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SoftKeyboardUtils.hiddenSoftKeyboard(getActivity(), edtSearch);
        TFileInfo tFileInfo = (TFileInfo) searchAdapter.getItem(position);
        MenuHelper.showMenu(getActivity(), view, position, tFileInfo, SearchFragment.this);
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
