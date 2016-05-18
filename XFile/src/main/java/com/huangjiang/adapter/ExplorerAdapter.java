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
import com.huangjiang.utils.XFileUtils;
import com.huangjiang.xfile.R;

import java.util.ArrayList;
import java.util.List;

public class ExplorerAdapter extends BaseAdapter {

    List<TFileInfo> list;
    LayoutInflater inflater;

    public ExplorerAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        list = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public List<TFileInfo> getTFiles() {
        return list;
    }

    public void addTFiles(List<TFileInfo> fileList) {
        this.list.clear();
        this.list.addAll(fileList);
    }

    public void removeFile(TFileInfo tFileInfo) {
        for (TFileInfo file : list) {
            if (tFileInfo.getFileType() == FileType.Apk) {
                if (tFileInfo.getPackageName().equals(file.getPackageName())) {
                    list.remove(file);
                    break;
                }
            } else {
                if (tFileInfo.getTaskId().equals(file.getTaskId()) || tFileInfo.getPath().equals(file.getPath())) {
                    list.remove(file);
                    break;
                }
            }

        }
    }

    public void updateFile(TFileInfo tFileInfo) {
        for (TFileInfo file : list) {
            if (!file.isDirectory() && file.getTaskId() != null && file.getTaskId().equals(tFileInfo.getTaskId())) {
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listview_browser_files, null);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.name = (TextView) convertView.findViewById(R.id.fileName);
            holder.size = (TextView) convertView.findViewById(R.id.fileSize);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        TFileInfo tFileInfo = list.get(position);
        if (tFileInfo.isDirectory()) {
            holder.size.setVisibility(View.GONE);
            holder.image.setImageResource(R.mipmap.data_folder_folder);
            holder.name.setText(tFileInfo.getName());
        } else {
            holder.size.setVisibility(View.VISIBLE);
            holder.size.setText(XFileUtils.parseSize(tFileInfo.getLength()));
            holder.name.setText(tFileInfo.getFullName());
            switch (tFileInfo.getFileType()) {
                case Normal:
                    holder.image.setImageResource(R.mipmap.data_folder_documents_placeholder);
                    break;
                case Audio:
                    holder.image.setImageResource(R.mipmap.data_music_play_cover_placeholder);
                    break;
                case Video:
                    holder.image.setImageResource(R.mipmap.data_movie_default);
                    ImageLoader.getInstance().displayThumb(holder.image, tFileInfo);
                    break;
                case Image:
                    holder.image.setImageResource(R.mipmap.data_photo_l);
                    ImageLoader.getInstance().displayThumb(holder.image, tFileInfo);
                    break;
                case Apk:
                    holder.image.setImageResource(R.mipmap.ic_launcher);
                    ImageLoader.getInstance().displayThumb(holder.image, tFileInfo);
                    break;
                default:
                    holder.image.setImageResource(R.mipmap.data_folder_documents_placeholder);
                    break;
            }
        }

        return convertView;
    }


    final class ViewHolder {
        ImageView image;
        TextView name;
        TextView size;
    }

}
