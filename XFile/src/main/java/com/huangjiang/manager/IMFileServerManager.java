package com.huangjiang.manager;

import com.huangjiang.config.SysConstant;
import com.huangjiang.message.MessageServerThread;
import com.huangjiang.utils.Logger;

/**
 * 文件管理
 */
public class IMFileServerManager extends IMManager {

    private Logger logger = Logger.getLogger(IMMessageServerManager.class);


    private static IMFileServerManager inst = null;

    private MessageServerThread messageServerThread = null;

    public static IMFileServerManager getInstance() {
        if (inst == null) {
            inst = new IMFileServerManager();
        }
        return inst;
    }

    public IMFileServerManager() {
        messageServerThread = new MessageServerThread(SysConstant.FILE_SERVER_PORT);
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
