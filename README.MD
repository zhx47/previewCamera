# 海康SDK 大华SDK WEB播放

获取SDK的回调函数中的PS流，使用 Java CV 转封装到流媒体服务器中，使用HTTP_FLV达到网页无插件播放的效果。

**注意：摄像头视频需要为H264，如为H265可以通过SDK去修改压缩格式**
**注意：摄像头视频需要为H264，如为H265可以通过SDK去修改压缩格式**
**注意：摄像头视频需要为H264，如为H265可以通过SDK去修改压缩格式**

项目支持 海康SDK 大华SDK RTSP连接三种方式。参考资料来自 [banmajio](https://blog.csdn.net/weixin_40777510
) 、 [eguid_1](https://eguid.blog.csdn.net) 以及网上无数篇文章。

修改[项目](https://github.com/banmajio/RTSPtoHTTP-FLV)，完成海康SDK与大华SDK的实时预览。

流媒体服务器可以使用Nginx或者SRS，Nginx需要编译此[模块](https://github.com/winshining/nginx-http-flv-module)

## 踩坑点

1. 大华SDK与海康SDK的JNA版本不一致
2. 大华SDK回调需要指定流格式，否则为大华私有协议，需要另行解码

## 摄像头表结构

```mysql
CREATE TABLE `y_camera` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id（主键）',
  `NAME` varchar(32) NOT NULL COMMENT '探头名称',
  `CHANNEL_NO` varchar(32) DEFAULT '' COMMENT '通道号',
  `SCENIC_UNION_ID` varchar(11) NOT NULL COMMENT '景区ID',
  `SCENIC_NAME` varchar(50) NOT NULL COMMENT '景区名称',
  `SCENIC_LEVEL` tinyint(4) DEFAULT NULL COMMENT '景区等级',
  `RECOMMEND_FLG` tinyint(4) DEFAULT '0' COMMENT '是否推荐(1:是,0:否)',
  `AREA_CODE` varchar(20) DEFAULT NULL COMMENT '区县编码',
  `AREA_NAME` varchar(20) DEFAULT NULL COMMENT '区县名称',
  `CAMERA_TYPE` tinyint(4) NOT NULL COMMENT '摄像头类型：1:RTSP链接;2:海康SDK;3:大华SDK',
  `RTSP` varchar(255) DEFAULT NULL COMMENT 'RTSP地址',
  `IP_ADDRESS` varchar(32) DEFAULT '' COMMENT 'IP地址',
  `PORT` int(5) DEFAULT NULL COMMENT '端口号',
  `USER_NAME` varchar(32) DEFAULT '' COMMENT '用户名',
  `PASSWORD` varchar(32) DEFAULT '' COMMENT '密码',
  `VALID` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否有效：0无效，1有效',
  `ETL_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'ETL更新时间',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='景区摄像头';
```

实际代码中如果**摄像头类型为大华或者海康SDK不填写通道号，默认为0**

## 效果

海康SDK预览

![image-20220808230758247](https://img.zhx47.xyz/i/2022/08/08/62f128a4778e9.png)

大华SDK预览

![image-20220808233322488](https://img.zhx47.xyz/i/2022/08/08/62f12cc47f0ea.png)

RTSP不再演示了，原项目就支持