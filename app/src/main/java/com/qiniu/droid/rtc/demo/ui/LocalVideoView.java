package com.qiniu.droid.rtc.demo.ui;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;

import com.faceunity.FURenderer;
import com.faceunity.entity.Effect;
import com.qiniu.droid.rtc.QNLocalSurfaceView;
import com.qiniu.droid.rtc.QNLocalVideoCallback;
import com.qiniu.droid.rtc.demo.R;

import org.webrtc.VideoFrame;

public class LocalVideoView extends RTCVideoView implements QNLocalVideoCallback {

    private FURenderer fuRenderer;
    private byte[] mData;
    private boolean isNeedF = false;

    public LocalVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(mContext).inflate(R.layout.local_video_view, this, true);
    }

    public void setFuRenderer(FURenderer fuRenderer) {
        this.fuRenderer = fuRenderer;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLocalSurfaceView = (QNLocalSurfaceView) findViewById(R.id.local_surface_view);
        mLocalSurfaceView.setLocalVideoCallback(this);
    }

    @Override
    public int onRenderingFrame(int textureId, int width, int height, VideoFrame.TextureBuffer.Type type, long timestampNs) {
        Log.e("onRenderingFrame", "onRenderingFrame textureId " + textureId + " width= " + width + " height= " + height
                + "  type=" + type.name() + " timestampNs=" + timestampNs);
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
    public void onPreviewFrame(byte[] data, int width, int height, int rotation, int fmt, long timestampNs) {
        this.mData = data;
        Log.d("onPreviewFrame", "data.length=" + data.length + "--width=" + width + "--height=" +
                height + "--rotation=" + rotation + "--timestampNs=" + timestampNs);
    }

    @Override
    public void onSurfaceCreated() {
        Log.e("onSurfaceCreated", "onSurfaceCreated");
        if (fuRenderer != null) {
            fuRenderer.loadItems();
        }
    }

    @Override
    public void onSurfaceChanged(int i, int i1) {
        Log.e("onSurfaceChanged", "onSurfaceChanged");
    }

    @Override
    public void onSurfaceDestroyed() {
        Log.e("onSurfaceDestroyed", "onSurfaceDestroyed");
        if (fuRenderer != null)
            fuRenderer.destroyItems();
    }
}
