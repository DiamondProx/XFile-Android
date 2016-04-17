package com.huangjiang.dao;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import com.huangjiang.dao.DFile;
import com.huangjiang.dao.DLinkDetail;
import com.huangjiang.dao.DTransferDetail;

import com.huangjiang.dao.DFileDao;
import com.huangjiang.dao.DLinkDetailDao;
import com.huangjiang.dao.DTransferDetailDao;

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

    private final DFileDao dFileDao;
    private final DLinkDetailDao dLinkDetailDao;
    private final DTransferDetailDao dTransferDetailDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        dFileDaoConfig = daoConfigMap.get(DFileDao.class).clone();
        dFileDaoConfig.initIdentityScope(type);

        dLinkDetailDaoConfig = daoConfigMap.get(DLinkDetailDao.class).clone();
        dLinkDetailDaoConfig.initIdentityScope(type);

        dTransferDetailDaoConfig = daoConfigMap.get(DTransferDetailDao.class).clone();
        dTransferDetailDaoConfig.initIdentityScope(type);

        dFileDao = new DFileDao(dFileDaoConfig, this);
        dLinkDetailDao = new DLinkDetailDao(dLinkDetailDaoConfig, this);
        dTransferDetailDao = new DTransferDetailDao(dTransferDetailDaoConfig, this);

        registerDao(DFile.class, dFileDao);
        registerDao(DLinkDetail.class, dLinkDetailDao);
        registerDao(DTransferDetail.class, dTransferDetailDao);
    }
    
    public void clear() {
        dFileDaoConfig.getIdentityScope().clear();
        dLinkDetailDaoConfig.getIdentityScope().clear();
        dTransferDetailDaoConfig.getIdentityScope().clear();
    }

    public DFileDao getDFileDao() {
        return dFileDao;
    }

    public DLinkDetailDao getDLinkDetailDao() {
        return dLinkDetailDao;
    }

    public DTransferDetailDao getDTransferDetailDao() {
        return dTransferDetailDao;
    }

}
