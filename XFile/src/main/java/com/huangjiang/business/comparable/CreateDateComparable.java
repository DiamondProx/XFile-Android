package com.huangjiang.business.comparable;

import com.huangjiang.business.model.TFileInfo;

import java.util.Comparator;

/**
 * 时间对比
 */
public class CreateDateComparable implements Comparator<TFileInfo> {
    @Override
    public int compare(TFileInfo tFileInfo1, TFileInfo tFileInfo2) {
        return tFileInfo1.getCreateTime().compareTo(tFileInfo2.getCreateTime());
    }
}
