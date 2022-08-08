package xyz.zhx47.previewcamera.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class YCamera implements Serializable {
    public static final int CAMERA_TYPE_RTSP_H264 = 1;
    public static final int CAMERA_TYPE_HK_SDK_H264 = 2;
    public static final int CAMERA_TYPE_DH_SDK_H264 = 3;

    private String rtmp;// rtmp地址
    private String url;// 播放地址
    private Long opentime;// 打开时间
    private int count = 0;// 使用人数
    private String token;

    /**
     * id（主键）
     */
    private Long id;

    /**
     * 探头名称
     */
    private String name;

    /**
     * 通道号
     */
    private String channelNo;

    /**
     * 景区ID
     */
    private String scenicUnionId;

    /**
     * 景区名称
     */
    private String scenicName;

    /**
     * 景区等级
     */
    private Byte scenicLevel;

    /**
     * 是否推荐(1:是,0:否)
     */
    private Byte recommendFlg;

    /**
     * 区县编码
     */
    private String areaCode;

    /**
     * 区县名称
     */
    private String areaName;

    /**
     * 摄像头类型：1:千里眼；2:海康
     */
    private Integer cameraType;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 是否有效：0无效，1有效
     */
    private Byte valid;

    /**
     * ETL更新时间
     */
    private Date etlTime;

    /**
     * RTSP地址
     */
    private String rtsp;

    private static final long serialVersionUID = 1L;
}

