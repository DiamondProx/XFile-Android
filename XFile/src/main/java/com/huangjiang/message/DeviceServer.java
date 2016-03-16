package com.huangjiang.message;

import com.huangjiang.message.protocol.XFileProtocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;

/**
 * 发现设备服务器
 */
public class DeviceServer {

    private static Channel mServerChannel;


    public static Channel getServerChannel() {
        return mServerChannel;
    }

    public static void start() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                EventLoopGroup group = new NioEventLoopGroup();
                try {
                    Bootstrap b = new Bootstrap();
                    b.group(group).channel(NioDatagramChannel.class)
                            .option(ChannelOption.SO_BROADCAST, true)
                            .handler(new ChannelInitializer<NioDatagramChannel>() {
                                @Override
                                protected void initChannel(NioDatagramChannel ch) throws Exception {
                                    ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                                    ch.pipeline().addLast(new ProtobufDecoder(XFileProtocol.File.getDefaultInstance()));
                                    ch.pipeline().addLast(new ProtobufEncoder());
                                    ch.pipeline().addLast(new DeviceServerHandler());
                                }
                            });
                    b.bind(8081).sync().channel().closeFuture().await();
                } catch (Exception e) {
                    System.out.println("exception:" + e.getMessage());
                }
            }
        }).start();


    }
}
