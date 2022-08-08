package xyz.zhx47.previewcamera.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.zhx47.previewcamera.entity.YCamera;

@Mapper
public interface YCameraMapper extends BaseMapper<YCamera> {
    YCamera queryCameraById(Long cameraId);
}