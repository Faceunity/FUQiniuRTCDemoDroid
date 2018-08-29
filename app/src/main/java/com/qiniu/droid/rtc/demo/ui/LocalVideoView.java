package com.qiniu.droid.rtc.demo.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.qiniu.droid.rtc.QNLocalSurfaceView;
import com.qiniu.droid.rtc.QNLocalVideoCallback;
import com.qiniu.droid.rtc.demo.R;

public class LocalVideoView extends RTCVideoView {

    public LocalVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(mContext).inflate(R.layout.local_video_view, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLocalSurfaceView = (QNLocalSurfaceView) findViewById(R.id.local_surface_view);
    }

    public void setLocalVideoCallback(QNLocalVideoCallback callback) {
        mLocalSurfaceView.setLocalVideoCallback(callback);
    }
}
