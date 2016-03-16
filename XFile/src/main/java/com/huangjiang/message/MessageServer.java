package com.huangjiang.message;

import com.huangjiang.config.SysConstant;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 消息服务器
 */
public class MessageServer {
    private ServerChannel mServerChannel;

    public ServerChannel getServerChannel() {
        return mServerChannel;
    }

    private static MessageServer instant = null;

    public static MessageServer getInstance() {
        if (instant == null) {
            instant = new MessageServer();
        }
        return instant;
    }


    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                EventLoopGroup bossGroup = new NioEventLoopGroup();
                EventLoopGroup workGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                            .option(ChannelOption.SO_BACKLOG, 1024)
                            .childHandler(new MessageServerHandler());
                    ChannelFuture f = b.bind(SysConstant.MESSAGE_PORT).sync();
                    f.channel().closeFuture().sync();

                } catch (Exception e) {
                    System.out.println("error*****"+e.getMessage());
                } finally {
                    bossGroup.shutdownGracefully();
                    workGroup.shutdownGracefully();
                }
            }
        }).start();
    }


}
