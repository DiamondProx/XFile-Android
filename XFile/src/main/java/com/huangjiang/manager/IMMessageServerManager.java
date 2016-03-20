package com.huangjiang.manager;

import com.huangjiang.config.SysConstant;
import com.huangjiang.message.MessageServerThread;
import com.huangjiang.utils.Logger;

/**
 * 消息服务管理
 */
public class IMMessageServerManager extends IMManager {

    private Logger logger = Logger.getLogger(IMMessageServerManager.class);

    private static IMMessageServerManager inst = null;

    private MessageServerThread messageServerThread = null;

    public static IMMessageServerManager getInstance() {
        if (inst == null) {
            inst = new IMMessageServerManager();
        }
        return inst;
    }

    public IMMessageServerManager() {
        messageServerThread = new MessageServerThread(SysConstant.MESSAGE_PORT);
    }

    @Override
    public void start() {
        messageServerThread.run();
    }

    @Override
    public void stop() {
        messageServerThread.stopServer();
    }
}
