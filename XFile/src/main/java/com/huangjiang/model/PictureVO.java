package com.huangjiang.model;

public class PictureVO implements Comparable<PictureVO> {

	public PictureVO() {

	}

	public PictureVO(String path, String time) {
		this.filePath = path;
		this.createTime = time;
	}

	private String filePath;
	private String createTime;
	private int section;

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public int getSection() {
		return section;
	}

	public void setSection(int section) {
		this.section = section;
	}

	@Override
	public int compareTo(PictureVO vo) {
		if (this.createTime != null && vo != null)
			return vo.getCreateTime().compareTo(this.createTime);
		else
			throw new IllegalArgumentException();
	}
}
