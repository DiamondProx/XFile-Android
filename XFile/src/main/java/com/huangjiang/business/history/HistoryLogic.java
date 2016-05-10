package com.huangjiang.business.history;

import android.content.Context;

import com.huangjiang.business.BaseLogic;
import com.huangjiang.business.event.*;
import com.huangjiang.business.model.FileType;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.core.ThreadPoolManager;
import com.huangjiang.dao.DFile;
import com.huangjiang.dao.DFileDao;
import com.huangjiang.dao.DaoMaster;
import com.huangjiang.manager.event.FileEvent;
import com.huangjiang.utils.XFileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 历史消息业务逻辑
 */
public class HistoryLogic extends BaseLogic {

    private HistoryInterface historyInterface;
    private Context context;
    private DFileDao fileDao;

    public HistoryLogic(Context context) {
        historyInterface = new HistoryInterface(context);
        this.context = context;
        this.fileDao = DaoMaster.getInstance().newSession().getDFileDao();
    }

    /**
     * 读取历史消息
     */
    public void getHistory() {
        ThreadPoolManager.getInstance(HistoryLogic.class.getName()).startTaskThread(new Runnable() {
            @Override
            public void run() {
                List<DFile> history = historyInterface.getHistory();
                if (history != null) {
                    List<TFileInfo> tFileInfoList = convertDFileToTFile(history);
                    triggerEvent(FindResEvent.MimeType.HISTORY, tFileInfoList);
                }
            }
        });
    }

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

    public void addTMessage(TFileInfo tFileInfo) {
        DFile dFile = XFileUtils.buildDFile(tFileInfo);
        fileDao.insert(dFile);
    }

    public List<TFileInfo> convertDFileToTFile(List<DFile> dList) {
        List<TFileInfo> list = new ArrayList<>();
        for (DFile dFile : dList) {
            TFileInfo tFileInfo = new TFileInfo();
            tFileInfo.setName(dFile.getName());
            tFileInfo.setPosition(dFile.getPosition());
            tFileInfo.setLength(dFile.getLength());
            tFileInfo.setExtension(dFile.getExtension());
            tFileInfo.setFullName(dFile.getFullName());
            tFileInfo.setTaskId(dFile.getTaskId());
            tFileInfo.setIsSend(dFile.getIsSend());
            tFileInfo.setFrom(dFile.getFrom());
//            tFileInfo.setPercent(dFile.getPercent());
            if (dFile.getIsSend()) {
                tFileInfo.setPath(dFile.getPath());
            } else {
                tFileInfo.setPath(dFile.getSavePath());
            }
            switch (dFile.getStatus()) {
                case 0:
                    tFileInfo.setFileEvent(FileEvent.SET_FILE_STOP);
                    break;
                case 1:
                    tFileInfo.setFileEvent(FileEvent.SET_FILE_SUCCESS);
                    break;
                case 2:
                    tFileInfo.setFileEvent(FileEvent.SET_FILE_FAILED);
                    break;
            }
            FileType fileType = XFileUtils.getFileType(context, dFile.getFullName());
            tFileInfo.setFileType(fileType);
            list.add(tFileInfo);
        }
        return list;
    }
}
