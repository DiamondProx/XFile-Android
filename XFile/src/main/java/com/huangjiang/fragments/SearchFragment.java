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

import com.huangjiang.activity.HomeActivity;
import com.huangjiang.business.audio.AudioInterface;
import com.huangjiang.business.model.FileInfo;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.core.ResponseCallback;
import com.huangjiang.filetransfer.R;
import com.huangjiang.view.MenuItem;
import com.huangjiang.view.PopupMenu;

import java.util.List;

/**
 * 查找
 */
public class SearchFragment extends Fragment implements PopupMenu.OnItemSelectedListener {

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

                PopupMenu menu = new PopupMenu(getActivity());
                menu.setOnItemSelectedListener(SearchFragment.this);
                menu.setOutPosition(position);
                menu.add(0, R.string.transfer).setIcon(getResources().getDrawable(R.mipmap.data_downmenu_send));
                menu.add(1, R.string.play).setIcon(getResources().getDrawable(R.mipmap.data_downmenu_open));
                menu.add(2, R.string.more).setIcon(getResources().getDrawable(R.mipmap.data_downmenu_more));
                menu.show(view);


            }
        });
        return view;
    }

    @Override
    public void onItemSelected(int position, MenuItem item, TFileInfo tFileInfo) {
        ImageView image = (ImageView) listView.getChildAt(position).findViewById(R.id.img);
        if (image != null) {
            Drawable drawable = image.getDrawable();
            HomeActivity homeActivity = (HomeActivity) getActivity();
            int[] location = new int[2];
            image.getLocationOnScreen(location);
            homeActivity.initFileThumbView(drawable, image.getWidth(), image.getHeight(), location[0], location[1]);
        }
    }

    class SearchAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<FileInfo> mList;

        public SearchAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public void setList(List<FileInfo> mList) {
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
            FileInfo file = mList.get(position);
            if (file != null) {
                videoHolder.Img.setImageResource(R.mipmap.data_folder_documents_placeholder);
                videoHolder.Name.setText(file.getName());
                videoHolder.Size.setText(file.getSpace());
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
                AudioInterface audioInterface = new AudioInterface(getActivity());
                audioInterface.getAudioByKey("doufu", new ResponseCallback<List<FileInfo>>() {
                    @Override
                    public void onResponse(int stateCode, int code, String msg, List<FileInfo> fileInfos) {
                        if (fileInfos != null) {
                            searchAdapter.setList(null);
                            searchAdapter.setList(fileInfos);
                            searchAdapter.notifyDataSetChanged();
                        } else {
                            searchAdapter.setList(null);
                            searchAdapter.notifyDataSetChanged();
                        }
                    }
                });
            } else {
                searchAdapter.setList(null);
                searchAdapter.notifyDataSetChanged();
            }
        }
    }
}
