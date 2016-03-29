package com.huangjiang.message;

import com.huangjiang.manager.IMDeviceServerManager;
import com.huangjiang.utils.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * 发现设备消息处理
 */
public class DeviceServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private Logger logger = Logger.getLogger(DeviceServerHandler.class);

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
//        logger.e("****DeviceServerMessageReceived");
        IMDeviceServerManager.getInstance().packetDispatch(msg.content());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.e(cause.getMessage());
    }
}
