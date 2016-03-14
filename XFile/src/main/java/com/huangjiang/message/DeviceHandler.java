package com.huangjiang.message;

import com.huangjiang.config.SysConstant;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * Created by huangjiang on 2016/3/14.
 */
public class DeviceHandler extends SimpleChannelInboundHandler<DatagramPacket> {


    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf byteBuf = msg.content();
        byte[] byteHeader = byteBuf.readBytes(SysConstant.HEADER_LENGTH).array();
        Header header = new Header(byteHeader);
        switch (header.getCommandId()){
            case SysConstant.CMD_Bonjour:

                break;
            case SysConstant.CMD_ECHO:

                break;
        }
    }
}
