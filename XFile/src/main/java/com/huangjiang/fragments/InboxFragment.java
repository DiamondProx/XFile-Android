package com.huangjiang.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.huangjiang.business.model.Catalog;
import com.huangjiang.filetransfer.R;
import com.huangjiang.utils.XFileUtils;
import com.huangjiang.view.ExplorerControl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 收件箱
 */
public class InboxFragment extends Fragment {

    ExplorerControl explorerControl;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.inbox_fragment, null);
        init(view);
        return view;
    }

    void init(View view) {
        List<Catalog> list = new ArrayList<>();
        String prePath = XFileUtils.getStorageCardPath()+ File.separator;
        prePath += "XFile" + File.separator;
        // 视频目录
        Catalog videoCatalog = new Catalog();
        videoCatalog.setImage(R.mipmap.data_folder_inbox_video);
        videoCatalog.setName(getString(R.string.video));
        videoCatalog.setPath(prePath + "video");
        list.add(videoCatalog);
        // 照片目录
        Catalog imageCatalog = new Catalog();
        imageCatalog.setImage(R.mipmap.data_folder_inbox_photo);
        imageCatalog.setName(getString(R.string.picture));
        imageCatalog.setPath(prePath + "image");
        list.add(imageCatalog);
        // 音乐目录
        Catalog musicCatalog = new Catalog();
        musicCatalog.setImage(R.mipmap.data_folder_inbox_music);
        musicCatalog.setName(getString(R.string.music));
        musicCatalog.setPath(prePath + "music");
        list.add(musicCatalog);
        // 安装包目录
        Catalog appCatalog = new Catalog();
        appCatalog.setImage(R.mipmap.data_folder_inbox_app);
        appCatalog.setName(getString(R.string.install));
        appCatalog.setPath(prePath + "apk");
        list.add(appCatalog);
        // 安装包目录
        Catalog otherCatalog = new Catalog();
        otherCatalog.setImage(R.mipmap.data_folder_inbox_other);
        otherCatalog.setName(getString(R.string.other));
        otherCatalog.setPath(prePath + "other");
        list.add(otherCatalog);

        explorerControl = (ExplorerControl) view.findViewById(R.id.explorer);
        explorerControl.setActivity(getActivity());
        explorerControl.setCatalog(list);
    }


}
