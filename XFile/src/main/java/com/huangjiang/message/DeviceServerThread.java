package com.huangjiang.message;

import com.google.protobuf.GeneratedMessage;
import com.huangjiang.config.SysConstant;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;

import java.net.InetSocketAddress;
import java.nio.Buffer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;

/**
 * 发现设备服务器
 */
public class DeviceServerThread extends Thread {

    private Logger logger = Logger.getLogger(DeviceServerThread.class);

    Channel mServerChannel = null;
    EventLoopGroup mGroup = null;
    Bootstrap mBootstarp = null;

    @Override
    public void run() {
        startService();
    }

    public void startService() {
        mGroup = new NioEventLoopGroup();
        try {
            mBootstarp = new Bootstrap();
            mBootstarp.group(mGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) throws Exception {
                            ch.pipeline().addLast(new DeviceServerHandler());
                        }
                    });
            mServerChannel = mBootstarp.bind(SysConstant.BROADCASE_PORT).sync().channel();
            mServerChannel.closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }

    public void stopService() {
        try {
            if (mServerChannel != null)
                mServerChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }

    public void sendRequest(GeneratedMessage msg, Header header, String host, int port) {
        try {
            ByteBuf byteBuf = Unpooled.buffer(header.getLength());
            byteBuf.writeBytes(header.toByteArray());
            byteBuf.writeBytes(msg.toByteArray());
            if (mServerChannel != null) {
                mServerChannel.writeAndFlush(new DatagramPacket(byteBuf, new InetSocketAddress(host, port))).sync();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
