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

/**
 * 安装程序适配器
 */
public class InstallAdapter extends BaseAdapter {

    List<TFileInfo> list;
    LayoutInflater inflater;

    public InstallAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        list = new ArrayList<>();
    }

    public void setList(List<TFileInfo> mList) {
        this.list = mList;
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


    public void removeFile(TFileInfo tFileInfo) {
        for (TFileInfo file : list) {
            if (tFileInfo.getFileType() == FileType.Install) {
                if (tFileInfo.getPackageName().equals(file.getPackageName())) {
                    list.remove(file);
                    break;
                }
            } else {
                if (tFileInfo.getTaskId().equals(file.getTaskId())) {
                    list.remove(file);
                    break;
                }
            }

        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.group_gridview_apps_item, null);
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
            holder.name.setText(tFileInfo.getName());
            holder.image.setImageResource(R.mipmap.ic_launcher);
            ImageLoader.getInstance().displayThumb(holder.image, tFileInfo);
        }

        return convertView;
    }


    final class ViewHolder {
        ImageView image;
        TextView name;
        TextView size;
    }


}
