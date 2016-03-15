package com.huangjiang.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 文件消息处理
 */
public class FileServerHandler extends ChannelHandlerAdapter {


//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        super.channelActive(ctx);
//        System.out.println("FileServer---ChannelActive");
//    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("*****channelRead");
//        super.channelRead(ctx, msg);
        ByteBuf byteBuf = (ByteBuf) msg;
        if (byteBuf != null) {
            byte[] data = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(data);
            String str = new String(data, "UTF-8");
            System.out.println("*****channelRead:" + str);
        }

    }
}
