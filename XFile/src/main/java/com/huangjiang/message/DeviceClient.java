package com.huangjiang.message;

import android.content.Context;

import com.google.protobuf.ByteString;
import com.huangjiang.config.SysConstant;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.IPv4Util;
import com.huangjiang.utils.NetStateUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufProcessor;
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
public class DeviceClient {

    private Context mContext;

    public DeviceClient(Context context) {
        this.mContext = context;
    }


    public void start() {
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

                    Header header = new Header();
                    header.setCommandId(SysConstant.CMD_Bonjour);

                    String ip = NetStateUtil.getIPv4(mContext);
                    byte[] ipbs = IPv4Util.ipToBytesByInet(ip);

                    XFileProtocol.Bonjour.Builder bonjour = XFileProtocol.Bonjour.newBuilder();
                    ByteString byteString = ByteString.copyFrom(ipbs);
                    bonjour.setIp(byteString);
                    bonjour.setPort(8081);

//                    Device.Location.Builder builder = Device.Location.newBuilder();
//                    builder.setCmd(100);
//                    builder.setIp("ip1");
//                    builder.setName("name2");


//                    ch.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("t", CharsetUtil.UTF_8), new InetSocketAddress("255.255.255.255", 8081))).sync();

//                    ch.connect(new InetSocketAddress("255.255.255.255", 8081));
//                    ch.writeAndFlush(builder.build()).sync();
//                    byte[] data = builder.build().toByteArray();
                    byte[] body = bonjour.build().toByteArray();
                    byte[] data = new byte[SysConstant.HEADER_LENGTH + body.length];
                    System.arraycopy(header.toByteArray(), 0, data, 0, SysConstant.HEADER_LENGTH);
                    System.arraycopy(body, 0, data, SysConstant.HEADER_LENGTH, body.length);

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
