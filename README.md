FUQNRTCDemoDroid是集成了 FaceUnity 美颜贴纸功能和 **[七牛实时音视频云](https://developer.qiniu.com/pili/sdk/3715/PLDroidMediaStreaming-overview)** 的 Demo。

SDK 版本为 **7.4.1.0**。关于 SDK 的详细说明，请参看 **[FULiveDemoDroid](https://github.com/Faceunity/FULiveDemoDroid/tree/master/doc)**。

**这个README是关于自定义视频对接方式的**
**使用七牛sdk采集相机数据，并处理的代码在 RoomActivity 与 UserTrackViewFullScreen 类中**

## 快速集成方法

### 一、添加 SDK

### 1. build.gradle配置

#### 1.1 allprojects配置
```java
allprojects {
    repositories {
        ...
        maven { url 'http://maven.faceunity.com/repository/maven-public/' }
        ...
  }
}
```

#### 1.2 dependencies导入依赖
```java
dependencies {
...
implementation 'com.faceunity:core:7.4.1.0' // 实现代码
implementation 'com.faceunity:model:7.4.1.0' // 道具以及AI bundle
...
}
```

##### 备注

集成参考文档：FULiveDemoDroid 工程 doc目录

### 2. 其他接入方式-底层库依赖

```java
dependencies {
...
implementation 'com.faceunity:nama:7.4.1.0' //底层库-标准版
implementation 'com.faceunity:nama-lite:7.4.1.0' //底层库-lite版
...
}
```

如需指定应用的 so 架构，请修改 app 模块 build.gradle：

```groovy
android {
    // ...
    defaultConfig {
        // ...
        ndk {
            abiFilters 'armeabi-v7a', 'arm64-v8a'
        }
    }
}
```

如需剔除不必要的 assets 文件，请修改 app 模块 build.gradle：

```groovy
android {
    // ...
    applicationVariants.all { variant ->
        variant.mergeAssetsProvider.configure {
            doLast {
                delete(fileTree(dir: outputDir, includes: ['model/ai_face_processor_lite.bundle',
                                                           'model/ai_hand_processor.bundle',
                                                           'graphics/controller.bundle',
                                                           'graphics/fuzzytoonfilter.bundle',
                                                           'graphics/fxaa.bundle',
                                                           'graphics/tongue.bundle']))
            }
        }
    }
}
```

### 二、使用 SDK

#### 1. 初始化

调用 `FURenderer` 类的  `setup` 方法初始化 SDK，可以在工作线程调用，应用启动后仅需调用一次。

在  CameraRenderer 类 根据是否启用美颜sdk来调用该方法。

#### 2.创建

调用 `FURenderer` 类的  `prepareRenderer` 方法在 SDK 使用前加载必要的资源。

自采集的处理都在`CameraRenderer `中，包含**开启相机**、**预览**、**美颜处理**，**切换相机**。

在CameraRenderer.onResume 方法中会执行`prepareRenderer`

#### 3. 图像处理

调用 `FURenderer` 类的  `onDrawFrameXXX` 方法进行图像处理，有许多重载方法适用于不同数据类型的需求。

在 `CameraRenderer  `类中，onPreviewFrame方法会获取到相机数据，在这个方法中可以处理图片

#### 4. 销毁

调用 `FURenderer` 类的  `release` 方法在 SDK 结束前释放占用的资源。

在CameraRenderer.onPause 方法中会执行`release`。CameraRenderer.onPause方法可在Activity onPause方法中执行。

#### 5. 切换相机

调用 `FURenderer` 类 的  `onCameraChanged` 方法，用于重新为 SDK 设置参数。

在 CameraRenderer 中执行切换相机操作时调用

#### 6. 旋转手机

在 `CustomRoomActivity` 中调用 `FURenderer` 类 的  `setDeviceOrientation` 方法，用于重新为 SDK 设置参数。

```java
1. implements SensorEventListener
2. private void onCreate() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
   }

3.
    @Override
    protected void onDestroy() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        super.onDestroy();
    }
  4.
  	//实现接口
    @Override
    public void onSensorChanged(SensorEvent event) {
        //具体代码见 CustomRoomActivity  类
    }

```

上面一系列方法的使用，具体在 demo 中的 CustomRoomActivity ，请参考该代码示例接入。

### 三、接口介绍

- IFURenderer 是核心接口，提供了创建、销毁、渲染等接口。
- FaceUnityDataFactory 控制四个功能模块，用于功能模块的切换，初始化
- FaceBeautyDataFactory 是美颜业务工厂，用于调整美颜参数。
- PropDataFactory 是道具业务工厂，用于加载贴纸效果。
- MakeupDataFactory 是美妆业务工厂，用于加载美妆效果。
- BodyBeautyDataFactory 是美体业务工厂，用于调整美体参数。

**至此快速集成完毕，关于 FaceUnity SDK 的更多详细说明，请参看 [FULiveDemoDroid](https://github.com/Faceunity/FULiveDemoDroid/)**。