package com.huangjiang.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.huangjiang.adapter.StorageRootAdapter;
import com.huangjiang.business.model.StorageRootInfo;
import com.huangjiang.filetransfer.R;
import com.huangjiang.utils.XFileUtils;
import com.huangjiang.view.FileBrowserControl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件浏览
 */
public class ExplorerFragment extends Fragment implements AdapterView.OnItemClickListener, FileBrowserControl.FileBrowserListener {

    StorageRootAdapter rootAdapter;
    FileBrowserControl fileBrowser;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.explorer_fragment, null);
        init(view);
        return view;
    }

    void init(View view) {
        ListView listRoot = (ListView) view.findViewById(R.id.list_root);
        rootAdapter = new StorageRootAdapter(getActivity());
        listRoot.setAdapter(rootAdapter);
        listRoot.setOnItemClickListener(this);
        List<StorageRootInfo> list = new ArrayList<>();

        // 根目录
        StorageRootInfo rootMemory = new StorageRootInfo();
        rootMemory.setImg(R.mipmap.data_folder_memory_default);
        rootMemory.setDesc(this.getString(R.string.root_memory_storage));
        rootMemory.setFilePath("/");
        list.add(rootMemory);

        // 内存
        if (XFileUtils.ExistSDCard()) {
            StorageRootInfo rootSdCard = new StorageRootInfo();
            rootSdCard.setImg(R.mipmap.data_folder_sdcard_default);
            rootSdCard.setDesc(this.getString(R.string.root_sdcard_storage));
            rootSdCard.setFilePath(XFileUtils.getStorageCardPath());
            list.add(rootSdCard);
        }

        // TF存储
        List<String> sdPaths = XFileUtils.getSdCardPaths();
        for (String string : sdPaths) {
            StorageRootInfo rootSdCardExten = new StorageRootInfo();
            rootSdCardExten.setImg(R.mipmap.data_folder_sdcard_default);
            rootSdCardExten.setDesc(this.getString(R.string.root_sdcard_storage_exten));
            rootSdCardExten.setFilePath(string);
            list.add(rootSdCardExten);
        }

        rootAdapter.getRoots().addAll(list);
        rootAdapter.notifyDataSetChanged();

        fileBrowser = (FileBrowserControl) view.findViewById(R.id.fileBrowser);
        fileBrowser.setFileBrowserListener(this);
        fileBrowser.initFiles(new File(XFileUtils.getStorageCardPath()));
    }


    @Override
    public void rootDir() {
        fileBrowser.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        StorageRootInfo storage = (StorageRootInfo) rootAdapter.getItem(position);
        if (storage != null) {
            fileBrowser.initRootPath(storage.getFilePath());
            fileBrowser.initFiles(new File(storage.getFilePath()));
            fileBrowser.setVisibility(View.VISIBLE);
        }
    }

}
