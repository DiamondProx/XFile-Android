package com.huangjiang.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.huangjiang.activity.HomeActivity;
import com.huangjiang.adapter.CatalogAdapter;
import com.huangjiang.adapter.ExplorerAdapter;
import com.huangjiang.business.event.OpFileEvent;
import com.huangjiang.business.explorer.ExplorerLogic;
import com.huangjiang.business.model.Catalog;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.business.opfile.OpLogic;
import com.huangjiang.xfile.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * 文件浏览器
 */
public class ExplorerControl extends FrameLayout implements OnItemClickListener, View.OnClickListener, PopupMenu.MenuCallback, CustomDialog.DialogCallback {

    private Context mContext;
    private String rootFilePath = "";
    private String currentFilePath = "/";
    private ExplorerAdapter explorerAdapter;
    private CatalogAdapter catalogAdapter;
    private ListView catalogListView;
    private LinearLayout explorerLayout;
    private ExplorerLogic explorerLogic;
    private ListView explorerListView;
    private Activity activity;
    private OpLogic opLogic;

    public ExplorerControl(Context context) {
        super(context);
        init(context);
    }

    public ExplorerControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    void init(Context context) {

        mContext = context;
        setBackgroundResource(R.color.white);
        // 目录布局
        catalogListView = new ListView(context);
        catalogListView.setId(R.id.listview_catalog_id);
        LayoutParams catalogParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        ColorDrawable dividerColor = new ColorDrawable(mContext.getResources().getColor(R.color.listview_divider_color));
        catalogListView.setDivider(dividerColor);
        catalogListView.setDividerHeight(1);
        catalogAdapter = new CatalogAdapter(mContext);
        catalogListView.setAdapter(catalogAdapter);
        catalogListView.setOnItemClickListener(this);
        addView(catalogListView, catalogParams);

        // 文件布局
        explorerLayout = new LinearLayout(context);
        LayoutParams explorerLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        explorerLayout.setOrientation(LinearLayout.VERTICAL);
        addView(explorerLayout, explorerLayoutParams);

        // 上一级布局
        LinearLayout headerLayout = new LinearLayout(context);
        headerLayout.setId(R.id.fileBrowserUpdir);
        int headerHeight = (int) mContext.getResources().getDimension(R.dimen.dp_40);
        LayoutParams headerLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, headerHeight);
        headerLayout.setLayoutParams(headerLayoutParams);
        headerLayout.setBackgroundResource(R.drawable.updir_selector);
        headerLayout.setGravity(Gravity.CENTER_VERTICAL);
        headerLayout.setOnClickListener(this);
        explorerLayout.addView(headerLayout);

        // 上一级箭头
        ImageView headerUpDirIcon = new ImageView(context);
        LayoutParams headerIconParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        headerIconParams.setMargins(0, 0, 0, 0);
        headerUpDirIcon.setLayoutParams(headerIconParams);
        headerUpDirIcon.setBackgroundResource(R.mipmap.data_folder_dir);
        headerLayout.addView(headerUpDirIcon);

        // 上一级文本
        TextView headerUpDir = new TextView(context);
        LayoutParams headerPathParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        headerPathParams.setMargins(0, 0, 0, 0);
        headerUpDir.setLayoutParams(headerPathParams);
        headerUpDir.setText(R.string.up_dir);
        ColorStateList csl = context.getResources().getColorStateList(R.color.font_txt_gray);
        headerUpDir.setTextColor(csl);
        headerLayout.addView(headerUpDir);

        // 文件浏览
        explorerListView = new ListView(context);
        explorerListView.setId(R.id.listview_explorer_id);
        LayoutParams headerFileListParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        explorerListView.setLayoutParams(headerFileListParams);
        explorerListView.setBackgroundResource(R.color.app_background);
        explorerLayout.addView(explorerListView);
        explorerAdapter = new ExplorerAdapter(context);
        explorerListView.setAdapter(explorerAdapter);
        explorerListView.setOnItemClickListener(this);

