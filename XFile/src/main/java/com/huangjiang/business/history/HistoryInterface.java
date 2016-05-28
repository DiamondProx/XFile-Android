package com.huangjiang.business.history;

import android.content.Context;

import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.dao.TFileDao;
import com.huangjiang.dao.DaoMaster;
import com.huangjiang.utils.Logger;

import java.util.List;

/**
 * 历史消息
 */
public class HistoryInterface {

    private Logger logger = Logger.getLogger(HistoryInterface.class);

    private Context mContext;
    private TFileDao dFileDao;

    public HistoryInterface(Context context) {
        this.mContext = context;
        dFileDao = DaoMaster.getInstance().newSession().getFileDao();
    }

    public List<TFileInfo> getHistory() {
        return dFileDao.queryBuilder().orderDesc(TFileDao.Properties.Id).list();
    }
}
