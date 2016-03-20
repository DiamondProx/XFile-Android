package com.huangjiang.message;

import android.os.Looper;

import com.huangjiang.config.SysConstant;
import com.huangjiang.utils.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 消息服务器
 */
public class MessageServerThread extends Thread {

    private Logger logger = Logger.getLogger(MessageServerThread.class);
    private int port;
    EventLoopGroup bossGroup = null;
    EventLoopGroup workGroup = null;
    ServerBootstrap serverBootstrap = null;
    ChannelFuture channelFuture = null;
    Channel channel = null;

    public MessageServerThread(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        startServer();
    }

    public void startServer() {
        try {
            bossGroup = new NioEventLoopGroup();
            workGroup = new NioEventLoopGroup();
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MessageServerHandler());
            channelFuture = serverBootstrap.bind(port).sync();
            channel = channelFuture.channel();
            channel.closeFuture().wait();
        } catch (Exception e) {
            e.printStackTrace();
            logger.d(e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public void stopServer() {
        if (channel != null) {
            channel.close();
        }
    }


}
