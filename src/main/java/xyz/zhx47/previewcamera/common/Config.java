package xyz.zhx47.previewcamera.common;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author wuguodong
 * @Title ConfigPojo.java
 * @description 读取配置文件的bean
 * @time 2019年12月25日 下午5:11:21
 **/
@Data
@ToString
@Component
@ConfigurationProperties(prefix = "config")
public class Config {
    private Integer keepalive;// 保活时长（分钟）
    private String push_host;// 推送地址
    private String pull_host;// 拉流地址
    private Integer push_port;// 推送端口
    private Integer pull_port;// 推送端口
}