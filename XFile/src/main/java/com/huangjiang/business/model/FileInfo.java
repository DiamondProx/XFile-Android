package com.huangjiang.business.model;

import android.graphics.drawable.Drawable;

public class FileInfo implements Comparable<FileInfo> {

	// 文件名
	private String fileName;
	// 文件路径
	private String filePath;
	// 图标
	private Drawable fileIcon;
	// 文件或文件夹
	private boolean isDirectory;
	// 文件类型
	private FileType fileType;

	// 文件大小
	private int fileLength;
	// 大小描述
	private String fileLengthDesc;

	public int getFileLength() {
		return fileLength;
	}

	public void setFileLength(int fileLength) {
		this.fileLength = fileLength;
	}

	public String getFileLengthDesc() {
		return fileLengthDesc;
	}

	public void setFileLengthDesc(String fileLengthDesc) {
		this.fileLengthDesc = fileLengthDesc;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	// 是否选中
	private boolean selectable;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Drawable getFileIcon() {
		return fileIcon;
	}

	public void setFileIcon(Drawable fileIcon) {
		this.fileIcon = fileIcon;
	}

	public boolean isSelectable() {
		return selectable;
	}

	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public int compareTo(FileInfo vo) {
		if (this.fileName != null)
			return this.fileName.compareTo(vo.getFileName());
		else
			throw new IllegalArgumentException();
	}

	public enum FileType {
		Folder, Normal, Audio, Video, Image, Apk
	}

}
