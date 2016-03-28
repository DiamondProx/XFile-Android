package com.huangjiang.message;

import com.huangjiang.XFileApplication;
import com.huangjiang.config.SysConstant;
import com.huangjiang.manager.IMDeviceServerManager;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.event.DeviceInfoEvent;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.NetStateUtil;

import org.greenrobot.eventbus.EventBus;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
        logger.e("****DeviceServerMessageReceived");
        IMDeviceServerManager.getInstance().packetDispatch(msg.content());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.e(cause.getMessage());
    }
}
