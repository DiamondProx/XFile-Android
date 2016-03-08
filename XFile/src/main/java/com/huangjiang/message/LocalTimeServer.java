package com.huangjiang.message;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.TimerTask;

public class LocalTimeServer {
	
	 public static void main(String[] args) throws Exception { 
		
		ServerBootstrap bootstarp=new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),Executors.newCachedThreadPool()));
		bootstarp.setPipelineFactory(new LocalTimeServerPipelineFactory());
		bootstarp.bind(new InetSocketAddress(8081));

		System.out.println("LocalTimeServer start success");
		
//		new Thread(new  Runnable() {
//			public void run() {
//				try {
//					System.out.println("take start:"+System.currentTimeMillis());
//					String str=list.take();
//					System.out.println("take success:"+System.currentTimeMillis()+" str:"+str);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				
//			}
//		}).start();
//		
//		Timer timer=new Timer();//
//		timer.schedule(new java.util.TimerTask() {
//			
//			@Override
//			public void run() {
//				list.add("test string");
//				
//			}
//		}, 10000);
		
	}
	 
	 private static BlockingQueue<String> list=new LinkedBlockingDeque<>();
	 
	 
	 
	 


}

