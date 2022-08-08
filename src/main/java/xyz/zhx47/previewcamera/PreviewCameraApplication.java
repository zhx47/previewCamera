package xyz.zhx47.previewcamera;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import xyz.zhx47.previewcamera.contoller.CameraController;
import xyz.zhx47.previewcamera.utils.CacheUtil;
import xyz.zhx47.previewcamera.utils.CameraPush;
import xyz.zhx47.previewcamera.utils.CameraThread;
import xyz.zhx47.previewcamera.utils.CameraTimer;
import xyz.zhx47.previewcamera.utils.dahuatech.DahuatechSDKUtil;
import xyz.zhx47.previewcamera.utils.hikvision.HikvisionSDKUtil;

import javax.annotation.PreDestroy;
import java.util.Set;

@SpringBootApplication
public class PreviewCameraApplication {

    private final static Logger logger = LoggerFactory.getLogger(PreviewCameraApplication.class);

    public static void main(String[] args) {
        // 服务启动执行FFmpegFrameGrabber和FFmpegFrameRecorder的tryLoad()，以免导致第一次推流时耗时。
        try {
            FFmpegFrameGrabber.tryLoad();
            FFmpegFrameRecorder.tryLoad();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final ApplicationContext applicationContext = SpringApplication.run(PreviewCameraApplication.class, args);
        // 将上下文传入RealPlay类中,以使其使用config中的变量
        CameraPush.setApplicationContext(applicationContext);
        HikvisionSDKUtil.setApplicationContext(applicationContext);
        DahuatechSDKUtil.setApplicationContext(applicationContext);
    }

    @PreDestroy
    public void destory() {
        logger.info("服务结束，开始释放空间...");
        // 结束正在进行的任务
        Set<String> keys = CameraController.JOBMAP.keySet();
        for (String key : keys) {
            CameraController.JOBMAP.get(key).setInterrupted(key);
        }
        // 关闭线程池
        CameraThread.MyRunnable.es.shutdown();
        // 销毁定时器
        CameraTimer.timer.cancel();
    }
}
