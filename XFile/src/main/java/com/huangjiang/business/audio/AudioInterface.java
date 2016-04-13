package com.huangjiang.business.audio;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.huangjiang.business.model.FileType;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.core.ResponseCallback;
import com.huangjiang.core.StateCode;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.XFileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 音频
 */
public class AudioInterface {

    private Logger logger = Logger.getLogger(AudioInterface.class);

    private Context mContext;

    public AudioInterface(Context context) {
        this.mContext = context;
    }

    /**
     * 查找所有音频
     */
    public void searchAudio(final ResponseCallback<List<TFileInfo>> callback) {
        try {
            Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = mContext.getContentResolver();
            Cursor cursor = mContentResolver.query(audioUri, null, null, null, null);
            List<TFileInfo> list = readAudioCursor(cursor);
            callback.onResponse(StateCode.REQUEST_SUCCESS, 0, null, list);
        } catch (Exception e) {
            callback.onResponse(StateCode.REQUEST_FAIL, 0, e.getMessage(), null);
            e.printStackTrace();
            logger.e(e.getMessage());
        }


    }

    /**
     * 根据关键字查找音频
     */
    public void searchAudio(final String searchKey, final ResponseCallback<List<TFileInfo>> callback) {
        try {
            Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = mContext.getContentResolver();
            Cursor cursor = mContentResolver.query(audioUri, null, MediaStore.Audio.Media.DISPLAY_NAME + " like '%" + searchKey + "%'", null, null);
            List<TFileInfo> list = readAudioCursor(cursor);
            callback.onResponse(StateCode.REQUEST_SUCCESS, 0, null, list);
            logger.e("****searchAudioCurrentThreadId:" + Thread.currentThread().getId());
        } catch (Exception e) {
            callback.onResponse(StateCode.REQUEST_FAIL, 0, e.getMessage(), null);
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }

    /**
     * 读取音频cursor内容
     */
    List<TFileInfo> readAudioCursor(Cursor cursor) {
        List<TFileInfo> list = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {

                String display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));// 文件名
                String file_path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));// 路径
                long create_time = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));// 创建时间
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));// 文件大小
                int play_time = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));// 播放时长

                TFileInfo audio_file = new TFileInfo();
                audio_file.setName(display_name);
                audio_file.setPath(file_path);
                audio_file.setCreateTime(XFileUtils.paserTimeToYMD(create_time));
                audio_file.setLength(size);
                audio_file.setPlayTime(play_time);
                audio_file.setFileType(FileType.Audio);
                list.add(audio_file);
            }
            cursor.close();
        }
        return list;
    }


}
