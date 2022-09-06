package xyz.zhx47.previewcamera.utils.dahuatech;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import xyz.zhx47.previewcamera.utils.CacheUtil;
import xyz.zhx47.previewcamera.utils.CameraPush;
import xyz.zhx47.previewcamera.utils.SDK;

public class DahuatechSDKUtil {
    private final static Logger logger = LoggerFactory.getLogger(DahuatechSDKUtil.class);
    private static int DW_DATA_TYPE = NetSDKLib.NET_DATA_CALL_BACK_VALUE + NetSDKLib.EM_REAL_DATA_TYPE.EM_REAL_DATA_TYPE_GBPS;

    private static SDK sdk;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        sdk = applicationContext.getBean(SDK.class);
    }

    // 监视预览句柄
    private NetSDKLib.LLong lRealHandle = new NetSDKLib.LLong(0);

    // 设备信息
    private NetSDKLib.NET_DEVICEINFO_Ex m_stDeviceInfo = new NetSDKLib.NET_DEVICEINFO_Ex();
    // 登陆句柄
    private NetSDKLib.LLong m_hLoginHandle = new NetSDKLib.LLong(0);
    private CbfRealDataCallBackEx cbfRealDataCallBackEx = null;
//    private CBfRealDataCallBackEx2 cBfRealDataCallBackEx2 = null;

    private boolean running = true;
    private String token;

    /**
     * 实时预览拉流
     */
    public void previewCamera(String token, String cameraId) throws Exception {
        this.token = token;
        int nChannelID;
        if (StringUtils.isNotBlank(cameraId)) {
            nChannelID = Integer.parseInt(cameraId);
        } else {
            nChannelID = 0;
        }
        if (cbfRealDataCallBackEx == null) {
            cbfRealDataCallBackEx = new CbfRealDataCallBackEx();
        }
//        if (cBfRealDataCallBackEx2 == null) {
//            cBfRealDataCallBackEx2 = new CBfRealDataCallBackEx2();
//        }

        NetSDKLib.NET_IN_REALPLAY_BY_DATA_TYPE inParam = new NetSDKLib.NET_IN_REALPLAY_BY_DATA_TYPE();
        NetSDKLib.NET_OUT_REALPLAY_BY_DATA_TYPE outParam = new NetSDKLib.NET_OUT_REALPLAY_BY_DATA_TYPE();
        inParam.nChannelID = nChannelID;
        inParam.rType = NetSDKLib.NET_RealPlayType.NET_RType_Realplay;
        inParam.cbRealData = cbfRealDataCallBackEx;
        inParam.emDataType = NetSDKLib.EM_REAL_DATA_TYPE.EM_REAL_DATA_TYPE_GBPS;
        logger.info("大华摄像头开启预览，设置回调方法");
        lRealHandle = sdk.getDahuaTechSDK().CLIENT_RealPlayByDataType(m_hLoginHandle, inParam, outParam, 5000);

//        lRealHandle = sdk.getDahuaTechSDK().CLIENT_RealPlayEx(m_hLoginHandle, nChannelID, null, 0);
//        if (lRealHandle.longValue() != 0) {
//            sdk.getDahuaTechSDK().CLIENT_SetRealDataCallBackEx(lRealHandle, cbfRealDataCallBackEx, null, 31);
//        }

        logger.info("预览句柄：{}", lRealHandle.longValue());
        if (lRealHandle.longValue() == 0) {
            logger.error("开始实时监视失败，错误码{}", sdk.getDahuaTechSDK().CLIENT_GetLastError());
        } else {
            logger.info("开始实时监视成功！");
            CacheUtil.PUSHMAP.get(token).start();
            while (running) {
            }
            sdk.getDahuaTechSDK().CLIENT_StopRealPlay(lRealHandle);
            logger.info("停止预览！");
            logout();
            logger.info("退出登陆！");
        }
    }

    public boolean login(String m_strIp, int m_nPort, String m_strUser, String m_strPassword) throws Exception {
        // 登陆设备
        int nSpecCap = NetSDKLib.EM_LOGIN_SPAC_CAP_TYPE.EM_LOGIN_SPEC_CAP_TCP;
        IntByReference nError = new IntByReference(0);
        logger.info("大华摄像头调用SDK登陆：{}", m_strIp);
        m_hLoginHandle = sdk.getDahuaTechSDK().CLIENT_LoginEx2(m_strIp, m_nPort, m_strUser,
                m_strPassword, nSpecCap, null, m_stDeviceInfo, nError);
        if (m_hLoginHandle.longValue() != 0) {
            logger.info("{}, {}, {}, {}", m_strIp, m_nPort, m_strUser, m_strPassword);
            logger.info("{} :设备登录成功!", m_strIp);
        } else {
            logger.error("登录失败，错误码为:{}", sdk.getDahuaTechSDK().CLIENT_GetLastError());
        }
        return m_hLoginHandle.longValue() != 0;
    }

    private boolean logout() throws Exception {
        if (m_hLoginHandle.longValue() == 0) {
            return false;
        }

        boolean bRet = sdk.getDahuaTechSDK().CLIENT_Logout(m_hLoginHandle);
        if (bRet) {
            m_hLoginHandle.setValue(0);
        }
        return bRet;
    }

    /**
     * 实时监视数据回调函数--扩展(pBuffer内存由SDK内部申请释放)
     */
    private class CbfRealDataCallBackEx implements NetSDKLib.fRealDataCallBackEx {
        @Override
        public void invoke(NetSDKLib.LLong lRealHandle, int dwDataType, Pointer pBuffer,
                           int dwBufSize, int param, Pointer dwUser) {
            if (0 != lRealHandle.longValue()) {
                if (dwDataType == DW_DATA_TYPE) {
                    byte[] outputData = pBuffer.getByteArray(0, dwBufSize);
                    CameraPush cameraPush = CacheUtil.PUSHMAP.get(token);
                    if (cameraPush != null) {
                        CacheUtil.PUSHMAP.get(token).push(outputData, outputData.length);
                    } else {
                        setRunning(false);
                    }
                }
            }
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
