package com.huangjiang.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.huangjiang.business.event.RecordEvent;
import com.huangjiang.business.history.HistoryLogic;
import com.huangjiang.xfile.R;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;

public class UserCenterActivity extends BaseActivity {

    private final String mPageName = "UserCenterActivity";

    HistoryLogic historyLogic;
    TextView txt_num1, txt_num2, txt_num3, txt_num4, txt_num5, txt_unit;
    String resBit, resKb, resMb, resGb, resZero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(R.string.title_activity_user_center, R.layout.activity_user_center);
        txt_num1 = (TextView) findViewById(R.id.txt_num1);
        txt_num2 = (TextView) findViewById(R.id.txt_num2);
        txt_num3 = (TextView) findViewById(R.id.txt_num3);
        txt_num4 = (TextView) findViewById(R.id.txt_num4);
        txt_num5 = (TextView) findViewById(R.id.txt_num5);
        txt_unit = (TextView) findViewById(R.id.txt_unit);
        resBit = getString(R.string.bit);
        resKb = getString(R.string.kb);
        resMb = getString(R.string.mb);
        resGb = getString(R.string.gb);
        resZero = getString(R.string.zero);
        TextView mobile_name = (TextView) findViewById(R.id.mobile_name);
        mobile_name.setText(android.os.Build.MODEL);
        EventBus.getDefault().register(this);
        historyLogic = new HistoryLogic(UserCenterActivity.this);
        historyLogic.getRecordInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mPageName);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mPageName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RecordEvent recordEvent) {

        long totalSize = recordEvent.getTotalSize();

        String sizeStr;
        float kb = 1024;
        float mb = kb * 1024;
        float gb = mb * 1024;
        DecimalFormat fnum = new DecimalFormat("##0.00");
        if (totalSize < kb) {
            txt_unit.setText(resBit);
            sizeStr = totalSize + "";
        } else if (totalSize < mb) {
            txt_unit.setText(resKb);
            float showSize = (float) totalSize / kb;
            fnum.format(showSize);
            sizeStr = fnum.format(showSize);
        } else if (totalSize < gb) {
            txt_unit.setText(resMb);
            float showSize = (float) totalSize / mb;
            fnum.format(showSize);
            sizeStr = fnum.format(showSize);
        } else {
            txt_unit.setText(resGb);
            float showSize = (float) totalSize / gb;
            fnum.format(showSize);
            sizeStr = fnum.format(showSize);
        }
        String preNum;
        if (sizeStr.contains(".")) {
            txt_num5.setText(sizeStr.substring(sizeStr.indexOf(".")));
            preNum = sizeStr.substring(0, sizeStr.indexOf("."));
        } else {
            txt_num5.setText(resZero);
            preNum = sizeStr;
        }
        int maxLength = preNum.length();
        for (int i = 0; i < maxLength; i++) {
            char num = sizeStr.charAt(maxLength - i - 1);
            switch (i) {
                case 0:
                    txt_num4.setText(String.valueOf(num));
                    break;
                case 1:
                    txt_num3.setText(String.valueOf(num));
                    break;
                case 2:
                    txt_num2.setText(String.valueOf(num));
                    break;
                case 4:
                    txt_num1.setText(String.valueOf(num));
                    break;
            }
        }


    }

}
