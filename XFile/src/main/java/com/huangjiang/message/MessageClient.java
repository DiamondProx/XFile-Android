package com.huangjiang.message;

import com.huangjiang.config.SysConstant;
import com.huangjiang.utils.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 消息客户端
 */
public class MessageClient {
    Channel mChannel;

    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                EventLoopGroup group = new NioEventLoopGroup();
                try {
                    Bootstrap b = new Bootstrap();
                    b.group(group).channel(NioSocketChannel.class)
                            .option(ChannelOption.TCP_NODELAY, true)
                            .handler(new MessageClientHandler());
                    ChannelFuture f = b.connect("127.0.0.1", SysConstant.MESSAGE_PORT);
                    mChannel = f.channel();
//                    mChannel.writeAndFlush(Unpooled.copiedBuffer("hello".getBytes()));
                    mChannel.closeFuture().sync();
                } catch (Exception e) {
                    Logger.getLogger(FileClient.class).d("FileClient", e.getMessage());
                } finally {
                    group.shutdownGracefully();
                }
            }
        }).start();

    }

    public void close() {
        if (mChannel != null) {
            mChannel.close();
        }
    }

    public void write(Object msg) {
        mChannel.writeAndFlush(msg);
    }

}
