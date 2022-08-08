package xyz.zhx47.previewcamera.utils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import xyz.zhx47.previewcamera.utils.dahuatech.NetSDKLib;
import xyz.zhx47.previewcamera.utils.hikvision.HCNetSDK;

import java.io.File;
import java.time.LocalDate;

@Data
@Component
public class SDK {
    private final static Logger logger = LoggerFactory.getLogger(SDK.class);

    @Value("${sdk.hikvision}")
    private String hikvision;
    @Value("${sdk.dahuatech}")
    private String dahuatech;

    private HCNetSDK hcNetSDK = null;
    private NetSDKLib netSDKLib = null;

    private static final int waitTime = 5000;
    private static final int tryTimes = 1;
    private static DisConnect disConnect = new DisConnect();

    /**
     * 获取到海康SDK对象
     *
     * @return 海康SDK对象
     * @throws Exception 未配置SDK路径 抛出异常
     */
    public HCNetSDK getHikvisionSDK() throws Exception {
        if (hcNetSDK == null) {
            if (StringUtils.isNotBlank(hikvision)) {
                if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                    hcNetSDK = (HCNetSDK) Native.loadLibrary(hikvision + "/libhcnetsdk.so", HCNetSDK.class);
                } else {
                    hcNetSDK = (HCNetSDK) Native.loadLibrary(hikvision + "\\HCNetSDK.dll", HCNetSDK.class);
                }
                if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                    logger.info("Linux系统，重新配置初始化参数");
                    // 库的绝对路径
                    //设置HCNetSDKCom组件库所在路径
                    String strPathCom = hikvision;
                    HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
                    System.arraycopy(strPathCom.getBytes(), 0, struComPath.sPath, 0, strPathCom.length());
                    struComPath.write();
                    hcNetSDK.NET_DVR_SetSDKInitCfg(2, struComPath.getPointer());

                    //设置libcrypto.so所在路径
                    HCNetSDK.BYTE_ARRAY ptrByteArrayCrypto = new HCNetSDK.BYTE_ARRAY(256);
                    String strPathCrypto = hikvision + "/libcrypto.so.1.1";
                    System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
                    ptrByteArrayCrypto.write();
                    hcNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArrayCrypto.getPointer());

                    //设置libssl.so所在路径
                    HCNetSDK.BYTE_ARRAY ptrByteArraySsl = new HCNetSDK.BYTE_ARRAY(256);
                    String strPathSsl = hikvision + "/libssl.so.1.1";
                    System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
                    ptrByteArraySsl.write();
                    hcNetSDK.NET_DVR_SetSDKInitCfg(4, ptrByteArraySsl.getPointer());
                }
                boolean initSuc = hcNetSDK.NET_DVR_Init();
                if (!initSuc) {
                    logger.error("海康SDK初始化失败");
                } else {
                    logger.info("海康SDK初始化成功");
                    if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                        hcNetSDK.NET_DVR_SetLogToFile(3, hikvision + "/sdkLog", false);
                    } else {
                        hcNetSDK.NET_DVR_SetLogToFile(3, hikvision + "\\sdkLog", false);
                    }
                }
            } else {
                throw new Exception("系统未配置海康SDK路径");
            }
        }
        return hcNetSDK;
    }

    /**
     * 获取到大华SDK对象
     *
     * @return 大华SDK对象
     * @throws Exception 未配置SDK路径 抛出异常
     */
    public NetSDKLib getDahuaTechSDK() throws Exception {
        if (netSDKLib == null) {
            if (StringUtils.isNotBlank(dahuatech)) {
                if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                    netSDKLib = (NetSDKLib) Native.loadLibrary(dahuatech + "/libdhnetsdk.so", NetSDKLib.class);
                } else {
                    netSDKLib = (NetSDKLib) Native.loadLibrary(dahuatech + "\\dhnetsdk.dll", NetSDKLib.class);
                }
                boolean initSuc = netSDKLib.CLIENT_Init(disConnect, null);
                if (!initSuc) {
                    logger.error("海康大华初始化失败");
                } else {
                    NetSDKLib.LOG_SET_PRINT_INFO setLog = new NetSDKLib.LOG_SET_PRINT_INFO();
                    File path = new File(dahuatech + "/sdklog/");
                    if (!path.exists()) {
                        path.mkdir();
                    }
                    String logPath = dahuatech + "\\sdklog\\" + LocalDate.now() + ".log";
                    setLog.nPrintStrategy = 0;
                    setLog.bSetFilePath = 1;
                    System.arraycopy(logPath.getBytes(), 0, setLog.szLogFilePath, 0, logPath.getBytes().length);
                    System.out.println(logPath);
                    setLog.bSetPrintStrategy = 1;
                    if (!netSDKLib.CLIENT_LogOpen(setLog)) {
                        System.err.println("Failed to open NetSDK log");
                    }

                    netSDKLib.CLIENT_SetConnectTime(waitTime, tryTimes);
                    NetSDKLib.NET_PARAM netParam = new NetSDKLib.NET_PARAM();
                    netParam.nConnectTime = 10000;      // 登录时尝试建立链接的超时时间
                    netParam.nGetConnInfoTime = 3000;   // 设置子连接的超时时间
                    netParam.nGetDevInfoTime = 3000;//获取设备信息超时时间，为0默认1000ms
                    netSDKLib.CLIENT_SetNetworkParam(netParam);
                }
                if (!initSuc) {
                    logger.error("大华SDK初始化失败");
                } else {
                    logger.info("大华SDK初始化成功");
                }
            } else {
                throw new Exception("系统未配置海康SDK路径");
            }
        }
        return netSDKLib;
    }

    // 设备断线回调: 通过 CLIENT_Init 设置该回调函数，当设备出现断线时，SDK会调用该函数
    private static class DisConnect implements NetSDKLib.fDisConnect {
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            logger.error("大华设备断开连接  {}:{}", pchDVRIP, nDVRPort);
        }
    }
}
