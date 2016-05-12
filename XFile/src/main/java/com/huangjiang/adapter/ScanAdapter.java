package com.huangjiang.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huangjiang.business.model.ScanInfo;
import com.huangjiang.xfile.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 扫描设备
 */
public class ScanAdapter extends BaseAdapter {

    private List<ScanInfo> list = null;

    private LayoutInflater inflater;

    public ScanAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        list = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listview_scan_device, null);
            holder.name = (TextView) convertView.findViewById(R.id.device_name);
            holder.connectType = (ImageView) convertView.findViewById(R.id.ivConnectType);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ScanInfo device = list.get(position);
        holder.name.setText(device.getName());
        if (device.getLinkType() == ScanInfo.LinkType.WIFI) {
            holder.connectType.setBackgroundResource(R.mipmap.connect_wifi);
        } else {
            holder.connectType.setBackgroundResource(R.mipmap.connect_hotspot);
        }
        return convertView;
    }

    public void clear() {
        if (list != null) {
            list.clear();
        }
    }

    public void add(ScanInfo device) {
        if (list != null) {
            list.add(device);
        }
    }

    public boolean contains(ScanInfo device) {
        for (ScanInfo d : list) {
            if (d.getDeviceId().equals(device.getDeviceId())) {
                return true;
            }
        }
        return false;
    }

    public void setList(List<ScanInfo> mList) {
        this.list = mList;
    }

    final class ViewHolder {
        TextView name;
        ImageView connectType;
    }


}
