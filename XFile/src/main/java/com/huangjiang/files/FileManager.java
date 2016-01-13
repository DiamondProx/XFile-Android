package com.huangjiang.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.huangjiang.model.FileVO;
import com.huangjiang.model.FileVO.FileType;
import com.huangjiang.utils.Utils;

import com.huangjiang.filetransfer.R;

public class FileManager {

	private Context mContext;

	public FileManager(Context context) {
		mContext = context;
	}

	public List<FileVO> fillFiles(File[] list) {

		if (mContext == null) {
			throw new NullPointerException("Context Null");
		}
		if (list == null) {
			return new ArrayList<FileVO>();
		}
		List<FileVO> listVO = new ArrayList<FileVO>();
		for (File file : list) {

			FileVO vo = new FileVO();
			vo.setFileName(file.getName());
			vo.setFilePath(file.getPath());
			vo.setDirectory(file.isDirectory());
			vo.setSelectable(false);

			String fileName = file.getName();
			
			if (file.isDirectory()) {
				vo.setFileType(FileType.Folder);
			} else {
				if (Utils.checkEndsWithInStringArray(fileName, mContext.getResources().getStringArray(R.array.fileEndingImage))) {
					vo.setFileType(FileType.Image);
				} else if (Utils.checkEndsWithInStringArray(fileName, mContext.getResources().getStringArray(R.array.fileEndingAudio))) {
					vo.setFileType(FileType.Audio);
				} else if (Utils.checkEndsWithInStringArray(fileName, mContext.getResources().getStringArray(R.array.fileEndingVideo))) {
					vo.setFileType(FileType.Video);
				} else {
					vo.setFileType(FileType.Normal);
				}
			}
			listVO.add(vo);

		}
		return listVO;
	}
}
