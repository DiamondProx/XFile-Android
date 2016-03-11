package com.huangjiang.message;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by huangjiang on 2016/3/11.
 */
public class UdpClientDeviceHandler extends ChannelHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
//        Device.Location.Builder builder = Device.Location.newBuilder();
//        builder.setCmd(100);
//        builder.setIp("ip1");
//        builder.setName("name2");
//        ctx.write(builder.build());
//        ctx.flush();
//       System.out.println("-------------channelActivity");
    }
}
