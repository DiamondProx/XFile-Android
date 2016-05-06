package com.huangjiang.utils;

import android.media.AudioManager;
import android.media.SoundPool;

import com.huangjiang.XFileApplication;
import com.huangjiang.filetransfer.R;

/**
 * 播放音效
 */
public class SoundHelper {

    static SoundPool soundPool;
    static int onlineId;
    static int receiveFileId;
    static int dragThrowId;

    public static void init() {
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        onlineId = soundPool.load(XFileApplication.context, R.raw.online, 1);
        receiveFileId = soundPool.load(XFileApplication.context, R.raw.receivefile, 1);
        dragThrowId = soundPool.load(XFileApplication.context, R.raw.dragthrow, 1);
    }

    /**
     * 连接成功
     */
    public static void plaOnline() {
        playSound(onlineId);
    }

    /**
     * 接收文件
     */
    public static void playReceiveFile() {
        playSound(receiveFileId);
    }

    /**
     * 传输文件
     */
    public static void playDragThrow() {
        playSound(dragThrowId);
    }


    public static void playSound(int soundId) {
        if (soundPool == null) return;
        AudioManager mgr = (AudioManager) XFileApplication.context.getSystemService(XFileApplication.context.AUDIO_SERVICE);
        final float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        final float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final float volume = streamVolumeCurrent / streamVolumeMax;
        soundPool.play(soundId, volume, volume, 1, 0, 1);
    }

}
