package com.huangjiang.message;

import com.huangjiang.config.SysConstant;
import com.huangjiang.utils.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 文件服务器
 */
public class FileServer {

    private ServerChannel mServerChannel;

    public ServerChannel getServerChannel() {
        return mServerChannel;
    }

    private static FileServer instant = null;

    public static FileServer getInstance() {
        if (instant == null) {
            instant = new FileServer();
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
                            .childHandler(new FileServerHandler());
                    ChannelFuture f = b.bind(SysConstant.FILE_SERVER_PORT).sync();
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
