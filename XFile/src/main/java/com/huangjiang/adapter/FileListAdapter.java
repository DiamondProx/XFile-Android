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

import com.huangjiang.model.FileVO;

import com.huangjiang.filetransfer.R;

public class FileListAdapter extends BaseAdapter {

	List<FileVO> mList;
	private LayoutInflater inflater;

	public FileListAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		mList = new ArrayList<FileVO>();
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
			convertView = inflater.inflate(R.layout.listview_browser_files, null);
			holder.img = (ImageView) convertView.findViewById(R.id.image);
			holder.name = (TextView) convertView.findViewById(R.id.fileName);
			holder.size = (TextView) convertView.findViewById(R.id.fileSize);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		FileVO vo = mList.get(position);
		if (vo.isDirectory()) {
			holder.size.setVisibility(View.GONE);
			holder.img.setImageResource(R.mipmap.data_folder_folder);
			holder.name.setText(vo.getFileName());
		} else {
			holder.size.setVisibility(View.VISIBLE);
			holder.size.setText("100kb");
			holder.name.setText(vo.getFileName());
			switch (vo.getFileType()) {
			case Normal:
				holder.img.setImageResource(R.mipmap.data_folder_documents_placeholder);
				break;
			case Audio:
				holder.img.setImageResource(R.mipmap.data_folder_documents_placeholder);
				break;
			case Video:
				holder.img.setImageResource(R.mipmap.data_folder_documents_placeholder);
				break;
			case Image:
				holder.img.setImageResource(R.mipmap.data_folder_documents_placeholder);
				break;
			case Apk:
				holder.img.setImageResource(R.mipmap.data_folder_documents_placeholder);
				break;
			default:
				break;
			}
		}

		return convertView;
	}

	public void clearAll() {
		mList.clear();
	}

	public List<FileVO> getFiles() {
		return mList;
	}

	final class ViewHolder {
		ImageView img;
		TextView name;
		TextView size;
	}

}
