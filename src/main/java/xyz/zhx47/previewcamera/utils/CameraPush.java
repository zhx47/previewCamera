package xyz.zhx47.previewcamera.utils;

import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import xyz.zhx47.previewcamera.common.Config;
import xyz.zhx47.previewcamera.contoller.CameraController;
import xyz.zhx47.previewcamera.entity.YCamera;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.ffmpeg.global.avcodec.av_packet_unref;

/**
 * javacv推数据帧
 **/
public class CameraPush extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(CameraPush.class);

    // 配置类
    private static Config config;

    // 通过applicationContext上下文获取Config类
    public static void setApplicationContext(ApplicationContext applicationContext) {
        config = applicationContext.getBean(Config.class);
    }

    /**
     * 设备信息
     */
    private YCamera yCamera;
    /**
     * 解码器
     */
    private FFmpegFrameRecorder recorder;
    /**
     * 采集器
     */
    private FFmpegFrameGrabber grabber;
    /**
     * 推流过程中出现错误的次数
     */
    private int err_index = 0;
    /**
     * 退出状态码：0-正常退出;1-手动中断;
     */
    private int exitcode = 0;
    /**
     * 海康跟大华的SDK回调的监控数据会存入到PS流中
     */
    private PipedInputStream pin;
    private PipedOutputStream pout;

    public CameraPush(YCamera yCamera) throws IOException {
        pout = new PipedOutputStream();
        pin = new PipedInputStream(pout);
        this.yCamera = yCamera;
    }

    private List<String> command = new ArrayList<>();
    private Process process;
    private boolean running = true; // 启动

    /**
     * 推送视频流数据包
     */
    @Override
    public void run() {
        if (yCamera.getCameraType().equals(YCamera.CAMERA_TYPE_HK_SDK_H264) || yCamera.getCameraType().equals(YCamera.CAMERA_TYPE_DH_SDK_H264)) {
            psStream();
        } else {
            rtspH264();
        }
    }

    /**
     * 推PS流，海康和大华的SDK会使用此方法
     */
    private void psStream() {
        grabber = new FFmpegFrameGrabber(pin, 0);

        long stime = System.currentTimeMillis();
        //从这里就要等待海康/大华/宇视等设备sdk回调函数传输数据，所以需要开始阻塞等待，这里只等三秒，大家可以根据自己需要修改
        long waitStreamDelay = 3000;
        for (; ; ) {
            logger.info("等待检查流");
            //检测管道流中是否存在数据，如果3s后依然没有写入1024的数据，则认为管道流中无数据，避免grabber.start();发生阻塞
            if (System.currentTimeMillis() - stime > waitStreamDelay) {
                return;
            }
            try {
                if (pin.available() == 1024) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
            }
        }
        logger.info("开始录制");
        try {
            avutil.av_log_set_level(avutil.AV_LOG_ERROR);
            FFmpegLogCallback.set();

            grabber.start();

            recorder = new FFmpegFrameRecorder(yCamera.getRtmp(), grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("flv");
            recorder.setCloseOutputStream(false);
            recorder.start(grabber.getFormatContext());
            logger.info("开始推送");
            long startTime = 0;
            long videoTS = 0;
            AVPacket pkt;
            while (running == true) {
                pkt = grabber.grabPacket();
                if (pkt == null || pkt.size() <= 0 || pkt.data() == null) {
                    continue;
                }
                if (pkt.stream_index() == 1) {
                    av_packet_unref(pkt);
                    continue;
                }
                if (startTime == 0) {
                    startTime = System.currentTimeMillis();
                }
                videoTS = 1000 * (System.currentTimeMillis() - startTime);
                if (videoTS > recorder.getTimestamp()) {
                    recorder.setTimestamp((videoTS));
                }
                recorder.recordPacket(pkt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            release();
        }
    }

    /**
     * H264的RTSP服务
     */
    private void rtspH264() {
        try {
            grabber = new FFmpegFrameGrabber(yCamera.getRtsp());
            grabber.setOption("rtsp_transport", "tcp");
            grabber.setOption("rtsp_flags", "prefer_tcp");
            grabber.setOption("stimeout", "2000000");
            grabber.setOption("threads", "1");
            // 设置缓存大小，提高画质、减少卡顿花屏
            grabber.setOption("buffer_size", "1024000");
            // 读写超时，适用于所有协议的通用读写超时
            grabber.setOption("rw_timeout", "5000000");
            // 探测视频流信息，为空默认5000000微秒
            grabber.setOption("probesize", "5000000");
            // 解析视频流信息，为空默认5000000微秒
            grabber.setOption("analyzeduration", "5000000");
            // 设置采集器构造超时时间
            // 设置码率，使用字码率
            grabber.start();

            int width = grabber.getImageWidth();
            int height = grabber.getImageHeight();
            // 若视频像素值为0，说明拉流异常，程序结束
            if (width == 0 && height == 0) {
                logger.error(yCamera.getRtsp() + "  拉流异常！");
                grabber.stop();
                grabber.close();
                release();
                return;
            }
            recorder = new FFmpegFrameRecorder(yCamera.getRtmp(), grabber.getImageWidth(), grabber.getImageHeight());
            // 封装flv格式
            recorder.setFormat("flv");
            // 不让recorder关联关闭outputStream
            recorder.setCloseOutputStream(false);
            recorder.start(grabber.getFormatContext());

            logger.debug("开始推流 设备信息：{}" + yCamera.toString());
            grabber.flush();
            // 时间戳计算
            long startTime = 0;
            long videoTS = 0;
            AVPacket pkt;
            for (int no_frame_index = 0; no_frame_index < 5 && err_index < 5; ) {
                long startGrab = System.currentTimeMillis();
                if (exitcode == 1 || running == false) {
                    break;
                }
                pkt = grabber.grabPacket();
                if ((System.currentTimeMillis() - startGrab) > 5000) {
                    logger.info("视频流网络异常 {}", yCamera.getRtsp());
                    break;
                }
                if (pkt == null || pkt.size() == 0 || pkt.data() == null) {
                    // 空包记录次数跳过
                    logger.warn("JavaCV 出现空包 ：{}", yCamera.getRtsp());
                    no_frame_index++;
                    continue;
                }
                // 过滤音频
                if (pkt.stream_index() == 1) {
                    av_packet_unref(pkt);
                    continue;
                }
                if (startTime == 0) {
                    startTime = System.currentTimeMillis();
                }
                videoTS = 1000 * (System.currentTimeMillis() - startTime);
                // 判断时间偏移
                if (videoTS > recorder.getTimestamp()) {
                    // 矫正时间戳
                    recorder.setTimestamp((videoTS));
                }
                recorder.recordPacket(pkt);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } finally {
            release();
            logger.info("推流结束 {}", yCamera.getRtsp());
        }
    }

    /**
     * 异步接收海康/大华/宇视设备sdk回调实时视频裸流数据
     *
     * @param data
     * @param size
     */
    public void push(byte[] data, int size) {
        try {
            pout.write(data, 0, size);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 执行命令
     *
     * @param command 命令
     * @return 控制台输出结果
     */
    public List<String> executeNewFlow(String command) {
        List<String> rspList = new ArrayList<String>();
        Runtime run = Runtime.getRuntime();
        try {
            process = run.exec("/bin/bash", null, null);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())), true);
            out.println(command);
            out.println("exit");// 这个命令必须执行，否则in流不结束。
            String rspLine = "";
            while (running && (rspLine = in.readLine()) != null) {
                System.out.println(rspLine);
                rspList.add(rspLine);
            }
            process.waitFor();
            in.close();
            out.close();
            stopFFmpeg();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return rspList;
    }


    /**
     * 添加命令参数
     *
     * @param argument
     * @return
     */
    private CameraPush addArgument(String argument) {
        command.add(argument);
        return this;
    }

    /**
     * 关闭FFmpeg
     */
    public void stopFFmpeg() {
        try {
            CacheUtil.STREATMAP.remove(yCamera.getToken());
            CameraController.JOBMAP.remove(yCamera.getToken());
            CacheUtil.PUSHMAP.remove(yCamera.getToken());
            this.process.destroy();
        } catch (Exception e) {
            process.destroyForcibly();
        }
    }


    /**
     * 资源释放
     */
    public void release() {
        try {
            CacheUtil.STREATMAP.remove(yCamera.getToken());
            CameraController.JOBMAP.remove(yCamera.getToken());
            CacheUtil.PUSHMAP.remove(yCamera.getToken());
            grabber.stop();
            grabber.close();
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setExitcode(int exitcode) {
        this.exitcode = exitcode;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}