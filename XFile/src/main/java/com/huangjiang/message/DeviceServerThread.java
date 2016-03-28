package com.huangjiang.message;

import com.google.protobuf.GeneratedMessage;
import com.huangjiang.config.SysConstant;
import com.huangjiang.message.base.Header;
import com.huangjiang.utils.Logger;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * 发现设备服务器
 */
public class DeviceServerThread extends Thread {

    private Logger logger = Logger.getLogger(DeviceServerThread.class);

    ChannelFuture channelFuture = null;
    EventLoopGroup mGroup = null;
    Bootstrap bootstrap = null;

    @Override
    public void run() {
        startServer();
    }

    public void startServer() {
        mGroup = new NioEventLoopGroup();
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(mGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) throws Exception {
                            ch.pipeline().addLast(new DeviceServerHandler());
                        }
                    });
            channelFuture = bootstrap.bind(SysConstant.BROADCASE_PORT).sync();
            channelFuture.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        } finally {
            mGroup.shutdownGracefully();
        }

    }

    public void stopServer() {
        if (channelFuture != null) {
            channelFuture.channel().close();
            channelFuture = null;
        }
    }

    public void sendRequest(Header header, GeneratedMessage msg, String host, int port) {
        try {
            ByteBuf byteBuf = Unpooled.buffer(header.getLength());
            byteBuf.writeBytes(header.toByteArray());
            byteBuf.writeBytes(msg.toByteArray());
            if (channelFuture != null && channelFuture.channel() != null) {
                channelFuture.channel().writeAndFlush(new DatagramPacket(byteBuf, new InetSocketAddress(host, port))).sync();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }


}
