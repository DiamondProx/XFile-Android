package com.huangjiang.business.audio;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.huangjiang.business.model.FileInfo;
import com.huangjiang.business.model.VideoInfo;
import com.huangjiang.core.ResponseCallback;
import com.huangjiang.core.StateCode;
import com.huangjiang.utils.XFileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 音频
 */
public class AudioInterface {

    private Context mContext;

    public AudioInterface(Context context) {
        this.mContext = context;
    }

    /**
     * 查找所有音频
     *
     * @param callback
     */
    public void getAudio(final ResponseCallback<List<FileInfo>> callback) {
        try {
            Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = mContext.getContentResolver();
            Cursor cursor = mContentResolver.query(audioUri, null, null, null, null);
            List<FileInfo> list = new ArrayList<>();
            while (cursor.moveToNext()) {

                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));// 文件名
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));// 路径
                long createTime = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));// 创建时间
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));// 文件大小
                int playTime = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));// 播放时长

                FileInfo audio = new FileInfo();
                audio.setName(name);
                audio.setPath(path);
                audio.setCreator(XFileUtils.paserTimeToYMD(createTime));
                audio.setSpace(XFileUtils.getFolderSizeString(size));
                audio.setTotalTimeStr(XFileUtils.getDuration(playTime));
                audio.setFileType(FileInfo.FileType.Audio);
                list.add(audio);
            }
            callback.onResponse(StateCode.REQUEST_SUCCESS, 0, null, list);
        } catch (Exception e) {
            callback.onResponse(StateCode.REQUEST_FAIL, 0, e.getMessage(), null);
        }


    }

    /**
     * 根据关键字查找音频
     *
     * @param searchKey
     * @param callback
     */
    public void getAudioByKey(final String searchKey, final ResponseCallback<List<FileInfo>> callback) {
        try {
            Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = mContext.getContentResolver();
            Cursor cursor = mContentResolver.query(audioUri, null, MediaStore.Audio.Media.DISPLAY_NAME + " like '%" + searchKey + "%'", null, null);
            List<FileInfo> list = new ArrayList<>();
            while (cursor.moveToNext()) {

                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));// 文件名
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));// 路径
                long createTime = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));// 创建时间
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));// 文件大小
                int playTime = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));// 播放时长

                FileInfo audio = new FileInfo();
                audio.setName(name);
                audio.setPath(path);
                audio.setCreator(XFileUtils.paserTimeToYMD(createTime));
                audio.setSpace(XFileUtils.getFolderSizeString(size));
                audio.setTotalTimeStr(XFileUtils.getDuration(playTime));
                audio.setFileType(FileInfo.FileType.Audio);
                audio.setSpace(XFileUtils.getFolderSizeString(size));
                list.add(audio);
            }
            callback.onResponse(StateCode.REQUEST_SUCCESS, 0, null, list);
        } catch (Exception e) {
            callback.onResponse(StateCode.REQUEST_FAIL, 0, e.getMessage(), null);
        }
    }


}
