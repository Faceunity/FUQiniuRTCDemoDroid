package com.qiniu.droid.rtc.demo.custom;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.faceunity.core.camera.FUCamera;
import com.faceunity.core.camera.FUCameraPreviewData;
import com.faceunity.core.entity.FUCameraConfig;
import com.faceunity.core.entity.FURenderOutputData;
import com.faceunity.core.enumeration.CameraFacingEnum;
import com.faceunity.core.enumeration.FUInputTextureEnum;
import com.faceunity.core.enumeration.FUTransformMatrixEnum;
import com.faceunity.core.faceunity.OffLineRenderHandler;
import com.faceunity.core.listener.OnFUCameraListener;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.data.FaceUnityDataFactory;
import com.faceunity.nama.listener.FURendererListener;
import com.qiniu.droid.rtc.QNRTCEngine;
import com.qiniu.droid.rtc.QNTrackInfo;
import com.qiniu.droid.rtc.QNVideoFrame;
import com.qiniu.droid.rtc.demo.RTCApplication;
import com.qiniu.droid.rtc.demo.profile.CSVUtils;
import com.qiniu.droid.rtc.demo.profile.Constant;
import com.qiniu.droid.rtc.demo.utils.PreferenceUtil;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

/**
 * 用户自定义数据采集及数据处理，接入 faceunity 美颜贴纸
 *
 * @author Richie on 2019.12.20
 */
public class CameraRenderer {
    private static final String TAG = "CameraRenderer";
    private static final int DEFAULT_CAMERA_WIDTH = 1280;
    private static final int DEFAULT_CAMERA_HEIGHT = 720;
    private static final int PREVIEW_BUFFER_COUNT = 3;
    private Activity mActivity;
    private int mCameraWidth = DEFAULT_CAMERA_WIDTH;
    private int mCameraHeight = DEFAULT_CAMERA_HEIGHT;
    private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private int mCameraOrientation = 270;
    private int mCameraTextureId;
    private int mSkippedFrames = 5;
    private FURenderer mFURenderer;
    private CSVUtils mCSVUtils;
    private QNRTCEngine mQNRTCEngine;
    private QNTrackInfo mQNTrackInfo;
    private boolean openFU;
    private FURendererListener mRenderListener;
    private FaceUnityDataFactory mFaceUnityDataFactory;
    private FUCamera fuCamera;
    private OffLineRenderHandler mOffLineRenderHandler;

    public CameraRenderer(Activity activity, QNRTCEngine engine, FaceUnityDataFactory faceUnityDataFactory, FURendererListener renderListener) {
        mActivity = activity;
        mQNRTCEngine = engine;
        mRenderListener = renderListener;
        mFaceUnityDataFactory = faceUnityDataFactory;
        String isOpen = PreferenceUtil.getString(RTCApplication.getInstance(), PreferenceUtil.KEY_FACEUNITY_ISON);
        openFU = PreferenceUtil.FU_BEAUTY_ON.equals(isOpen);
        FURenderer.getInstance().setup(activity);
        mFURenderer = FURenderer.getInstance();
        mFURenderer.setMarkFPSEnable(true);
        mFURenderer.setInputTextureType(FUInputTextureEnum.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE);
        mFURenderer.setCameraFacing(CameraFacingEnum.CAMERA_FRONT);
        mFURenderer.setInputOrientation(CameraUtils.getCameraOrientation(Camera.CameraInfo.CAMERA_FACING_FRONT));
        mFURenderer.setInputBufferMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
        mFURenderer.setInputTextureMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
        mFURenderer.setOutputMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
        mFURenderer.setCreateEGLContext(true);


        fuCamera = FUCamera.getInstance();
        mOffLineRenderHandler = OffLineRenderHandler.getInstance();
    }

    public FURenderer getFURenderer() {
        return mFURenderer;
    }

    private volatile byte[] mInputBuffer;
    private Object mInputBufferLock = new Object();


    private byte[] getCurrentBuffer() {
        synchronized (mInputBufferLock) {
            byte[] currentInputBuffer = new byte[mInputBuffer.length];
            System.arraycopy(mInputBuffer, 0, currentInputBuffer, 0, currentInputBuffer.length);
            return currentInputBuffer;
        }
    }

    private OnFUCameraListener onFUCameraListener = new OnFUCameraListener() {
        @Override
        public void onPreviewFrame(FUCameraPreviewData fuCameraPreviewData) {
            if (!openFU) {
                QNVideoFrame frame = new QNVideoFrame();
                frame.buffer = fuCameraPreviewData.getBuffer();
                frame.height = mCameraHeight;
                frame.width = mCameraWidth;
                frame.rotation = mCameraOrientation;
                frame.timestampNs = System.nanoTime();
                Log.e(TAG, "onPreviewFrame: send data");
                mQNRTCEngine.pushVideoBuffer(mQNTrackInfo.getTrackId(), frame);
                return;
            }
            synchronized (mInputBufferLock) {
                mInputBuffer = new byte[fuCameraPreviewData.getBuffer().length];
                System.arraycopy(fuCameraPreviewData.getBuffer(), 0, mInputBuffer, 0, mInputBuffer.length);
            }
            mOffLineRenderHandler.requestRender();
        }
    };

