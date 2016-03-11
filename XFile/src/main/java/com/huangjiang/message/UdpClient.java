package com.huangjiang.message;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.util.CharsetUtil;

/**
 * Created by huangjiang on 2016/3/11.
 */
public class UdpClient {

    public static void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                EventLoopGroup group = new NioEventLoopGroup();
                try {
                    Bootstrap b = new Bootstrap();
                    b.group(group).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, true)
                            .handler(new ChannelInitializer<NioDatagramChannel>() {
                                @Override
                                protected void initChannel(NioDatagramChannel ch) throws Exception {
                                    ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                                    ch.pipeline().addLast(new ProtobufDecoder(Device.Location.getDefaultInstance()));
                                    ch.pipeline().addLast(new ProtobufEncoder());
                                    ch.pipeline().addLast(new UdpClientDeviceHandler());
                                }
                            });

                    Channel ch = b.bind(0).sync().channel();
                    Device.Location.Builder builder = Device.Location.newBuilder();
                    builder.setCmd(100);
//                    builder.setIp("ip1");
                    builder.setName("name2");


//                    ch.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("t", CharsetUtil.UTF_8), new InetSocketAddress("255.255.255.255", 8081))).sync();

//                    ch.connect(new InetSocketAddress("255.255.255.255", 8081));
//                    ch.writeAndFlush(builder.build()).sync();
                    byte[] data = builder.build().toByteArray();

                    Device.Location ll = Device.Location.parseFrom(data);
                    ll.getName();

                    ch.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(data), new InetSocketAddress("255.255.255.255", 8081))).sync();


                    if (!ch.closeFuture().await(15000)) {
                        System.out.println("search timeout");
                    }
                } catch (Exception e) {
                    group.shutdownGracefully();
                }
            }
        }).start();


    }
}
