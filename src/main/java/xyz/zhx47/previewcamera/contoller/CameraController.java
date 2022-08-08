package xyz.zhx47.previewcamera.contoller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import xyz.zhx47.previewcamera.common.Config;
import xyz.zhx47.previewcamera.common.HttpStatus;
import xyz.zhx47.previewcamera.entity.YCamera;
import xyz.zhx47.previewcamera.service.YCameraService;
import xyz.zhx47.previewcamera.utils.CacheUtil;
import xyz.zhx47.previewcamera.utils.CameraThread;
import xyz.zhx47.previewcamera.utils.R;

/**
 * @author wuguodong
 * @Title CameraController.java
 * @description controller
 * @time 2019年12月16日 上午9:00:27
 **/
@RestController
@RequiredArgsConstructor
public class CameraController {

    private final static Logger logger = LoggerFactory.getLogger(CameraController.class);

    @Autowired
    public Config config;// 配置文件bean
    private final YCameraService yCameraService;

    // 存放任务 线程
    public static Map<String, CameraThread.MyRunnable> JOBMAP = new HashMap<>();

    /**
     * 开始推送视频流
     *
     * @param cameraId 摄像头ID
     * @return 返回推流结果的基本信息
     */
    @RequestMapping(value = "/cameras/{cameraId}", method = RequestMethod.GET)
    public R openCamera(@PathVariable Long cameraId) {
        // 空值校验
        if (cameraId == null) {
            return R.error(HttpStatus.ERROR_PARAM, "参数错误");
        }

        YCamera yCamera = yCameraService.queryCameraById(cameraId);
        if (yCamera == null) {
            return R.error(HttpStatus.ERROR_PARAM, "ID无效");
        }

        // 获取当前时间
        yCamera.setOpentime(System.currentTimeMillis());
        Set<String> keys = CacheUtil.STREATMAP.keySet();
        // 缓存是否为空
        if (0 == keys.size()) {
            // 开始推流
            if (openStream(yCamera)) {
                return R.ok("打开视频流成功").putBodyByObject(yCamera);
            } else {
                return R.error("打开视频流失败");
            }
        }
        // 是否存在的标志；false：不存在；true：存在
        boolean sign = false;
        for (String key : keys) {
            if (CacheUtil.STREATMAP.get(key).getId().equals(cameraId)) {
                yCamera = CacheUtil.STREATMAP.get(key);
                sign = true;
                break;
            }
        }
        if (sign) {// 存在
            yCamera.setCount(yCamera.getCount() + 1);
            yCamera.setOpentime(System.currentTimeMillis());
            return R.ok("打开视频流成功").putBodyByObject(yCamera);
        } else {
            if (openStream(yCamera)) {
                return R.ok("打开视频流成功").putBodyByObject(yCamera);
            } else {
                return R.error("打开视频流失败");
            }
        }
    }

    /**
     * 开始推流逻辑
     *
     * @param yCamera 摄像头信息
     * @return 推流是否成功
     */
    private boolean openStream(YCamera yCamera) {
        // 生成token
        String token = UUID.randomUUID().toString();
        String rtmp = "rtmp://" + config.getPush_host() + ":" + config.getPush_port() + "/live/" + token;
        // nginx播放地址拼接规则
//        String url = "http://" + config.getPull_host() + "/live?port=" + config.getPush_port() + "&app=live&stream=" + token;
        // SRS播放地址拼接规则
        String url = "http://" + config.getPull_host() + ":" + config.getPull_port() + "/live/" + token + ".flv";

        yCamera.setRtmp(rtmp);
        yCamera.setUrl(url);
        yCamera.setOpentime(System.currentTimeMillis());
        yCamera.setCount(1);
        yCamera.setToken(token);

        // 解决ip输入错误时，grabber.start();出现阻塞无法释放grabber而导致后续推流无法进行；
        // 建立TCP Scoket连接，超时时间1s，如果成功继续执行，否则return
        try {
            Socket serverSocket = new Socket();
            serverSocket.connect(new InetSocketAddress(yCamera.getIpAddress(), yCamera.getPort()), 1000);
        } catch (IOException e) {
            logger.error("与摄像头建立连接失败 -> {} - {}", yCamera.getId(), yCamera.getScenicName() + "-" + yCamera.getName());
            return false;
        }
        try {
            Socket rtmpSocket = new Socket();
            rtmpSocket.connect(new InetSocketAddress(config.getPush_host(), config.getPush_port()), 1000);
        } catch (IOException e) {
            logger.error("与推流服务器建立连接失败 -> {}:{}" + config.getPush_host(), config.getPush_port());
            return false;
        }
        // 执行任务
        CameraThread.MyRunnable job = new CameraThread.MyRunnable(yCamera);
        CameraThread.MyRunnable.es.execute(job);
        JOBMAP.put(token, job);
        return true;
    }

    /**
     * 关闭视频流
     *
     * @param token 推流的TOKEN
     */
    @RequestMapping(value = "/cameras/{token}", method = RequestMethod.DELETE)
    public R closeCamera(@PathVariable("token") String token) {
        if (JOBMAP.containsKey(token) && CacheUtil.STREATMAP.containsKey(token)) {
            if (0 < CacheUtil.STREATMAP.get(token).getCount()) {
                // 人数-1
                CacheUtil.STREATMAP.get(token).setCount(CacheUtil.STREATMAP.get(token).getCount() - 1);
                logger.info("关闭成功 {} -> {}使用人数为{}", CacheUtil.STREATMAP.get(token).getId(), CacheUtil.STREATMAP.get(token).getScenicName() + "-" + CacheUtil.STREATMAP.get(token).getName(), CacheUtil.STREATMAP.get(token).getCount());
            }
        }
        return R.ok("关闭成功");
    }

    /**
     * 获取视频流
     *
     * @return 所有推流的任务信息
     */
    @RequestMapping(value = "/cameras", method = RequestMethod.GET)
    public R getCameras() {
        logger.info("获取视频流信息：" + CacheUtil.STREATMAP.toString());
        return R.ok().putBodyByObject(CacheUtil.STREATMAP);
    }

    /**
     * 视频流保活
     *
     * @param token 推流任务的TOKEN
     * @return 保活结果
     */
    @RequestMapping(value = "/cameras/{token}", method = RequestMethod.PUT)
    public R keepAlive(@PathVariable("token") String token) {
        // 直播流token
        if (null != CacheUtil.STREATMAP.get(token)) {
            YCamera yCamera = CacheUtil.STREATMAP.get(token);
            // 更新当前系统时间
            yCamera.setOpentime(System.currentTimeMillis());
            logger.info("摄像头保活成功 {} -> {}", yCamera.getId(), yCamera.getScenicName() + "-" + yCamera.getName());
            return R.ok("摄像头保活成功");
        }
        return R.error("TOKEN失效");
    }
}