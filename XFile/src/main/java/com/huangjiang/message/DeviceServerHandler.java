package com.huangjiang.message;

import com.huangjiang.XFileApplication;
import com.huangjiang.config.SysConstant;
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
 *
 */
public class DeviceServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {

        ByteBuf byteBuf = msg.content();
        byte[] byteHeader = byteBuf.readBytes(SysConstant.HEADER_LENGTH).array();
        Header header = new Header(byteHeader);
        int commandId = header.getCommandId();
        switch (commandId) {
            case SysConstant.CMD_Bonjour:
                sendEcho(ctx, byteBuf, header);
                break;
            case SysConstant.CMD_ECHO:
                recEcho(ctx, byteBuf, header);
                break;
        }



    }

    void sendEcho(ChannelHandlerContext ctx, ByteBuf byteBuf, Header inHeader) {

        try {
            long threadId=Thread.currentThread().getId();
            System.out.println("*****currentThreadId111111:"+threadId);
            byte[] bonjourData = byteBuf.readBytes(inHeader.getLength() - SysConstant.HEADER_LENGTH).array();
            XFileProtocol.Bonjour bonjour = XFileProtocol.Bonjour.parseFrom(bonjourData);
            String remoteIp = bonjour.getIp().toString();
            int remotePort = bonjour.getPort();
            Header sendHeader = new Header();
            sendHeader.setCommandId(SysConstant.CMD_ECHO);
            String localIp = NetStateUtil.getIPv4(XFileApplication.context);
            XFileProtocol.Echo.Builder echo = XFileProtocol.Echo.newBuilder();
            echo.setIp(localIp);
            echo.setPort(8081);
            echo.setName(android.os.Build.MODEL + "-echo");
            byte[] body = echo.build().toByteArray();
            sendHeader.setLength(SysConstant.HEADER_LENGTH + body.length);
            byte[] data = new byte[SysConstant.HEADER_LENGTH + body.length];
            System.arraycopy(sendHeader.toByteArray(), 0, data, 0, SysConstant.HEADER_LENGTH);
            System.arraycopy(body, 0, data, SysConstant.HEADER_LENGTH, body.length);
            ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(data), new InetSocketAddress(remoteIp, remotePort))).sync();
        } catch (Exception e) {
            Logger.getLogger(DeviceServerHandler.class).d("sendEcho", e.getMessage());
        }

    }

    void recEcho(ChannelHandlerContext ctx, ByteBuf byteBuf, Header inHeader) {
        try {
            long threadId=Thread.currentThread().getId();
            System.out.println("*****currentThreadId222222:"+threadId);
            byte[] echoData = byteBuf.readBytes(inHeader.getLength() - SysConstant.HEADER_LENGTH).array();
            XFileProtocol.Echo echo = XFileProtocol.Echo.parseFrom(echoData);
            DeviceInfoEvent event = new DeviceInfoEvent();
            event.setIp(echo.getIp());
            event.setName(echo.getName());
            event.setPort(echo.getPort());
            EventBus.getDefault().postSticky(event);

        } catch (Exception e) {
            Logger.getLogger(DeviceServerHandler.class).d("recEcho", e.getMessage());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
