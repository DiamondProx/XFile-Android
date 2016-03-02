package com.huangjiang.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huangjiang.business.model.StorageRootInfo;

import com.huangjiang.filetransfer.R;

public class StorageRootAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;
	private List<StorageRootInfo> mList;
	public StorageRootAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		mList=new ArrayList<StorageRootInfo>();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {

			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.listview_storage_root, null);
			holder.img = (ImageView) convertView.findViewById(R.id.image);
			holder.desc = (TextView) convertView.findViewById(R.id.desc);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		StorageRootInfo vo = mList.get(position);
		holder.img.setImageResource(vo.getImg());
		holder.desc.setText(vo.getDesc());
		

		return convertView;
	}
	
	public List<StorageRootInfo> getRoots() {
		return mList;
	}

	final class ViewHolder {
		ImageView img;
		TextView desc;
	}
	
}
