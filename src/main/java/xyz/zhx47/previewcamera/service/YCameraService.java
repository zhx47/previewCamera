package xyz.zhx47.previewcamera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.zhx47.previewcamera.entity.YCamera;

public interface YCameraService extends IService<YCamera> {

    YCamera queryCameraById(Long cameraId);
}
