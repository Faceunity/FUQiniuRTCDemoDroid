package com.qiniu.droid.rtc.demo.ui;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import com.faceunity.nama.FURenderer;
import com.faceunity.nama.IFURenderer;
import com.qiniu.droid.rtc.QNCaptureVideoCallback;
import com.qiniu.droid.rtc.demo.R;
import com.qiniu.droid.rtc.demo.gles.ProgramTexture2d;
import com.qiniu.droid.rtc.demo.gles.core.GlUtil;
import com.qiniu.droid.rtc.demo.profile.CSVUtils;
import com.qiniu.droid.rtc.demo.profile.Constant;

import org.webrtc.VideoFrame;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class UserTrackViewFullScreen extends UserTrackView implements QNCaptureVideoCallback {
    private static final String TAG = "UserTrackViewFullScreen";
    private FURenderer fuRenderer;
    private boolean mIsToSwitchCamera;
    private byte[] mCameraData;
    private CSVUtils mCSVUtils;

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

    private long currentTime = 0;

    @Override
    public void onCaptureStarted() {
        // 切换相机时，不再进行重建
        if (mIsToSwitchCamera) {
            return;
        }
        Log.d(TAG, "onCaptureStarted() tid: " + Thread.currentThread().getId());
        if (fuRenderer != null) {
            fuRenderer.onSurfaceCreated();
            currentTime = System.currentTimeMillis();
            initCsvUtil(getContext());
        }
    }

    @Override
    public void onRenderingFrame(VideoFrame.TextureBuffer textureBuffer, long l) {
        Log.v(TAG, "onRenderingFrame: tid:" + Thread.currentThread().getId() + ", width:" + textureBuffer.getWidth()
                + ", height:" + textureBuffer.getHeight() + ", timestamp:" + l );
        if (mIsToSwitchCamera) {
            return;
        }
        int height = textureBuffer.getHeight();
        int width = textureBuffer.getWidth();
        if (fuRenderer != null) {
            //三星s6花屏的问题，输入接口保持一致！！！这里用 texture输入，下面buffer双输入都有
            if (System.currentTimeMillis() - currentTime < 300) {
                fuRenderer.onDrawFrameSingleInput(textureBuffer.getTextureId(), textureBuffer.getWidth(), textureBuffer.getHeight());
                return;
            }
            int textureId = textureBuffer.getTextureId();
            long start = System.nanoTime();
            int texId = fuRenderer.onDrawFrameSingleInput(textureId, width, height);
            long renderTime = System.nanoTime() - start;
            if (mCSVUtils != null) {
                mCSVUtils.writeCsv(null, renderTime);
            }
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
            mCSVUtils.close();
        }
    }

    private void initCsvUtil(Context context) {
        mCSVUtils = new CSVUtils(context);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        String dateStrDir = format.format(new Date(System.currentTimeMillis()));
        dateStrDir = dateStrDir.replaceAll("-", "").replaceAll("_", "");
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        String dateStrFile = df.format(new Date());
        String filePath = Constant.filePath + dateStrDir + File.separator + "excel-" + dateStrFile + ".csv";
        Log.d(TAG, "initLog: CSV file path:" + filePath);
        StringBuilder headerInfo = new StringBuilder();
        headerInfo.append("version：").append(FURenderer.getVersion()).append(CSVUtils.COMMA)
                .append("机型：").append(android.os.Build.MANUFACTURER).append(android.os.Build.MODEL)
                .append("处理方式：双输入").append(CSVUtils.COMMA);
        mCSVUtils.initHeader(filePath, headerInfo);
    }

}