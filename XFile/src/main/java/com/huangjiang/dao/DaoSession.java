package com.huangjiang.dao;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import com.huangjiang.business.model.TFileInfo;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig dFileDaoConfig;
    private final DaoConfig dLinkDetailDaoConfig;
    private final DaoConfig dTransferDetailDaoConfig;

    private final TFileDao dFileDao;
    private final LinkDetailDao dLinkDetailDao;
    private final TransferDetailDao dTransferDetailDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        dFileDaoConfig = daoConfigMap.get(TFileDao.class).clone();
        dFileDaoConfig.initIdentityScope(type);

        dLinkDetailDaoConfig = daoConfigMap.get(LinkDetailDao.class).clone();
        dLinkDetailDaoConfig.initIdentityScope(type);

        dTransferDetailDaoConfig = daoConfigMap.get(TransferDetailDao.class).clone();
        dTransferDetailDaoConfig.initIdentityScope(type);

        dFileDao = new TFileDao(dFileDaoConfig, this);
        dLinkDetailDao = new LinkDetailDao(dLinkDetailDaoConfig, this);
        dTransferDetailDao = new TransferDetailDao(dTransferDetailDaoConfig, this);

        registerDao(TFileInfo.class, dFileDao);
        registerDao(LinkDetail.class, dLinkDetailDao);
        registerDao(TransferDetail.class, dTransferDetailDao);
    }
    
    public void clear() {
        dFileDaoConfig.getIdentityScope().clear();
        dLinkDetailDaoConfig.getIdentityScope().clear();
        dTransferDetailDaoConfig.getIdentityScope().clear();
    }

    public TFileDao getFileDao() {
        return dFileDao;
    }

    public LinkDetailDao getLinkDetailDao() {
        return dLinkDetailDao;
    }

    public TransferDetailDao getTransferDetailDao() {
        return dTransferDetailDao;
    }

}
