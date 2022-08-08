package xyz.zhx47.previewcamera.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Component;
import xyz.zhx47.previewcamera.common.Config;
import xyz.zhx47.previewcamera.contoller.CameraController;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author wuguodong
 * @Title TimerUtil.java
 * @description 定时任务
 * @time 2019年12月16日 下午3:10:08
 **/
@Component
public class CameraTimer implements CommandLineRunner {

    private final static Logger logger = LoggerFactory.getLogger(CameraTimer.class);
    public static Timer timer;

    @Autowired
    private Config config;

    @Override
    public void run(String... args) throws Exception {
        timer = new Timer("timeTimer");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info("定时任务  当前有" + CameraController.JOBMAP.size() + "个推流任务正在进行推流");
                // 管理缓存
                if (null != CacheUtil.STREATMAP && 0 != CacheUtil.STREATMAP.size()) {
                    Set<String> keys = CacheUtil.STREATMAP.keySet();
                    for (String key : keys) {
                        // 最后打开时间
                        long openTime = CacheUtil.STREATMAP.get(key).getOpentime();
                        // 当前系统时间
                        long newTime = System.currentTimeMillis();
                        if ((newTime - openTime) / 1000 / 60 >= config.getKeepalive()) {
                            CameraController.JOBMAP.get(key).setInterrupted(key);
                        }
                    }
                }
            }
        }, 1, 1000 * 60);
    }
}
