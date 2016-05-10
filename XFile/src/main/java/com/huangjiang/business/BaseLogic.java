package com.huangjiang.business;

import com.huangjiang.business.event.FindResEvent;
import com.huangjiang.business.event.HistoryEvent;
import com.huangjiang.business.event.InstallEvent;
import com.huangjiang.business.event.OpFileEvent;
import com.huangjiang.business.event.RootEvent;
import com.huangjiang.business.model.TFileInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.List;


public class BaseLogic {

    public void triggerEvent(FindResEvent.MimeType mimeType, List<TFileInfo> fileInfoList) {
        FindResEvent searchTFileInfoEvent = new FindResEvent(mimeType, fileInfoList);
        EventBus.getDefault().post(searchTFileInfoEvent);
    }

    public void triggerEvent(OpFileEvent event) {
        EventBus.getDefault().post(event);
    }

    public void triggerEvent(RootEvent event) {
        EventBus.getDefault().post(event);
    }

    public void triggerEvent(InstallEvent event) {
        EventBus.getDefault().post(event);
    }

    public void triggerEvent(HistoryEvent event){
        EventBus.getDefault().post(event);
    }


}
