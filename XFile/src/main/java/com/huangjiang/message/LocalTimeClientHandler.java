package com.huangjiang.message;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class LocalTimeClientHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = Logger.getLogger(LocalTimeClientHandler.class.getName());

    private final BlockingQueue<LocalTimeProtocol.LocalTimes> answer = new LinkedBlockingDeque<>();
    // Stateful properties
    private volatile Channel channel;

    public List<String> getLocalTimes(Collection<String> cities) {
        System.out.println("---getLocalTimes:"+System.currentTimeMillis());
        LocalTimeProtocol.Locations.Builder builder = LocalTimeProtocol.Locations.newBuilder();
        for (String c : cities) {
            String[] componets = c.split("/");
//            builder.addLocation(LocalTimeProtocol.Location.newBuilder().setContinent(LocalTimeProtocol.Continent.valueOf(componets[0].toLowerCase())).setCity(componets[1]).build());
            builder.addLocation(LocalTimeProtocol.Location.newBuilder().
                    setContinent(LocalTimeProtocol.Continent.valueOf(componets[0].toUpperCase())).
                    setCity(componets[1]).build());
        }
        channel.write(builder.build());
        LocalTimeProtocol.LocalTimes localTimes;
        boolean interrupted = false;
        for (; ; ) {
            try {
                localTimes = answer.take();
                break;
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        System.out.println("---take success:"+System.currentTimeMillis());
        if (interrupted) {
            Thread.currentThread().interrupt();

        }

        List<String> result = new ArrayList<String>();
        for (LocalTimeProtocol.LocalTime lt : localTimes.getLocalTimeList()) {
            result.add(
                    new Formatter().format(
                            "%4d-%02d-%02d %02d:%02d:%02d %s",
                            lt.getYear(),
                            lt.getMonth(),
                            lt.getDayOfMonth(),
                            lt.getHour(),
                            lt.getMinute(),
                            lt.getSecond(),
                            lt.getDayOfWeek().name()).toString());
        }
        return result;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        System.out.println("-----handleUpstream");
        if (e instanceof ChannelStateEvent) {
            logger.info(e.toString());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println("-----channelOpen");
        channel = e.getChannel();
        super.channelOpen(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        System.out.println("-----messageReceived");
        boolean offered = answer.offer((LocalTimeProtocol.LocalTimes) e.getMessage());
        assert offered;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.log(Level.WARNING, "Unexpected exception from downstream.", e.getCause());
        System.out.println("-----exceptionCaught-----:"+e.getCause().getMessage());
        e.getChannel().close();
    }
}
