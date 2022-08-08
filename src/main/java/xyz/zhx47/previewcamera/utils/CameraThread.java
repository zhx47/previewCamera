package xyz.zhx47.previewcamera.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.zhx47.previewcamera.contoller.CameraController;
import xyz.zhx47.previewcamera.entity.YCamera;
import xyz.zhx47.previewcamera.utils.dahuatech.DahuatechSDKUtil;
import xyz.zhx47.previewcamera.utils.hikvision.HikvisionSDKUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wuguodong
 * @Title CameraThread.java
 * @description TODO
 * @time 2019年12月16日 上午9:32:43
 **/
public class CameraThread {

    private final static Logger logger = LoggerFactory.getLogger(CameraThread.class);

    public static class MyRunnable implements Runnable {

        // 创建线程池
        public static ExecutorService es = Executors.newCachedThreadPool();
        private YCamera cameraPojo;
        private boolean running = true;
        private HikvisionSDKUtil hikvisionSDKUtil = null;
        private DahuatechSDKUtil dahuatechSDKUtil = null;

        public MyRunnable(YCamera cameraPojo) {
            this.cameraPojo = cameraPojo;
        }

        // 中断线程
        public void setInterrupted(String key) {
            try {
                if (hikvisionSDKUtil != null) {
                    hikvisionSDKUtil.setRunning(false);
                }
                if (dahuatechSDKUtil != null) {
                    dahuatechSDKUtil.setRunning(false);
                }
                if (CacheUtil.PUSHMAP.get(key) != null) {
                    CameraPush cameraPush = CacheUtil.PUSHMAP.get(key);
                    cameraPush.setRunning(false);
                    Thread.sleep(5000);
                    if (cameraPush != null) {
                        cameraPush.setExitcode(1);
                    }
                }
                this.running = false;
                // 清除缓存
                CacheUtil.STREATMAP.remove(cameraPojo.getToken());
                CameraController.JOBMAP.remove(cameraPojo.getToken());
                CacheUtil.PUSHMAP.remove(cameraPojo.getToken());
                logger.info("摄像头{} 停止预览", cameraPojo.getId());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            // 直播流
            try {
                logger.info("摄像头{} 开始预览", cameraPojo.getId());
                // 获取当前线程存入缓存
                CacheUtil.STREATMAP.put(cameraPojo.getToken(), cameraPojo);
                // 执行转流推流任务
                CameraPush push = new CameraPush(cameraPojo);
                CacheUtil.PUSHMAP.put(cameraPojo.getToken(), push);
                // 2022-08-01 海康和大华的SDK的推流将会另起一个线程，所以添加一个while(running)
                if (cameraPojo.getCameraType().equals(YCamera.CAMERA_TYPE_HK_SDK_H264)) {
                    hikvisionSDKUtil = new HikvisionSDKUtil();
                    boolean isLogin = hikvisionSDKUtil.login(cameraPojo.getIpAddress(), cameraPojo.getUserName(), cameraPojo.getPassword(), cameraPojo.getPort());
                    if (!isLogin) {
                        this.setInterrupted(cameraPojo.getToken());
                    } else {
                        hikvisionSDKUtil.previewCamera(cameraPojo.getToken(), cameraPojo.getChannelNo());
                    }
                } else if (cameraPojo.getCameraType().equals(YCamera.CAMERA_TYPE_DH_SDK_H264)) {
                    logger.info("大华摄像头准备登陆：{}", cameraPojo.getId());
                    dahuatechSDKUtil = new DahuatechSDKUtil();
                    boolean isLogin = dahuatechSDKUtil.login(cameraPojo.getIpAddress(), cameraPojo.getPort(), cameraPojo.getUserName(), cameraPojo.getPassword());
                    if (!isLogin) {
                        this.setInterrupted(cameraPojo.getToken());
                    } else {
                        logger.info("大华摄像头准备进行预览：{}", cameraPojo.getId());
                        dahuatechSDKUtil.previewCamera(cameraPojo.getToken(), cameraPojo.getChannelNo());
                    }
                } else {
                    push.start();
                }
                while (running) {
                }
                // 清除缓存
                CacheUtil.STREATMAP.remove(cameraPojo.getToken());
                CameraController.JOBMAP.remove(cameraPojo.getToken());
                CacheUtil.PUSHMAP.remove(cameraPojo.getToken());
            } catch (Exception e) {
                this.setInterrupted(cameraPojo.getToken());
                e.printStackTrace();
            }
        }
    }
}