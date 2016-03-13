package com.huangjiang;

import android.app.Application;

import com.huangjiang.message.XFileDeviceServer;

import java.nio.ByteBuffer;

public class XFileApplication extends Application {
;
    @Override
    public void onCreate() {
        super.onCreate();
        XFileDeviceServer.start();

//        ByteBuffer buffer=ByteBuffer.allocate(88);
//        String value="12345";
//        buffer.put(value.getBytes());
//        System.out.println("buffer.remaining:"+buffer.remaining());
//        buffer.flip();
//        System.out.println("buffer.remaining:"+buffer.remaining());
//        byte[] vArray=new byte[buffer.remaining()];


    }

}
