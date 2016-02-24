package com.huangjiang.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huangjiang.filetransfer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab工具条
 */
public class TabBar extends HorizontalScrollView {

    private LinearLayout mContainer;
    private Context mContext;
    private List<View> mChildViews;
    private Resources resources;
    private int dp_20, dp_5;
    ColorStateList white_color ;

    public TabBar(Context context) {
        super(context);

        init(context);
    }

    public TabBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TabBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    void init(Context context) {
        this.mContext = context;
        this.resources = context.getResources();
        mContainer = new LinearLayout(mContext);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) resources.getDimension(R.dimen.dp_40)
        );
        mContainer.setLayoutParams(p);
        mContainer.setBackgroundResource(R.color.group_header_select_color);
        this.addView(mContainer);
        mChildViews = new ArrayList<>();
        dp_20 = (int) this.resources.getDimension(R.dimen.dp_20);
        dp_5 = (int) this.resources.getDimension(R.dimen.dp_5);
        white_color = (ColorStateList) this.resources.getColorStateList(R.color.white);
//        this.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//        this.arrowScroll(View.FOCUS_LEFT);
        this.setHorizontalScrollBarEnabled(false);
    }

    public void setMenu(int... resID) {
        int size = resID == null ? 0 : resID.length;
        if (size == 0) {
            throw new NullPointerException();
        }
        for (int i = 0; i < size; i++) {
            FrameLayout childContainer = new FrameLayout(mContext);
            if ((resID[i] >= 0x7f020000 && resID[i] <= 0x7f02ffff) || (resID[i] >= 0x7f030000 && resID[i] <= 0x7f03ffff)) {
                // 图片资源
                ImageView childView = new ImageView(mContext);
                childView.setBackgroundResource(resID[i]);
                FrameLayout.LayoutParams childParams = new FrameLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT
                );
                childParams.gravity = Gravity.CENTER;
                childParams.setMargins(dp_20, dp_5, dp_20, dp_5);
                childView.setLayoutParams(childParams);
                childContainer.addView(childView);
                mContainer.addView(childContainer);
                mChildViews.add(childContainer);
            } else if ((resID[i] >= 0x7f060000 && resID[i] <= 0x7f06ffff)) {
                // 文字资源
                TextView childView = new TextView(mContext);
                FrameLayout.LayoutParams childParams = new FrameLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT
                );
                childParams.gravity = Gravity.CENTER;
                childParams.setMargins(dp_20, dp_5, dp_20, dp_5);
                childView.setLayoutParams(childParams);
                childView.setText(resID[i]);
                childView.setTextColor(white_color);
                childContainer.addView(childView);

                mContainer.addView(childContainer);
                mChildViews.add(childContainer);
            }
        }
    }

}
