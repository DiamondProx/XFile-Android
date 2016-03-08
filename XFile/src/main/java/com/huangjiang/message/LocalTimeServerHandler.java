package com.huangjiang.message;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.huangjiang.message.LocalTimeProtocol.Continent;
import com.huangjiang.message.LocalTimeProtocol.DayOfWeek;
import com.huangjiang.message.LocalTimeProtocol.LocalTime;
import com.huangjiang.message.LocalTimeProtocol.LocalTimes;
import com.huangjiang.message.LocalTimeProtocol.Location;
import com.huangjiang.message.LocalTimeProtocol.Locations;

public class LocalTimeServerHandler extends SimpleChannelUpstreamHandler{
	
	private static final Logger logger=Logger.getLogger(LocalTimeServerHandler.class.getName());
	
	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		System.out.println("--------handleUpstream");
		if(e instanceof ChannelStateEvent){
			logger.info(e.toString());
		}
		super.handleUpstream(ctx, e);
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		System.out.println("--------messageReceived");
		Locations locations=(Locations)e.getMessage();
		long currentTime=System.currentTimeMillis();
		LocalTimes.Builder builder=LocalTimes.newBuilder();
		for(Location l:locations.getLocationList()){
			TimeZone tz=TimeZone.getTimeZone(toString(l.getContinent())+'/'+l.getCity());
			Calendar calendar=Calendar.getInstance(tz);
			calendar.setTimeInMillis(currentTime);
			builder.addLocalTime(LocalTime.newBuilder().setYear(calendar.get(Calendar.YEAR))
					.setMonth(calendar.get(Calendar.MONTH)+1)
					.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH))
					.setDayOfWeek(DayOfWeek.valueOf(calendar.get(Calendar.DAY_OF_WEEK)))
					.setHour(calendar.get(Calendar.HOUR_OF_DAY))
					.setMinute(calendar.get(Calendar.MINUTE))
					.setSecond(calendar.get(Calendar.SECOND)).build()
					);
		}
		System.out.println("currentTime:"+currentTime);
		Thread.sleep(10000);
		e.getChannel().write(builder.build());
		System.out.println("write success:"+System.currentTimeMillis());
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		logger.log(  
                Level.WARNING,  
                "Unexpected exception from downstream.",  
                e.getCause());  
		System.out.println("--------exceptionCaught:"+e.getCause().getMessage());
		e.getChannel().close();
	}
	
	 private static String toString(Continent c) {  
	        return "" + c.name().charAt(0) + c.name().toLowerCase().substring(1);  
	    }  
	
}
