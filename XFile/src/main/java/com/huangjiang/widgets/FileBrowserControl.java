package com.huangjiang.widgets;

import java.io.File;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.huangjiang.adapter.FileListAdapter;
import com.huangjiang.files.FileManager;
import com.huangjiang.model.FileVO;

import huangjiang.com.xfile_android.R;

public class FileBrowserControl extends LinearLayout implements OnItemClickListener, View.OnClickListener

{

	private Context mContext;
	private String rootFilePath = "";
	private String mCurrentFilePath = "/";
	private LinearLayout headerLayout;
	private ImageView headerUpDirIcon;
	private TextView headerUpDir;
	private ListView mListView;
	private FileListAdapter fileAdapter;
	private FileBrowserListener fileBrowserListener;

	public FileBrowserControl(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		initializeView(context);
	}

	public FileBrowserControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		initializeView(context);

	}

	@SuppressLint("NewApi")
	void initializeView(Context context) {

		setOrientation(LinearLayout.VERTICAL);

		headerLayout = new LinearLayout(context);
		headerLayout.setId(R.id.fileBrowserUpdir);
		LayoutParams headerLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, 50);
		headerLayout.setLayoutParams(headerLayoutParams);
		headerLayout.setBackground(context.getResources().getDrawable(R.drawable.updir_selector));
		headerLayout.setGravity(Gravity.CENTER_VERTICAL);
		headerLayout.setOnClickListener(this);
		addView(headerLayout);

		headerUpDirIcon = new ImageView(context);
		LayoutParams headerIconParams = new LayoutParams(60, 60);
		headerIconParams.setMargins(10, 0, 0, 0);
		headerUpDirIcon.setLayoutParams(headerIconParams);
		headerUpDirIcon.setBackgroundResource(R.mipmap.data_folder_dir);
		headerLayout.addView(headerUpDirIcon);

		headerUpDir = new TextView(context);
		LayoutParams headerPathParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		headerPathParams.setMargins(0, 0, 0, 0);
		headerUpDir.setLayoutParams(headerPathParams);
		headerUpDir.setText("上一级");
		ColorStateList csl = (ColorStateList) context.getResources().getColorStateList(R.color.font_txt_gray);
		headerUpDir.setTextColor(csl);
		headerLayout.addView(headerUpDir);

		mListView = new ListView(context);
		LayoutParams headerFileListParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mListView.setLayoutParams(headerFileListParams);
		mListView.setBackgroundResource(R.color.app_background);
		addView(mListView);
		fileAdapter = new FileListAdapter(context);
		mListView.setAdapter(fileAdapter);
		mListView.setOnItemClickListener(this);

	}

	public void initFiles() {

		List<FileVO> list = new FileManager(mContext).fillFiles(new File(mCurrentFilePath).listFiles());
		Collections.sort(list);
		fileAdapter.getFiles().addAll(list);
		fileAdapter.notifyDataSetChanged();

	}

	public void setFileBrowserListener(FileBrowserListener fileBrowserListener) {
		this.fileBrowserListener = fileBrowserListener;
	}

	public void initRootPath(String rootPath) {
		mCurrentFilePath = rootFilePath = rootPath;
	}

	public void initFiles(File file) {
		List<FileVO> list = new FileManager(mContext).fillFiles(file.listFiles());
		Collections.sort(list);
		fileAdapter.clearAll();
		fileAdapter.getFiles().addAll(list);
		fileAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
		// TODO Auto-generated method stub
		FileVO file = (FileVO) fileAdapter.getItem(index);
		if (file == null) {
			return;
		}
		if (file.isDirectory()) {
			String nextDir = "";
			if (mCurrentFilePath.equals("/")) {
				nextDir = mCurrentFilePath + file.getFileName();
			} else {
				nextDir = mCurrentFilePath + "/" + file.getFileName();
			}
			File nextFile = new File(nextDir);
			if (nextFile.isDirectory()) {
				mCurrentFilePath = nextDir;
				initFiles(nextFile);
			}
		} else {
			Toast.makeText(mContext, "fileName:" + file.getFileName(), Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.fileBrowserUpdir:
			upDir();
			break;
		default:
			break;
		}
	}

	void upDir() {
		if (mCurrentFilePath.equals("/") || rootFilePath.equals(mCurrentFilePath)) {
			if (fileBrowserListener != null) {
				fileBrowserListener.rootDir();
			}
			return;
		}
		int lastIndex = mCurrentFilePath.lastIndexOf("/");
		if (lastIndex == 0) {
			mCurrentFilePath = "/";
			initFiles(new File(mCurrentFilePath));
			return;
		}
		mCurrentFilePath = mCurrentFilePath.substring(0, lastIndex);
		initFiles(new File(mCurrentFilePath));
	}

	public interface FileBrowserListener {
		void rootDir();
	}

}
