package com.huangjiang.manager.callback;

import android.os.Handler;

import com.huangjiang.utils.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : yingmu on 15-1-7.
 * @email : yingmu@mogujie.com.
 */
public class MessageClientListenerQueue {

    private static MessageClientListenerQueue listenerQueue = new MessageClientListenerQueue();
    private Logger logger = Logger.getLogger(MessageClientListenerQueue.class);
    public static MessageClientListenerQueue instance(){
        return listenerQueue;
    }

    private volatile  boolean stopFlag = false;
    private volatile  boolean hasTask = false;


    //callback 队列
    private Map<Integer,Packetlistener> callBackQueue = new ConcurrentHashMap<>();
    private Handler timerHandler = new Handler();


    public void onStart(){
        logger.d("FileClientListenerQueue#onStart run");
        stopFlag = false;
        startTimer();
    }
    public void onDestory(){
        logger.d("FileClientListenerQueue#onDestory ");
        callBackQueue.clear();
        stopTimer();
    }

    //以前是TimerTask处理方式
    private void startTimer() {
        if(!stopFlag && hasTask == false) {
            hasTask = true;
            timerHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    timerImpl();
                    hasTask = false;
                    startTimer();
                }
            }, 8 * 1000);
        }
    }

    private void stopTimer(){
        stopFlag = true;
    }

    private void timerImpl() {
        long currentRealtime =   System.currentTimeMillis();//SystemClock.elapsedRealtime();

        for (Map.Entry<Integer, Packetlistener> entry : callBackQueue.entrySet()) {

            Packetlistener packetlistener = entry.getValue();
            Integer seqNo = entry.getKey();
            long timeRange = currentRealtime - packetlistener.getCreateTime();

            try {
                if (timeRange >= packetlistener.getTimeOut()) {
                    logger.d("FileClientListenerQueue#find timeout msg");
                    Packetlistener listener = pop(seqNo);
                    if (listener != null) {
                        listener.onTimeout();
                    }
                }
            } catch (Exception e) {
                logger.d("FileClientListenerQueue#timerImpl onTimeout is Error,exception is %s", e.getCause());
            }
        }
    }

    public void push(int seqNo,Packetlistener packetlistener){
        if(seqNo <=0 || null==packetlistener){
            logger.d("FileClientListenerQueue#push error, cause by Illegal params");
            return;
        }
        callBackQueue.put(seqNo,packetlistener);
    }


    public Packetlistener pop(int seqNo){
        synchronized (MessageClientListenerQueue.this) {
            if (callBackQueue.containsKey(seqNo)) {
                Packetlistener packetlistener = callBackQueue.remove(seqNo);
                return packetlistener;
            }
            return null;
        }
    }
}
