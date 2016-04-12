package com.huangjiang.view;

import android.app.Activity;
import android.view.View;

import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.filetransfer.R;

/**
 * 菜单帮助类
 */
public class MenuHelper {

    public static void showMenu(Activity context, View anchorView, TFileInfo tFileInfo) {

        PopupMenu menu = new PopupMenu(context);
        menu.setOnItemSelectedListener(new PopupMenu.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position, MenuItem item, TFileInfo tFileInfo) {

            }
        });
        menu.setOutPosition(1);
        menu.add(0, R.string.transfer).setIcon(context.getResources().getDrawable(R.mipmap.data_downmenu_send));
        menu.add(1, R.string.multi_select).setIcon(context.getResources().getDrawable(R.mipmap.data_downmenu_check));
        menu.add(2, R.string.play).setIcon(context.getResources().getDrawable(R.mipmap.data_downmenu_open));
        menu.add(3, R.string.more).setIcon(context.getResources().getDrawable(R.mipmap.data_downmenu_more));
        menu.show(anchorView);
    }

}
