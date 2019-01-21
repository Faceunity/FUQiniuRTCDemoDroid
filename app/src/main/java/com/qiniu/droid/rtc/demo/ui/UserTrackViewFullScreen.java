package com.qiniu.droid.rtc.demo.ui;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import com.faceunity.FURenderer;
import com.qiniu.droid.rtc.QNCaptureVideoCallback;
import com.qiniu.droid.rtc.demo.R;

import org.webrtc.VideoFrame;

public class UserTrackViewFullScreen extends UserTrackView implements QNCaptureVideoCallback {
    private FURenderer fuRenderer;
    private byte[] mData;

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
    public int onRenderingFrame(int textureId, int width, int height, VideoFrame.TextureBuffer.Type type, long timestampNs) {
        Log.d("UserTrackViewFullScreen", "onRenderingFrame:" + "textureId=" + textureId + "--width=" + width + "--height=" + height);
        int fuTuxId = textureId;

        int[] enabled = new int[10];
        for (int i = 0; i < 10; i++) {
            GLES20.glGetVertexAttribiv(i, GLES20.GL_VERTEX_ATTRIB_ARRAY_ENABLED, enabled, i);
            //Log.e("onRenderingFrame-before", "enabled" + i + "--" + enabled[i]);
        }
        if (fuRenderer != null) {
            if (!fuRenderer.isActive()) {
                fuRenderer.loadItems();
            }

            float[] matrix = new float[16];
            Matrix.setIdentityM(matrix, 0);
            if (fuRenderer.isFRONT()) {
                matrix[5] = -1.0f;
                fuTuxId = fuRenderer.onDrawFrame(fuTuxId, width, height, matrix);
            }
            fuTuxId = fuRenderer.onDrawFrame(mData, fuTuxId, width, height);
            fuTuxId = fuRenderer.onDrawFrame(fuTuxId, width, height, matrix);
        }
        for (int i = 0; i < 10; i++) {
            if (enabled[i] == 1) {
                GLES20.glEnableVertexAttribArray(i);
            } else {
                GLES20.glDisableVertexAttribArray(i);
            }
        }
        GLES20.glFinish();
        return fuTuxId;
    }

    @Override
    public void onPreviewFrame(byte[] bytes, int width, int height, int rotation, int fmt, long timestampNs) {
        Log.d("UserTrackViewFullScreen", "onPreviewFrame:" + "bytes=" + bytes.length + "--width=" + width + "--height=" + height);
        this.mData = bytes;
    }
}
