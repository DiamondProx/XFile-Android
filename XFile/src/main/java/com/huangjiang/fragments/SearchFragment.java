package com.huangjiang.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.huangjiang.activity.HomeActivity;
import com.huangjiang.business.event.FindResEvent;
import com.huangjiang.business.model.FileType;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.business.search.SearchLogic;
import com.huangjiang.core.ImageLoader;
import com.huangjiang.filetransfer.R;
import com.huangjiang.manager.IMFileManager;
import com.huangjiang.utils.ApkUtils;
import com.huangjiang.utils.XFileUtils;
import com.huangjiang.view.CustomDialog;
import com.huangjiang.view.DialogHelper;
import com.huangjiang.view.MenuHelper;
import com.huangjiang.view.MenuItem;
import com.huangjiang.view.OpenFileHelper;
import com.huangjiang.view.PopupMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 查找-图片,音频,视频三种类型
 */
public class SearchFragment extends Fragment implements PopupMenu.MenuCallback, View.OnClickListener, CustomDialog.DialogCallback {

    EditText edtSearch;
    ListView listView;
    SearchAdapter searchAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, null);
        edtSearch = (EditText) view.findViewById(R.id.edt_search);
        edtSearch.addTextChangedListener(new SearchKeyChange());
        listView = (ListView) view.findViewById(R.id.listview);
        searchAdapter = new SearchAdapter(getActivity());
        listView.setAdapter(searchAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TFileInfo tFileInfo = (TFileInfo) searchAdapter.getItem(position);
                MenuHelper.showMenu(getActivity(), view, position, tFileInfo, SearchFragment.this);
            }
        });
        EventBus.getDefault().register(this);
        return view;
    }


    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * 弹出菜单选择
     */
    @Override
    public void onMenuClick(PopupMenu menu, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_transfer:
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
                DialogHelper.showMore(getActivity(), menu.getTFileInfo(), SearchFragment.this);
                break;
            case R.id.menu_rename:

                break;
        }
    }

    /**
     * 弹出对话框选择事件
     */
    @Override
    public void onDialogClick(int id, TFileInfo tFileInfo) {
        switch (id) {
            case R.id.more_del:
                DialogHelper.showDel(getActivity(), tFileInfo, SearchFragment.this);
                break;
            case R.id.more_rename:
                DialogHelper.showRename(getActivity(), tFileInfo, SearchFragment.this);
                break;
            case R.id.more_uninstall:
                ApkUtils.unInstall(getActivity(), tFileInfo);
                break;
            case R.id.more_back:
                Toast.makeText(getActivity(), "点击了备份", Toast.LENGTH_SHORT).show();
                // TODO 直接备份
                break;
            case R.id.more_property:
                DialogHelper.showProperty(getActivity(), tFileInfo);
                break;
            case R.id.dialog_rename_ok:
                // TODO 执行重命名
                Toast.makeText(getActivity(), "确认重命名", Toast.LENGTH_SHORT).show();
                break;
            case R.id.dialog_del_ok:
                // TODO 执行删除
                Toast.makeText(getActivity(), "确认删除", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    class SearchAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<TFileInfo> mList;

        public SearchAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public void setList(List<TFileInfo> mList) {
            this.mList = mList;
        }

        @Override
        public int getCount() {
            return mList == null ? 0 : mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder videoHolder = null;
            if (convertView == null) {
                videoHolder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.listview_search_item, null);
                videoHolder.Img = (ImageView) convertView.findViewById(R.id.img);
                videoHolder.Name = (TextView) convertView.findViewById(R.id.name);
                videoHolder.Size = (TextView) convertView.findViewById(R.id.size);
                convertView.setTag(videoHolder);
            } else {
                videoHolder = (ViewHolder) convertView.getTag();
            }
            TFileInfo file = mList.get(position);
            if (file != null) {
                if (file.getFileType() == FileType.Apk || file.getFileType() == FileType.Video || file.getFileType() == FileType.Image) {
                    ImageLoader.getInstance().displayThumb(videoHolder.Img, file);
                } else {
                    videoHolder.Img.setImageResource(R.mipmap.data_folder_documents_placeholder);
                }
                videoHolder.Name.setText(file.getName());
                videoHolder.Size.setText(XFileUtils.getFolderSizeString(file.getLength()));
            }
            return convertView;
        }

        final class ViewHolder {
            ImageView Img;
            TextView Name;
            TextView Size;
        }
    }

    class SearchKeyChange implements TextWatcher {

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
                audioLogic.searchResource("XF");
            } else {
                searchAdapter.setList(null);
                searchAdapter.notifyDataSetChanged();
            }
        }
    }

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
}
