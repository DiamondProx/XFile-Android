package com.huangjiang.utils;

import android.media.AudioManager;
import android.media.SoundPool;

import com.huangjiang.XFileApplication;
import com.huangjiang.filetransfer.R;

/**
 * 播放音效
 */
public class SoundHelper {


    /**
     * 连接成功
     */
    public static void plaOnline() {
//        playSound(R.raw.online);
    }

    /**
     * 接收文件
     */
    public static void playReceiveFile() {
//        playSound(R.raw.receivefile);
    }

    /**
     * 传输文件
     */
    public static void playDragThrow() {
//        playSound(R.raw.dragthrow);
    }

    public static void playSound(int resId) {
        SoundPool soundPool;
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundPool.load(XFileApplication.context, resId, 1);
        soundPool.play(1, 1, 1, 0, 0, 1);
    }

}
