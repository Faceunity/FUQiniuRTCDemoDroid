package com.qiniu.droid.rtc.demo.ui;

import android.content.Context;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import com.faceunity.beautycontrolview.FURenderer;
import com.qiniu.droid.rtc.QNCaptureVideoCallback;
import com.qiniu.droid.rtc.demo.R;

import org.webrtc.VideoFrame;

public class UserTrackViewFullScreen extends UserTrackView implements QNCaptureVideoCallback {
    private static final String TAG = "UserTrackViewFullScreen";
    private FURenderer fuRenderer;
    private byte[] mData;
    private int[] mArrayEnabled = new int[10];

    public UserTrackViewFullScreen(@NonNull Context context) {
        super(context);
    }

    public UserTrackViewFullScreen(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setFuRenderer(FURenderer fuRenderer) {
        this.fuRenderer = fuRenderer;
    }

    @Override
    protected int getLayout() {
        return R.layout.user_tracks_view_full_screen;
    }

    @Override
    public void onCaptureStarted() {
        Log.d(TAG, "onCaptureStarted() tid: " + Thread.currentThread().getId());
        if (fuRenderer != null) {
            fuRenderer.loadItems();
        }
    }

    @Override
    public void onRenderingFrame(VideoFrame.TextureBuffer textureBuffer, long l) {
//        Log.v(TAG, "onRenderingFrame: tid:" +Thread.currentThread().getId() + ", width:" + textureBuffer.getWidth()
//        + ", height:" + textureBuffer.getHeight());

        for (int i = 0; i < mArrayEnabled.length; i++) {
            GLES20.glGetVertexAttribiv(i, GLES20.GL_VERTEX_ATTRIB_ARRAY_ENABLED, mArrayEnabled, i);
        }

        int fuTexId = textureBuffer.getTextureId();
        if (fuRenderer != null) {
            fuTexId = fuRenderer.onDrawFrameFBO(mData, fuTexId, textureBuffer.getWidth(), textureBuffer.getHeight());
        }
        GLES20.glFinish();
        for (int i = 0; i < mArrayEnabled.length; i++) {
            if (mArrayEnabled[i] == 1) {
                GLES20.glEnableVertexAttribArray(i);
            } else {
                GLES20.glDisableVertexAttribArray(i);
            }
        }

        textureBuffer.setTextureId(fuTexId);
    }

    @Override
    public void onPreviewFrame(byte[] bytes, int width, int height, int rotation, int fmt, long timestampNs) {
        this.mData = bytes;
    }

    @Override
    public void onCaptureStopped() {
        Log.d(TAG, "onCaptureStopped() tid: " + Thread.currentThread().getId());
        if (fuRenderer != null) {
            fuRenderer.destroyItems();
        }
    }

}