        explorerLogic = new ExplorerLogic(mContext);
        opLogic = new OpLogic(mContext);
        EventBus.getDefault().register(this);

    }


    public void getFiles(String dirPath) {
        List<TFileInfo> list = explorerLogic.getCatalogFiles(dirPath);
        Collections.sort(list);
        explorerAdapter.addTFiles(list);
        explorerAdapter.notifyDataSetChanged();
        explorerListView.setSelection(0);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * 设置导航目录
     */
    public void setCatalog(List<Catalog> catalogList) {
        explorerLayout.setVisibility(GONE);
        catalogListView.setVisibility(VISIBLE);
        catalogAdapter.addCatalogs(catalogList);
        catalogAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (R.id.listview_catalog_id == adapterView.getId()) {
            Catalog catalog = (Catalog) catalogAdapter.getItem(position);
            currentFilePath = rootFilePath = catalog.getPath();
            getFiles(catalog.getPath());
            explorerLayout.setVisibility(VISIBLE);
        } else if (R.id.listview_explorer_id == adapterView.getId()) {
            TFileInfo tFileInfo = (TFileInfo) explorerAdapter.getItem(position);
            if (tFileInfo.isDirectory()) {
                // 点击目录
                currentFilePath = tFileInfo.getPath();
                getFiles(tFileInfo.getPath());
            } else {
                // 文件操作
                MenuHelper.showMenu(activity, view, position, tFileInfo, ExplorerControl.this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fileBrowserUpdir:
                upDirectory();
                break;
        }
    }

    /**
     * 返回上一级目录
     */
    void upDirectory() {
        if (currentFilePath.equals(File.separator) || rootFilePath.equals(currentFilePath)) {
            // 根目录
            explorerLayout.setVisibility(GONE);
            explorerAdapter.getTFiles().clear();
            explorerAdapter.notifyDataSetChanged();
            return;
        }
        int lastIndex = currentFilePath.lastIndexOf(File.separator);
        if (lastIndex == 0) {
            currentFilePath = File.separator;
            getFiles(currentFilePath);
            return;
        }
        currentFilePath = currentFilePath.substring(0, lastIndex);
        getFiles(currentFilePath);
    }


    @Override
    public void onDialogClick(int id, TFileInfo tFileInfo, Object... params) {
        switch (id) {
            case R.id.more_del:
                DialogHelper.confirmDel(activity, tFileInfo, ExplorerControl.this);
                break;
            case R.id.more_rename:
                DialogHelper.showRename(activity, tFileInfo, ExplorerControl.this);
                break;
            case R.id.more_property:
                DialogHelper.showProperty(activity, tFileInfo);
                break;
            case R.id.dialog_confirm_ok:
                opLogic.deleteFile(tFileInfo);
                break;
            case R.id.dialog_rename_ok:
                opLogic.renameFile(tFileInfo, (String) params[0], OpFileEvent.Target.EXPLORER_CONTROL);
                break;
        }
    }

    @Override
    public void onMenuClick(PopupMenu menu, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_transfer:
                View view = explorerListView.getChildAt(menu.getItemPosition() - explorerListView.getFirstVisiblePosition());
                if (view != null) {
                    ImageView image = (ImageView) view.findViewById(R.id.image);
                    if (image != null && activity instanceof HomeActivity) {
                        HomeActivity homeActivity = (HomeActivity) activity;
                        homeActivity.sendTFile(menu.getTFileInfo(), image);
                    }
                }
                break;
            case R.id.menu_open:
                OpenFileHelper.openFile(activity, menu.getTFileInfo());
                break;
            case R.id.menu_more:
                DialogHelper.showMore(activity, menu.getTFileInfo(), ExplorerControl.this);
                break;
        }
    }

    /**
     * 文件操作
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OpFileEvent opFileEvent) {
        switch (opFileEvent.getOpType()) {
            case DELETE:
                if (opFileEvent.isSuccess()) {
                    explorerAdapter.removeFile(opFileEvent.getTFileInfo());
                    explorerAdapter.notifyDataSetChanged();
                }
                break;
            case RENAME:
                if (opFileEvent.getTarget() == OpFileEvent.Target.EXPLORER_CONTROL) {
                    if (!opFileEvent.isSuccess()) {
                        Toast.makeText(mContext, opFileEvent.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    explorerAdapter.updateFile(opFileEvent.getTFileInfo());
                    explorerAdapter.notifyDataSetChanged();
                }
                break;
        }

    }

    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }
}
