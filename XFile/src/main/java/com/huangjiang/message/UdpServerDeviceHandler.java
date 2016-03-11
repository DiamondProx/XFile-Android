package com.huangjiang.message;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by huangjiang on 2016/3/11.
 */
public class UdpServerDeviceHandler extends ChannelHandlerAdapter{
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        super.channelRead(ctx, msg);
        System.out.println("channelRead-----"+msg.toString());
        Device.Location location=(Device.Location)msg;
        System.out.println("location:"+location.getName());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
