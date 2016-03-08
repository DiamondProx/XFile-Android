package com.huangjiang.message;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by huangjiang on 2016/3/7.
 */
public class LocalTimeClient {

    public static void startClient() throws Exception {


        new Thread(new Runnable() {
            @Override
            public void run() {

                String host = "172.16.166.70";
                int port = 8081;
                Collection<String> cities = new ArrayList<String>() {
                    private static final long serialVersionUID = 1L;

                    {
                        add("America/New_York");
                        add("Asia/Seoul");
                    }
                };
                ClientBootstrap bootstrap = new ClientBootstrap(
                        new NioClientSocketChannelFactory(
                                Executors.newCachedThreadPool(),
                                Executors.newCachedThreadPool()
                        ));
                bootstrap.setPipelineFactory(new LocalTimeClientPipelineFactory());
                ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(host, port));
                Channel channel = connectFuture.awaitUninterruptibly().getChannel();
                LocalTimeClientHandler handler = channel.getPipeline().get(LocalTimeClientHandler.class);
                List<String> response = handler.getLocalTimes(cities);
                channel.close().awaitUninterruptibly();
                bootstrap.releaseExternalResources();

                Iterator<String> i1 = cities.iterator();
                Iterator<String> i2 = response.iterator();
                while (i1.hasNext()) {
                    System.out.format("--------response %28s: %s%n", i1.next(), i2.next());
                }


            }
        }).start();


    }
}
