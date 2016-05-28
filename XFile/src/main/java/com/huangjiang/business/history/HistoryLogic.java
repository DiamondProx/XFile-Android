package com.huangjiang.business.history;

import android.content.Context;

import com.huangjiang.business.BaseLogic;
import com.huangjiang.business.event.FindResEvent;
import com.huangjiang.business.event.HistoryEvent;
import com.huangjiang.business.event.RecordEvent;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.core.ThreadPoolManager;
import com.huangjiang.dao.TFileDao;
import com.huangjiang.dao.LinkDetailDao;
import com.huangjiang.dao.TransferDetailDao;
import com.huangjiang.dao.DaoMaster;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.XFileUtils;

import java.util.List;

/**
 * 历史消息业务逻辑
 */
public class HistoryLogic extends BaseLogic {

    private Logger logger = Logger.getLogger(HistoryLogic.class);
    private HistoryInterface historyInterface;
    private Context context;
    private TFileDao fileDao;
    private LinkDetailDao linkDetailDao;
    private TransferDetailDao transferDetailDao;

    public HistoryLogic(Context context) {
        historyInterface = new HistoryInterface(context);
        this.context = context;
        this.fileDao = DaoMaster.getInstance().newSession().getFileDao();
        this.linkDetailDao = DaoMaster.getInstance().newSession().getLinkDetailDao();
        this.transferDetailDao = DaoMaster.getInstance().newSession().getTransferDetailDao();
    }

    /**
     * 读取历史消息
     */
    public void getHistory() {
        ThreadPoolManager.getInstance(HistoryLogic.class.getName()).startTaskThread(new Runnable() {
            @Override
            public void run() {
                List<TFileInfo> history = historyInterface.getHistory();
                if (history != null) {
                    for (TFileInfo file : history) {
                        file.setFileType(XFileUtils.getFileType(context, file.getFullName()));
                    }
                    triggerEvent(FindResEvent.MimeType.HISTORY, history);
                }
            }
        });
    }

    /**
     * 删除所有历史消息
     */
    public void delAllHistory() {
        ThreadPoolManager.getInstance(HistoryLogic.class.getName()).startTaskThread(new Runnable() {
            @Override
            public void run() {
                HistoryEvent historyEvent = new HistoryEvent();
                try {
                    fileDao.deleteAll();
                    historyEvent.setSuccess(true);
                } catch (Exception e) {
                    historyEvent.setSuccess(false);
                }
                triggerEvent(historyEvent);
            }
        });
    }

    /**
     * 连接计数+1
     */
    public void addOneConnect(final String deviceId) {
        ThreadPoolManager.getInstance(HistoryLogic.class.getName()).startTaskThread(new Runnable() {
            @Override
            public void run() {
                try {
                    linkDetailDao.addCount(deviceId);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.e(e.getMessage());
                }

            }
        });
    }

    /**
     * 连接统计
     */
    public void getRecordInfo() {
        ThreadPoolManager.getInstance(HistoryLogic.class.getName()).startTaskThread(new Runnable() {
            @Override
            public void run() {
                try {
                    int deviceCount = linkDetailDao.getDeviceCount();
                    int connectCount = linkDetailDao.getConnectCount();
                    long totalSize = transferDetailDao.getTotalSize();
                    RecordEvent recordEvent = new RecordEvent();
                    recordEvent.setDeviceCount(deviceCount);
                    recordEvent.setConnectCount(connectCount);
                    recordEvent.setTotalSize(totalSize);
                    triggerEvent(recordEvent);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.e(e.getMessage());
                }

            }
        });
    }

}
