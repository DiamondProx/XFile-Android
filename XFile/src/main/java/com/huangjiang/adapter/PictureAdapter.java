package com.huangjiang.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.huangjiang.business.comparable.CreateDateComparable;
import com.huangjiang.business.model.FileType;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.core.ImageLoader;
import com.huangjiang.xfile.R;
import com.huangjiang.utils.XFileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 图片浏览适配器
 */
public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.TextViewHolder> {

    public static final int ITEM_VIEW_TYPE_HEADER = 0;
    public static final int ITEM_VIEW_TYPE_ITEM = 1;

    private Context context;
    private int itemSize;
    private CallBack callBack;

    public PictureAdapter(Context context) {
        this.context = context;
        this.itemSize = XFileUtils.getScreenWidth(context) / 4;

    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    private List<TFileInfo> pictures;
    private List<TFileInfo> headerPictures;

    public void setPictures(List<TFileInfo> list) {
        pictures = list;
        headerPictures = new ArrayList<>();
        for (int i = 0; i < pictures.size(); i++) {
            TFileInfo tFileInfo = pictures.get(i);
            TFileInfo headerFile = new TFileInfo();
            if (i == 0) {
                headerFile.setCreateTime(tFileInfo.getCreateTime());
                headerPictures.add(headerFile);
            } else {
                int prevIndex = i - 1;
                boolean isGroup = !pictures.get(prevIndex).getCreateTime().equals(tFileInfo.getCreateTime());
                if (isGroup) {
                    headerFile.setCreateTime(tFileInfo.getCreateTime());
                    headerPictures.add(headerFile);
                }
            }
            headerPictures.add(tFileInfo);
        }
        Collections.sort(pictures, new CreateDateComparable());
    }

    public void removeFile(TFileInfo tFileInfo) {
        for (TFileInfo file : pictures) {
            if (tFileInfo.getFileType() == FileType.Apk) {
                if (tFileInfo.getPackageName().equals(file.getPackageName())) {
                    pictures.remove(file);
                    break;
                }
            } else {
                if (tFileInfo.getTaskId().equals(file.getTaskId())) {
                    pictures.remove(file);
                    break;
                }
            }

        }
        setPictures(pictures);
    }

    public void updateFile(TFileInfo tFileInfo) {
        for (TFileInfo file : pictures) {
            if (file.getTaskId().equals(tFileInfo.getTaskId())) {
                file.setName(tFileInfo.getName());
                file.setPosition(tFileInfo.getPosition());
                file.setLength(tFileInfo.getLength());
                file.setPath(tFileInfo.getPath());
                file.setExtension(tFileInfo.getExtension());
                file.setFullName(tFileInfo.getFullName());
                break;
            }
        }
        setPictures(pictures);
    }


    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (ITEM_VIEW_TYPE_HEADER == viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_gridview_picture_header, parent, false);
            return new TextViewHolder(view, viewType);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picture_recyclerview_item, parent, false);
        return new TextViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(TextViewHolder holder, int position) {
        holder.setTFileInfoView(position);
    }

    @Override
    public int getItemCount() {
        return headerPictures == null ? 0 : headerPictures.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return ITEM_VIEW_TYPE_HEADER;
        TFileInfo tFileInfo = headerPictures.get(position);
        String currentDate = tFileInfo.getCreateTime();
        int prevIndex = position - 1;
        boolean isGroup = !headerPictures.get(prevIndex).getCreateTime().equals(currentDate);
        return isGroup ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_ITEM;
    }

    public interface CallBack {
        void onGridItemClick(View view, int position, TFileInfo tFileInfo);
    }

    class TextViewHolder extends RecyclerView.ViewHolder {

        private int viewType;
        public TextView createTime;
        private ImageView itemImage;

        public TextViewHolder(View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            if (viewType == ITEM_VIEW_TYPE_HEADER) {
                createTime = (TextView) itemView.findViewById(R.id.headerName);
            } else if (viewType == ITEM_VIEW_TYPE_ITEM) {
                itemImage = (ImageView) itemView.findViewById(R.id.item_image);
            }
        }

        public void setTFileInfoView(final int position) {
            final TFileInfo tFileInfo = headerPictures.get(position);
            if (viewType == ITEM_VIEW_TYPE_HEADER) {
                createTime.setText(tFileInfo.getCreateTime());
            } else if (viewType == ITEM_VIEW_TYPE_ITEM) {
                ViewGroup.LayoutParams layoutParams = itemImage.getLayoutParams();
                layoutParams.height = itemSize;
                layoutParams.width = itemSize;
                itemImage.setLayoutParams(layoutParams);
                itemImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (callBack != null) {
                            callBack.onGridItemClick(view, position, tFileInfo);
                        }
                    }
                });
                ImageLoader.getInstance().displayThumb(itemImage, tFileInfo);
            }
        }

    }

}
