package com.huangjiang.activity;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.tonicartos.widget.stickygridheaders.StickyGridHeadersBaseAdapter;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

import huangjiang.com.xfile_android.R;

public class TestActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		StickyGridHeadersGridView sgvPicture = (StickyGridHeadersGridView) findViewById(R.id.sgv);
		//sgvPicture.loadPicture();
		
		sgvPicture.setAdapter(new StickyGridHeadersBaseAdapter() {

			@Override
			public void unregisterDataSetObserver(DataSetObserver arg0) {

			}

			@Override
			public void registerDataSetObserver(DataSetObserver arg0) {
				
				

			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public boolean hasStableIds() {
				return false;
			}

			@Override
			public int getViewTypeCount() {
				return 1;
			}

			@Override
			public View getView(int arg0, View arg1, ViewGroup arg2) {
				final Button b = new Button(TestActivity.this);
				b.setWidth(10);
				b.setHeight(100);
				b.setText(arg0 + "");
				b.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						Toast.makeText(TestActivity.this, b.getText()+"", 0).show();
						
					}
				});
				return b;
			}

			@Override
			public int getItemViewType(int arg0) {
				return 0;
			}

			@Override
			public long getItemId(int arg0) {
				return 0;
			}

			@Override
			public Object getItem(int arg0) {
				return null;
			}

			@Override
			public int getCount() {
				return 100;
			}

			@Override
			public boolean isEnabled(int position) {
				return false;
			}

			@Override
			public boolean areAllItemsEnabled() {
				return false;
			}

			@Override
			public int getNumHeaders() {
				return 100;
			}

			@Override
			public View getHeaderView(int position, View convertView,
					ViewGroup parent) {
				Button b = new Button(TestActivity.this);
				b.setWidth(100);
				b.setHeight(50);
				b.setText("ttttttttttttttttttttttttt");
				return b;
			}

			@Override
			public int getCountForHeader(int header) {
				return 10;
			}
		});
	}
}