    private OffLineRenderHandler.Renderer mOffLineRenderHandlerRedner = new OffLineRenderHandler.Renderer() {
        @Override
        public void onDrawFrame() {
            if (mInputBuffer == null) {
                return;
            }
            SurfaceTexture surfaceTexture = fuCamera.getSurfaceTexture();
            try {
                surfaceTexture.updateTexImage();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            int orientation = mCameraOrientation;
            long start = System.nanoTime();
            FURenderOutputData outputData;
            byte[] inputBuffer = getCurrentBuffer();
            if (!mFaceUnityDataFactory.isHasMakeupLoaded()) {
                outputData = mFURenderer.onDrawFrameInputWithReturn(inputBuffer, mCameraTextureId, mCameraWidth, mCameraHeight);
                Log.e(TAG, "onPreviewFrame: dual" + EGL14.eglGetCurrentContext());
            } else {
                outputData = mFURenderer.onDrawFrameInputWithReturn(inputBuffer, 0, mCameraWidth, mCameraHeight);
                Log.e(TAG, "onPreviewFrame: single" + EGL14.eglGetCurrentContext());
            }
            long time = System.nanoTime() - start;
            mCSVUtils.writeCsv(null, time);
            if (mSkippedFrames > 0 || mFaceUnityDataFactory.getSkipFrames() > 0) {
                mSkippedFrames--;
            } else {
                if (mPostHandler != null && orientation == mCameraOrientation) {
                    mPostHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            QNVideoFrame frame = new QNVideoFrame();
                            frame.buffer = outputData == null ? inputBuffer : outputData.getImage().getBuffer();
                            frame.height = mCameraHeight;
                            frame.width = mCameraWidth;
                            frame.rotation = mCameraOrientation;
                            frame.timestampNs = System.nanoTime();
                            long start = System.currentTimeMillis();
                            mQNRTCEngine.pushVideoBuffer(mQNTrackInfo.getTrackId(), frame);
                            long time = System.currentTimeMillis() - start;
                            Log.e(TAG, "run: pushTime: " + time);
                        }
                    });
                }
            }
        }
    };

    public void onResume() {
        startBackgroundThread();
        mOffLineRenderHandler.onResume();
        mOffLineRenderHandler.setRenderer(mOffLineRenderHandlerRedner);
        mOffLineRenderHandler.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mFURenderer != null) {
                    mFURenderer.prepareRenderer(mRenderListener);
                }
                initCsvUtil(mActivity);
                mCameraTextureId = GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
                FUCameraConfig config = new FUCameraConfig();
                config.setCameraFacing(mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT ? CameraFacingEnum.CAMERA_FRONT : CameraFacingEnum.CAMERA_BACK);
                fuCamera.openCamera(config, mCameraTextureId, onFUCameraListener);
            }
        });
    }

    public void onPause() {
        if (mPostHandler == null) {
            return;
        }
        CountDownLatch countDownLatch = new CountDownLatch(1);
        mOffLineRenderHandler.queueEvent(new Runnable() {
            @Override
            public void run() {
                fuCamera.closeCamera();
                if (mCameraTextureId > 0) {
                    GLES20.glDeleteTextures(1, new int[]{mCameraTextureId}, 0);
                    mCameraTextureId = 0;
                }
                if (mFURenderer != null) {
                    mFURenderer.release();
                }
                mCSVUtils.close();
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mOffLineRenderHandler.onPause();
        stopBackgroundThread();
        mSkippedFrames = 5;
    }

    public void onDestroy() {
        fuCamera.releaseCamera();
    }

    public void setQNTrackInfo(QNTrackInfo QNTrackInfo) {
        mQNTrackInfo = QNTrackInfo;
    }

    /**
     * 切换相机
     */
    public void switchCamera() {
        Log.d(TAG, "switchCamera: ");
        fuCamera.switchCamera();
        boolean isFront = mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT;
        mCameraFacing = isFront ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
        mSkippedFrames = 5;
        if (mFURenderer != null) {
            mFURenderer.setCameraFacing(mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT ? CameraFacingEnum.CAMERA_FRONT : CameraFacingEnum.CAMERA_BACK);
            mFURenderer.setInputOrientation(CameraUtils.getCameraOrientation(mCameraFacing));

            if (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mFURenderer.setInputBufferMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
                mFURenderer.setInputTextureMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
                mFURenderer.setOutputMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
            }else {
                mFURenderer.setInputBufferMatrix(FUTransformMatrixEnum.CCROT0);
                mFURenderer.setInputTextureMatrix(FUTransformMatrixEnum.CCROT0);
                mFURenderer.setOutputMatrix(FUTransformMatrixEnum.CCROT0_FLIPHORIZONTAL);
            }

        }
    }

    private Handler mPostHandler;

    private void startBackgroundThread() {
        HandlerThread handlerThread = new HandlerThread("poster");
        handlerThread.start();
        mPostHandler = new Handler(handlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        mPostHandler.getLooper().quitSafely();
        mPostHandler = null;
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
        headerInfo.append("version：").append(FURenderer.getInstance().getVersion()).append(CSVUtils.COMMA)
                .append("机型：").append(android.os.Build.MANUFACTURER).append(android.os.Build.MODEL)
                .append("处理方式：双输入返回Buffer").append(CSVUtils.COMMA);
        mCSVUtils.initHeader(filePath, headerInfo);
    }

}
