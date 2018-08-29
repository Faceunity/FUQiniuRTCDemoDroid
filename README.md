# FUQiniuRTCDemoDroid 快速接入文档

FUQiniuRTCDemoDroid 是集成了 Faceunity 面部跟踪和虚拟道具功能 和 七牛RTC Demo  的 Demo。

本文是 FaceUnity SDK 快速对 七牛RTC Demo 的导读说明，关于 `FaceUnity SDK` 的详细说明，请参看 **[FULiveDemoDroid](https://github.com/Faceunity/FULiveDemoDroid/tree/dev)**



## 快速集成方法

### 一、导入 SDK

将 FaceUnity 文件夹全部拖入工程中。  

- jniLibs 文件夹下 libnama.so 人脸跟踪及道具绘制核心静态库
- libs 文件夹下 nama.jar java层native接口封装
- v3.bundle 初始化必须的二进制文件
- face_beautification.bundle 我司美颜相关的二进制文件
- effects 文件夹下的 *.bundle 文件是我司制作的特效贴纸文件，自定义特效贴纸制作的文档和工具请联系我司获取。

### 二、全局配置

在 FURenderer类 的  `initFURenderer` 静态方法是对 Faceunity SDK 一些全局数据初始化的封装，可以在 Application 中调用，仅需初始化一次即可。

```
public static void initFURenderer(Context context)；
```

### 三、使用 SDK

#### 初始化

在 FURenderer类 的  `onSurfaceCreated` 方法是对 Faceunity SDK 每次使用前数据初始化的封装。

在本demo中的使用：

```
            @Override
            public void onSurfaceCreated() {
                Log.e(TAG, "onSurfaceCreated " + Thread.currentThread().getId());
                final EGLContext eglContext = EGL14.eglGetCurrentContext();
                mGLHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mEglCore == null)
                            mEglCore = new EglCore(eglContext, 0);
                        mFURenderer.onSurfaceCreated();
                    }
                });
            }
```

#### 图像处理

在 FURenderer类 的  `onDrawFrame` 方法是对 Faceunity SDK 图像处理方法的封装，该方法有许多重载方法适用于不同的数据类型需求。

在本demo中的使用：

```
            @Override
            public int onRenderingFrame(final int i, final int i1, final int i2, VideoFrame.TextureBuffer.Type type, long l) {
                if (i <= 0 || i1 <= 0 || i2 <= 0 || mGLHandler == null || isCameraSwitching++ < 1)
                    return i;
                final CountDownLatch count = new CountDownLatch(1);
                mGLHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mEglSurfaceBase == null) {
                            mEglSurfaceBase = new OffscreenSurface(mEglCore, i1, i2);
                            mEglSurfaceBase.makeCurrent();
                        }

                        mFUTextureId = mFURenderer.onDrawFrame(i, i1, i2);

                        mEglSurfaceBase.swapBuffers();
                        count.countDown();
                    }
                });
                try {
                    count.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return mFUTextureId;
            }
```

#### 销毁

在 FURenderer类 的  `onSurfaceDestroyed` 方法是对 Faceunity SDK 数据销毁的封装。

在本demo中的使用：

```
            @Override
            public void onSurfaceDestroyed() {
                Log.e(TAG, "onSurfaceDestroyed " + Thread.currentThread().getId());
                mGLHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mFURenderer.onSurfaceDestroyed();
                    }
                });
            }
```

### 四、切换道具及调整美颜参数

本例中 FURenderer类 实现了 OnFUControlListener接口，而OnFUControlListener接口是对切换道具及调整美颜参数等一系列操作的封装，demo中使用了BeautyControlView作为切换道具及调整美颜参数的控制view。使用以下代码便可实现view对各种参数的控制。

```
mBeautyControlView.setOnFUControlListener(mFURenderer);
```

**快速集成完毕，关于 FaceUnity SDK 的更多详细说明，请参看 [FULiveDemoDroid](https://github.com/Faceunity/FULiveDemoDroid/tree/dev)**