package com.huangjiang.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.view.OpenFileHelper;
import com.huangjiang.xfile.R;
import com.huangjiang.manager.IMFileManager;
import com.huangjiang.manager.event.FileEvent;
import com.huangjiang.utils.XFileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件传送适配器
 */
public class TransmitAdapter extends BaseAdapter implements View.OnClickListener {

    private LayoutInflater mInflater;
    private Context mContext;
    List<TFileInfo> listTFileInfo;
    int RECEIVE_MESSAGE;
    int SEND_MESSAGE;
    String createFileSuccess, createFileFailed, checkTaskSuccess, checkTaskFailed,
            setFileComplete, setFileFailed, setFileTransmit, setFileStop, setFileWaiting,
            retry, view, resume, stop;
    int redColor, blackColor;


    public TransmitAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
        createFileSuccess = context.getString(R.string.create_file_success);
        createFileFailed = context.getString(R.string.create_file_failed);
        checkTaskSuccess = context.getString(R.string.check_task_success);
        checkTaskFailed = context.getString(R.string.check_task_failed);
        setFileComplete = context.getString(R.string.set_file_complete);
        setFileFailed = context.getString(R.string.set_file_failed);
        setFileTransmit = context.getString(R.string.set_file_transmit);
        setFileStop = context.getString(R.string.set_file_stop);
        setFileWaiting = context.getString(R.string.set_file_waiting);
        retry = context.getString(R.string.retry);
        view = context.getString(R.string.view);
        resume = context.getString(R.string.resume);
        stop = context.getString(R.string.stop);
        redColor = mContext.getResources().getColor(R.color.red);
        blackColor = mContext.getResources().getColor(R.color.black);
        listTFileInfo = new ArrayList<>();
    }

    public void setList(List<TFileInfo> list) {
        listTFileInfo = list;
    }

    @Override
    public int getCount() {
        return listTFileInfo == null ? 0 : listTFileInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return listTFileInfo == null ? null : listTFileInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        TFileInfo message = listTFileInfo.get(position);
        if (!message.isSend()) {
            return RECEIVE_MESSAGE;
        } else {
            return SEND_MESSAGE;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TFileInfo tFileInfo = listTFileInfo.get(position);
        boolean isSend = tFileInfo.isSend();
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            if (!isSend) {
                convertView = mInflater.inflate(R.layout.listview_transfer_message_revice, null);
                holder.btn_step = (Button) convertView.findViewById(R.id.setup);
            } else {
                convertView = mInflater.inflate(R.layout.listview_transfer_message_send, null);
            }
            holder.headImg = (ImageView) convertView.findViewById(R.id.head);
            holder.fileImg = (ImageView) convertView.findViewById(R.id.fileImg);
            holder.from = (TextView) convertView.findViewById(R.id.from);
            holder.name = (TextView) convertView.findViewById(R.id.fileName);
            holder.size = (TextView) convertView.findViewById(R.id.fileSize);
            holder.remainPercent = (TextView) convertView.findViewById(R.id.remainPercent);
            holder.status = (TextView) convertView.findViewById(R.id.status);
            holder.line1 = (TextView) convertView.findViewById(R.id.tv_line1);
            holder.line2 = (TextView) convertView.findViewById(R.id.tv_line2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        initFileInfoView(holder, tFileInfo);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        String taskId = v.getTag().toString();
        TFileInfo taskInfo = getTFileByTaskId(taskId);
        if (taskInfo == null) {
            return;
        }
        switch (taskInfo.getFileEvent()) {
            case SET_FILE_STOP:
                IMFileManager.getInstance().resumeReceive(taskInfo);
                break;
            case SET_FILE:
                IMFileManager.getInstance().stopReceive();
                break;
            case SET_FILE_SUCCESS:
                OpenFileHelper.openFile(mContext, taskInfo);
                break;
        }
    }

    public final class ViewHolder {
        ImageView headImg;
        ImageView fileImg;
        TextView from;
        TextView name;
        TextView size;
        TextView remainPercent;
        TextView status;
        TextView line1, line2;
        Button btn_step;
    }

    void initFileInfoView(ViewHolder holder, TFileInfo tFileInfo) {
        holder.headImg.setImageResource(R.mipmap.avatar_default);
        holder.fileImg.setImageResource(R.mipmap.data_folder_documents_placeholder);
        if (tFileInfo.isSend()) {
            holder.from.setText(String.format(mContext.getString(R.string.send_to), tFileInfo.getFrom()));
        } else {
            holder.from.setText(String.format(mContext.getString(R.string.receive_from), tFileInfo.getFrom()));
        }
        holder.name.setText(tFileInfo.getFullName());
        holder.size.setText(XFileUtils.parseSize(tFileInfo.getLength()));
        long percent = tFileInfo.getPosition() * 100 / tFileInfo.getLength();
        holder.remainPercent.setText(String.format(mContext.getString(R.string.percent), percent));
        if (tFileInfo.getFileEvent() == FileEvent.NONE) {
            holder.status.setVisibility(View.GONE);
            holder.line1.setVisibility(View.GONE);
        } else {
            holder.status.setText(getState(tFileInfo.getFileEvent()));
        }
        // 接受者才显示箭头按钮
        if (!tFileInfo.isSend()) {
            setStepState(holder.btn_step, tFileInfo.getFileEvent());
            holder.btn_step.setTag(tFileInfo.getTaskId());
            holder.btn_step.setOnClickListener(this);
        }
    }

    public TFileInfo getWaitFile() {
        for (TFileInfo fileInfo : listTFileInfo) {
            if (fileInfo.getFileEvent().equals(FileEvent.WAITING)) {
                return fileInfo;
            }
        }
        return null;
    }

    public void addTFileInfo(TFileInfo tFileInfo) {
        listTFileInfo.add(tFileInfo);
    }


    public TFileInfo getTFileByTaskId(String taskId) {
        for (TFileInfo fileInfo : listTFileInfo) {
            if (taskId.equals(fileInfo.getTaskId())) {
                return fileInfo;
            }
        }
        return null;
    }

    public void cancelTask(TFileInfo tFileInfo) {
        for (TFileInfo fileInfo : listTFileInfo) {
            if (tFileInfo.getTaskId().equals(fileInfo.getTaskId())) {
                listTFileInfo.remove(fileInfo);
                break;
            }
        }
    }

    public int getPosition(TFileInfo tFileInfo) {
        int position = -1;
        for (TFileInfo fileInfo : listTFileInfo) {
            position++;
            if (tFileInfo.getTaskId().equals(fileInfo.getTaskId())) {
                return position;
            }

        }
        return position;
    }

    public void updateTFileInfo(TFileInfo tFileInfo) {
        for (TFileInfo fileInfo : listTFileInfo) {
            if (tFileInfo.getTaskId().equals(fileInfo.getTaskId())) {
                fileInfo.setPercent(tFileInfo.getPercent());
                fileInfo.setPosition(tFileInfo.getPosition());
                fileInfo.setFileEvent(tFileInfo.getFileEvent());
                break;
            }
        }
    }

    public void updateTransmitState(ViewHolder holder, TFileInfo tFileInfo) {
        initFileInfoView(holder, tFileInfo);
    }


    /**
     * 设置传输状态
     */
    String getState(FileEvent fileEvent) {
        String stateStr = "";
        switch (fileEvent) {
            case CREATE_FILE_SUCCESS:
                stateStr = createFileSuccess;
                break;
            case CREATE_FILE_FAILED:
                stateStr = createFileFailed;
                break;
            case CHECK_TASK_SUCCESS:
                stateStr = checkTaskSuccess;
                break;
            case CHECK_TASK_FAILED:
                stateStr = checkTaskFailed;
                break;
            case SET_FILE_SUCCESS:
                stateStr = setFileComplete;
                break;
            case SET_FILE_FAILED:
                stateStr = setFileFailed;
                break;
            case SET_FILE_STOP:
                stateStr = setFileStop;
                break;
            case SET_FILE:
                stateStr = setFileTransmit;
                break;
            case WAITING:
                stateStr = setFileWaiting;
                break;
        }
        return stateStr;
    }


    void setStepState(Button btnStep, FileEvent fileEvent) {
        switch (fileEvent) {
            case CREATE_FILE_SUCCESS:
            case CHECK_TASK_SUCCESS:
                btnStep.setVisibility(View.GONE);
                break;
            case CREATE_FILE_FAILED:
            case CHECK_TASK_FAILED:
            case SET_FILE_FAILED:
                btnStep.setText(retry);
                btnStep.setVisibility(View.VISIBLE);
                break;
            case SET_FILE_SUCCESS:
                btnStep.setText(view);
                btnStep.setVisibility(View.VISIBLE);
                break;
            case SET_FILE_STOP:
                btnStep.setText(resume);
                btnStep.setVisibility(View.VISIBLE);
                break;
            case SET_FILE:
                btnStep.setText(stop);
                btnStep.setVisibility(View.VISIBLE);
                break;
        }
    }


}
