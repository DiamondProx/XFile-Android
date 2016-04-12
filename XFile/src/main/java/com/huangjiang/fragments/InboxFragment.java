package com.huangjiang.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.huangjiang.filetransfer.R;

/**
 * 收件箱
 */
public class InboxFragment extends Fragment implements View.OnClickListener {

    LinearLayout video_layout, picture_layout, music_layout, other_layout, folder_layout, install_layout;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.inbox_fragment, null);
        init(view);
        return view;
    }

    void init(View view) {
        video_layout = (LinearLayout) view.findViewById(R.id.video_layout);
        picture_layout = (LinearLayout) view.findViewById(R.id.picture_layout);
        music_layout = (LinearLayout) view.findViewById(R.id.music_layout);
        other_layout = (LinearLayout) view.findViewById(R.id.other_layout);
        folder_layout = (LinearLayout) view.findViewById(R.id.folder_layout);
        install_layout = (LinearLayout) view.findViewById(R.id.install_layout);

        video_layout.setOnClickListener(this);
        picture_layout.setOnClickListener(this);
        music_layout.setOnClickListener(this);
        other_layout.setOnClickListener(this);
        folder_layout.setOnClickListener(this);
        install_layout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }
}
