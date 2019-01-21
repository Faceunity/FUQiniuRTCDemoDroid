# FUQNRTCDemoDroid 快速接入文档

FUQNRTCDemoDroid 是集成了 Faceunity 面部跟踪和虚拟道具功能 和 七牛RTC Demo  的 Demo。

本文是 FaceUnity SDK 快速对 七牛RTC Demo 的导读说明，关于 `FaceUnity SDK` 的详细说明，请参看 **[FULiveDemoDroid](https://github.com/Faceunity/FULiveDemoDroid/tree/dev)**



## 快速集成方法

### 一、导入 SDK

将 FaceUnity 文件夹全部拖入工程中。

- jniLibs 文件夹下 libnama.so 人脸跟踪及道具绘制核心静态库
- libs 文件夹下 nama.jar java层native接口封装
- v3.bundle 初始化必须的二进制文件
- face_beautification.bundle 我司美颜相关的二进制文件
- effects 文件夹下的 *.bundle 文件是我司制作的特效贴纸文件，自定义特效贴纸制作的文档和工具请联系我司获取。


### 二、使用 SDK

#### 初始化

在 FURenderer类 的  `onSurfaceCreated` 方法是对 Faceunity SDK 每次使用前数据初始化的封装。

#### 图像处理

在 FURenderer类 的  `onDrawFrame` 方法是对 Faceunity SDK 图像处理方法的封装，该方法有许多重载方法适用于不同的数据类型需求。

在本demo中的使用：

```
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
```

### 三、切换道具及调整美颜参数

本例中 FURenderer类 实现了 OnFUControlListener接口，而OnFUControlListener接口是对切换道具及调整美颜参数等一系列操作的封装，demo中使用了BeautyControlView作为切换道具及调整美颜参数的控制view。使用以下代码便可实现view对各种参数的控制。

```
mBeautyControlView.setOnFaceUnityControlListener(mFURenderer);
```

**快速集成完毕，关于 FaceUnity SDK 的更多详细说明，请参看 [FULiveDemoDroid](https://github.com/Faceunity/FULiveDemoDroid/tree/dev)**
 No newline at end of file
