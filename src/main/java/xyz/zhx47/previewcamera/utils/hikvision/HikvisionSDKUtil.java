package xyz.zhx47.previewcamera.utils.hikvision;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import xyz.zhx47.previewcamera.utils.CacheUtil;
import xyz.zhx47.previewcamera.utils.CameraPush;
import xyz.zhx47.previewcamera.utils.SDK;

import java.io.IOException;

/**
 * @author zhx
 * @date 2022-07-29 11:39
 * @description 海康威视工具类
 */
public class HikvisionSDKUtil {
    private final static Logger logger = LoggerFactory.getLogger(HikvisionSDKUtil.class);

    private static SDK sdk;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        sdk = applicationContext.getBean(SDK.class);
    }

    private int lPlay = -1;  //预览句柄
    private int lUserID = -1;  //登陆句柄
    FRealDataCallBack fRealDataCallBack = null;//预览回调函数实现
    private boolean running = true;

    /**
     * 预览摄像头
     *
     * @param token   用于获取到推送线程
     * @param channel 通道
     * @throws Exception
     */
    public void previewCamera(String token, String channel) throws Exception {
        HCNetSDK.NET_DVR_PREVIEWINFO strClientInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
        strClientInfo.read();
        strClientInfo.hPlayWnd = null;
        strClientInfo.byProtoType = 0;
        if (StringUtils.isNotBlank(channel)) {
            strClientInfo.lChannel = Integer.parseInt(channel);
        } else {
            strClientInfo.lChannel = 1;
        }
        strClientInfo.dwLinkMode = 0;
        strClientInfo.dwStreamType = 0;
        strClientInfo.bBlocked = 1;
        strClientInfo.byPreviewMode = 0;
        strClientInfo.write();
        //回调函数定义必须是全局的
        if (fRealDataCallBack == null) {
            fRealDataCallBack = new FRealDataCallBack();
        }
        lPlay = sdk.getHikvisionSDK().NET_DVR_RealPlay_V40(lUserID, strClientInfo, fRealDataCallBack, null);
        if (lPlay == -1) {
            int iErr = sdk.getHikvisionSDK().NET_DVR_GetLastError();
            logger.error("取流失败:{}", iErr);
            return;
        }
        this.token = token;
        CacheUtil.PUSHMAP.get(token).start();
        while (running) {
        }
        if (lPlay >= 0) {
            if (sdk.getHikvisionSDK().NET_DVR_StopRealPlay(lPlay)) {
                logger.info("停止取流");
            } else {
                int iErr = sdk.getHikvisionSDK().NET_DVR_GetLastError();
                logger.error("停止取流出现了异常:{}", iErr);
            }
        }
        sdk.getHikvisionSDK().NET_DVR_Logout(lUserID);
    }

    private String token;

    static class FExceptionCallBack_Imp implements HCNetSDK.FExceptionCallBack {
        public void invoke(int dwType, int lUserID, int lHandle, Pointer pUser) {
            logger.error("异常事件类型: {}", dwType);
        }
    }


    class FRealDataCallBack implements HCNetSDK.FRealDataCallBack_V30 {

        //预览回调
        public void invoke(int lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, Pointer pUser) throws IOException {
            if (dwDataType == HCNetSDK.NET_DVR_STREAMDATA) {
                byte[] outputData = pBuffer.getPointer().getByteArray(0, dwBufSize);
                CameraPush cameraPush = CacheUtil.PUSHMAP.get(token);
                if (cameraPush != null) {
                    CacheUtil.PUSHMAP.get(token).push(outputData, outputData.length);
                } else {
                    setRunning(false);
                }
            }
        }
    }

    public boolean login(String address, String username, String password, int port) throws Exception {
        Pointer pUser = null;
        if (!sdk.getHikvisionSDK().NET_DVR_SetExceptionCallBack_V30(0, 0, new FExceptionCallBack_Imp(), pUser)) {
            return false;
        }

        //登录设备，每一台设备分别登录; 登录句柄是唯一的，可以区分设备
        HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();//设备登录信息
        HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();//设备信息

        m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(address.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, address.length());

        m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(username.getBytes(), 0, m_strLoginInfo.sUserName, 0, username.length());

        m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(password.getBytes(), 0, m_strLoginInfo.sPassword, 0, password.length());

        m_strLoginInfo.wPort = (short) port; //SDK端口
        m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
        m_strLoginInfo.write();

        lUserID = sdk.getHikvisionSDK().NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
        if (lUserID == -1) {
            logger.error("登录失败，错误码为:{}", sdk.getHikvisionSDK().NET_DVR_GetLastError());
        } else {
            logger.info("{}, {}, {}, {}", address, port, username, password);
            logger.info("{} :设备登录成功! 设备序列号:{}", address, new String(m_strDeviceInfo.struDeviceV30.sSerialNumber).trim());
            m_strDeviceInfo.read();
        }
        return lUserID != -1;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}