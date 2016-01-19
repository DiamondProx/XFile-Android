package com.huangjiang.setting;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

import com.huangjiang.filetransfer.R;

public class UserCenterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_center);
        TextView text=(TextView)findViewById(R.id.textView);
        text.setText("111111111111aaaaaaaaaaa");
    }

}
