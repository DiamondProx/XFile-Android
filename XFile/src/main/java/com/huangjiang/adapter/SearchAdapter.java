package com.huangjiang.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huangjiang.business.model.FileType;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.core.ImageLoader;
import com.huangjiang.filetransfer.R;
import com.huangjiang.utils.XFileUtils;

import java.io.File;
import java.util.List;

/**
 * 文件搜索
 */
public class SearchAdapter extends BaseAdapter {

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
            } else if (file.getFileType() == FileType.Audio) {
                videoHolder.Img.setImageResource(R.mipmap.data_music_play_cover_placeholder);
            } else {
                videoHolder.Img.setImageResource(R.mipmap.data_folder_documents_placeholder);
            }
            videoHolder.Name.setText(file.getName());
            videoHolder.Size.setText(XFileUtils.parseSize(file.getLength()));
        }
        return convertView;
    }

    public void removeFile(TFileInfo tFileInfo) {
        if (mList == null) return;
        for (TFileInfo file : mList) {
            if (tFileInfo.getFileType() == FileType.Apk) {
                if (tFileInfo.getPackageName().equals(file.getPackageName())) {
                    mList.remove(file);
                    break;
                }
            } else {
                if (tFileInfo.getTaskId().equals(file.getTaskId())) {
                    mList.remove(file);
                    break;
                }
            }

        }
    }

    public void updateFile(TFileInfo tFileInfo) {
        for (TFileInfo file : mList) {
            if (file.getTaskId().equals(tFileInfo.getTaskId())) {
                file.setName(tFileInfo.getName());
                file.setPosition(tFileInfo.getPosition());
                file.setLength(tFileInfo.getLength());
                file.setPath(tFileInfo.getPath());
                file.setExtension(tFileInfo.getExtension());
                file.setFullName(tFileInfo.getFullName());
                break;
            }
        }
    }

    final class ViewHolder {
        ImageView Img;
        TextView Name;
        TextView Size;
    }

}
