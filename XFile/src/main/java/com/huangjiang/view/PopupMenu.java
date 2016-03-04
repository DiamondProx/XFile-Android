package com.huangjiang.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.huangjiang.filetransfer.R;

import java.util.ArrayList;
import java.util.List;

public class PopupMenu {

    private Context mContext;
    private LayoutInflater mInflater;
    private WindowManager mWindowManager;

    private PopupWindow mPopupWindow;
    private View mContentView;
    private ListViewForScrollView mItemsView;
    private OnItemSelectedListener mListener;

    private List<MenuItem> mItems;
    private int mWidth = 240;
    private float mScale;
    int dp_90;
    int margin_top;
    int margin_buttom;
    /**
     * 资源对象
     */
    protected Resources resources;

    public PopupMenu(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScale = metrics.scaledDensity;
        resources = context.getResources();
        dp_90 = (int) resources.getDimension(R.dimen.dp_90);
        margin_top = (int) resources.getDimension(R.dimen.dp_20);
        margin_buttom = (int) resources.getDimension(R.dimen.dp_20);

        mItems = new ArrayList<MenuItem>();

        mPopupWindow = new PopupWindow(context);
        mPopupWindow.setTouchInterceptor(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    mPopupWindow.dismiss();
                    return true;
                }
                return false;
            }
        });

        setContentView(mInflater.inflate(R.layout.popup_menu, null));
    }

    /**
     * Sets the popup's content.
     *
     * @param contentView
     */
    private void setContentView(View contentView) {
        mContentView = contentView;
        mItemsView = (ListViewForScrollView) contentView.findViewById(R.id.items);

        mPopupWindow.setContentView(contentView);
    }

    /**
     * Add menu item.
     *
     * @param itemId
     * @param titleRes
     * @return item
     */
    public MenuItem add(int itemId, int titleRes) {
        MenuItem item = new MenuItem();
        item.setItemId(itemId);
        item.setTitle(mContext.getString(titleRes));
        mItems.add(item);

        return item;
    }

    /**
     * Show popup menu.
     */
    public void show() {
        show(null);
    }

    /**
     * Show popup menu.
     *
     * @param anchor
     */
    public void show(View anchor) {

        if (mItems.size() == 0) {
            throw new IllegalStateException("PopupMenu#add was not called with a menu item to display.");
        }

        preShow();

        MenuItemAdapter adapter = new MenuItemAdapter(mContext, mItems);
        mItemsView.setAdapter(adapter);
        mItemsView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                if (mListener != null) {
                    mListener.onItemSelected(mItems.get(position));
                }
                mPopupWindow.dismiss();
            }
        });

        if (anchor == null) {
            View parent = ((Activity) mContext).getWindow().getDecorView();
            mPopupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
            return;
        }

        int xPos, yPos;
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);

        Rect anchorRect = new Rect(location[0], location[1],
                location[0] + anchor.getWidth(),
                location[0] + anchor.getHeight());

        mContentView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mContentView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        int rootHeight = mContentView.getMeasuredHeight();
        int screenHeight = mWindowManager.getDefaultDisplay().getHeight();

        // My popup style
        xPos = anchorRect.centerX() - mPopupWindow.getWidth() / 2;
        if (rootHeight < screenHeight - (anchorRect.top + (anchorRect.bottom / 2)) + margin_buttom) {
            adapter.setShowUp(true);
            yPos = anchorRect.top + (anchorRect.bottom / 2);
        } else {
            adapter.setShowUp(false);
            yPos = anchorRect.top + (anchorRect.bottom / 2) - rootHeight;
        }


//        // Set x-coordinate to display the popup menu
//        xPos = anchorRect.centerX() - mPopupWindow.getWidth() / 2;
//
//        int dyTop = anchorRect.top;
//        int dyBottom = screenHeight + rootHeight;
//        boolean onTop = dyTop > dyBottom;
//
//        // Set y-coordinate to display the popup menu
//        if (onTop) {
//            yPos = anchorRect.top - rootHeight;
//        } else {
//            if (anchorRect.bottom > dyTop) {
//                yPos = anchorRect.bottom - 20;
//            } else {
//                yPos = anchorRect.top - anchorRect.bottom + 50;
//            }
//        }
        adapter.notifyDataSetChanged();
        mPopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);

    }

    private void preShow() {
//        int width = (int) (mWidth * mScale);
//        mPopupWindow.setWidth(width);
        mPopupWindow.setWidth(dp_90);
        mPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        //mPopupWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.panel_background));
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
    }

    /**
     * Dismiss the popup menu.
     */
    public void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }


    /**
     * Change the popup's width.
     *
     * @param width
     */
    public void setWidth(int width) {
        mWidth = width;
    }

    /**
     * Register a callback to be invoked when an item in this PopupMenu has
     * been selected.
     *
     * @param listener
     */
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mListener = listener;
    }

    /**
     * Interface definition for a callback to be invoked when
     * an item in this PopupMenu has been selected.
     */
    public interface OnItemSelectedListener {
        public void onItemSelected(MenuItem item);
    }

    static class ViewHolder {
        ImageView icon;
        TextView title;
        ImageView arrow_up;
        ImageView arrow_down;
        LinearLayout content_layout;
    }

    private class MenuItemAdapter extends ArrayAdapter<MenuItem> {


        private boolean showUp = true;

        public MenuItemAdapter(Context context, List<MenuItem> objects) {
            super(context, 0, objects);
        }

        public void setShowUp(boolean showUp) {
            this.showUp = showUp;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.menu_list_item, null);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.arrow_up = (ImageView) convertView.findViewById(R.id.arrow_up);
                holder.arrow_down = (ImageView) convertView.findViewById(R.id.arrow_down);
                holder.content_layout = (LinearLayout) convertView.findViewById(R.id.content_layout);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            MenuItem item = getItem(position);
            int length = getCount();
            if (length == 1) {
                if (showUp) {
                    holder.arrow_up.setVisibility(View.VISIBLE);
                    holder.arrow_down.setVisibility(View.GONE);
                } else {
                    holder.arrow_up.setVisibility(View.GONE);
                    holder.arrow_down.setVisibility(View.VISIBLE);
                }
                holder.content_layout.setBackgroundResource(R.drawable.menu_blue_selector);
            } else if (position == 0) {
                if (showUp) {
                    holder.arrow_up.setVisibility(View.VISIBLE);
                    holder.arrow_down.setVisibility(View.GONE);
                } else {
                    holder.arrow_up.setVisibility(View.GONE);
                    holder.arrow_down.setVisibility(View.GONE);
                }
                holder.content_layout.setBackgroundResource(R.drawable.menu_blue_top_selector);
            } else if (position == length - 1) {
                if (showUp) {
                    holder.arrow_up.setVisibility(View.GONE);
                    holder.arrow_down.setVisibility(View.GONE);
                } else {
                    holder.arrow_up.setVisibility(View.GONE);
                    holder.arrow_down.setVisibility(View.VISIBLE);
                }
                holder.content_layout.setBackgroundResource(R.drawable.menu_blue_under_selector);
            } else {
                holder.arrow_up.setVisibility(View.GONE);
                holder.arrow_down.setVisibility(View.GONE);
                holder.content_layout.setBackgroundResource(R.drawable.menu_blue_middle_selector);
            }
            if (item.getIcon() != null) {
                holder.icon.setImageDrawable(item.getIcon());
                holder.icon.setVisibility(View.VISIBLE);
            } else {
                holder.icon.setVisibility(View.GONE);
            }
            holder.title.setText(item.getTitle());

            return convertView;
        }
    }
}
