package xyz.zhx47.previewcamera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.zhx47.previewcamera.dao.YCameraMapper;
import xyz.zhx47.previewcamera.entity.YCamera;
import xyz.zhx47.previewcamera.service.YCameraService;

@Service
public class YCameraServiceImpl extends ServiceImpl<YCameraMapper, YCamera> implements YCameraService {

    @Override
    public YCamera queryCameraById(Long cameraId) {
        return this.baseMapper.queryCameraById(cameraId);
    }
}
