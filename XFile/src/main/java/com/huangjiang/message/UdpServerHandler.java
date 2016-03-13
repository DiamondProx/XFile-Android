package com.huangjiang.message;

import com.huangjiang.config.SysConstant;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 *
 */
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {


        ByteBuf byteBuf = msg.content();
        byte[] byteHeader = byteBuf.readBytes(SysConstant.HEADER_LENGTH).array();
        Header header=new Header(byteHeader);
        // 头命令
        System.out.println("header.length:" + header.getCommandId());

        byte[] data = msg.content().array();
        int length = 13;
        byte[] data2 = new byte[14];
        for (int i = 0; i <= length; i++) {
            data2[i] = data[i];
        }
        Device.Location builder = Device.Location.parseFrom(data2);
        System.out.println("-----UdpServerHanlder messageReceived:" + builder.getName());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
