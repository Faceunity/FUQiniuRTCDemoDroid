package com.qiniu.droid.rtc.demo.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import com.faceunity.nama.FURenderer;
import com.qiniu.droid.rtc.QNCaptureVideoCallback;
import com.qiniu.droid.rtc.demo.R;

import org.webrtc.VideoFrame;

public class UserTrackViewFullScreen extends UserTrackView implements QNCaptureVideoCallback {
    private static final String TAG = "UserTrackViewFullScreen";
    private FURenderer fuRenderer;
    private boolean mIsToSwitchCamera;

    public UserTrackViewFullScreen(@NonNull Context context) {
        super(context);
    }

    public UserTrackViewFullScreen(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setFuRenderer(FURenderer fuRenderer) {
        this.fuRenderer = fuRenderer;
    }

    public void setToSwitchCamera(boolean toSwitchCamera) {
        mIsToSwitchCamera = toSwitchCamera;
    }

    @Override
    protected int getLayout() {
        return R.layout.user_tracks_view_full_screen;
    }

    @Override
    public void onCaptureStarted() {
        // 切换相机时，不再进行重建
        if (mIsToSwitchCamera) {
            return;
        }
        Log.d(TAG, "onCaptureStarted() tid: " + Thread.currentThread().getId());
        if (fuRenderer != null) {
            fuRenderer.onSurfaceCreated();
        }
    }

    @Override
    public void onRenderingFrame(VideoFrame.TextureBuffer textureBuffer, long l) {
//            Log.v(TAG, "onRenderingFrame: tid:" + Thread.currentThread().getId() + ", width:" + textureBuffer.getWidth()
//                    + ", height:" + textureBuffer.getHeight() + ", timestamp:" + l);
        if (mIsToSwitchCamera) {
            return;
        }
        if (fuRenderer != null) {
            int texId = fuRenderer.onDrawFrameSingleInput(textureBuffer.getTextureId(), textureBuffer.getWidth(), textureBuffer.getHeight());
            textureBuffer.setTextureId(texId);
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, int width, int height, int rotation, int fmt, long timestampNs) {
    }

    @Override
    public void onCaptureStopped() {
        Log.d(TAG, "onCaptureStopped() tid: " + Thread.currentThread().getId());
        if (fuRenderer != null) {
            fuRenderer.onSurfaceDestroyed();
        }
    }

}