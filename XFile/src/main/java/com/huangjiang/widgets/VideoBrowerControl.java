package com.huangjiang.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.huangjiang.model.VideoVO;
import com.huangjiang.utils.Utils;
import com.huangjiang.video.VideoScanner;
import com.huangjiang.video.VideoScanner.ScanVideoCompleteCallBack;

import huangjiang.com.xfile_android.R;

public class VideoBrowerControl extends ListView {

	List<VideoVO> mList = new ArrayList<VideoVO>();
	VideoAdpater mAdapter;

	public VideoBrowerControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initializeVideo(context);

	}

	public VideoBrowerControl(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initializeVideo(context);
	}

	void initializeVideo(Context context) {
		mAdapter = new VideoAdpater(context);
		this.setAdapter(mAdapter);
		VideoScanner videoScanner = new VideoScanner(context);
		videoScanner.scanVideo(new ScanVideoCompleteCallBack() {
			{
				Log.i("XFile", "------ScanVideo");
			}

			@Override
			public void scanVideoComplete(Cursor cursor) {
				// TODO Auto-generated method stub
				mList.clear();
				while (cursor.moveToNext()) {

					String name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));// 文件名
					String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));// 路径
					long addTimes = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));// 创建时间
					long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));// 文件大小
					int playTimes = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));// 播放时长
					// 缩略图

					Log.i("XFile", "playTimes:" + playTimes);

					VideoVO video = new VideoVO();
					video.setFileName(name);
					video.setFilePath(path);
					video.setCreateTime(Utils.paserTimeToYMD(addTimes));
					video.setFileSize(Utils.getFolderSizeString(size));
					video.setTotalTime(Utils.getDuration(playTimes));
					mList.add(video);
				}
				Collections.sort(mList);
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	class VideoAdpater extends BaseAdapter {

		private LayoutInflater mInflater;

		public VideoAdpater(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			VideoViewHoler videoHolder = null;
			if (convertView == null) {
				videoHolder = new VideoViewHoler();
				convertView = mInflater.inflate(R.layout.listview_browser_video, null);
				videoHolder.displayImage = (ImageView) convertView.findViewById(R.id.displayImage);
				videoHolder.totalTime = (TextView) convertView.findViewById(R.id.totalTime);
				videoHolder.displayName = (TextView) convertView.findViewById(R.id.displayName);
				videoHolder.fileSize = (TextView) convertView.findViewById(R.id.fileSize);
				convertView.setTag(videoHolder);
			} else {
				videoHolder = (VideoViewHoler) convertView.getTag();
			}
			VideoVO video = mList.get(position);
			if (video != null) {
				videoHolder.displayImage.setImageResource(R.mipmap.ic_launcher);
				videoHolder.totalTime.setText(video.getTotalTime());
				videoHolder.displayName.setText(video.getFileName());
				videoHolder.fileSize.setText(video.getFileSize());
				videoHolder.displayImage.setImageBitmap(Utils.getVideoThumbnail(video.getFilePath(), 96, 96, MediaStore.Images.Thumbnails.MICRO_KIND));
			}
			return convertView;
		}

		final class VideoViewHoler {
			ImageView displayImage;
			TextView totalTime;
			TextView displayName;
			TextView fileSize;
		}

	}

}
