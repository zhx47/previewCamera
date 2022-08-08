package xyz.zhx47.previewcamera.utils.dahuatech;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;

import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

/**
 * 大华的SDK接口
 */
public interface NetSDKLib extends Library {

    /**
     * 设备序列号字符长度
     */
    int NET_SERIALNO_LEN = 48;

    /**
     * 日志路径名最大长度
     */
    int MAX_LOG_PATH_LEN = 260;

    /**
     * 配合EM_REAL_DATA_TYPE使用,码流转换后的数据回调函数(fRealDataCallBackEx,fDataCallBack)中的参数dwDataType的值为NET_DATA_CALL_BACK_VALUE + emDataType
     */
    int NET_DATA_CALL_BACK_VALUE = 1000;

    /**
     * 初始化SDK, 在调用SDK另外函数之前调用
     *
     * @param cbDisConnect 断线回调函数
     * @param dwUser       用户数据
     * @return 成功返回TRUE，失败返回FALSE
     */
    boolean CLIENT_Init(Callback cbDisConnect, Pointer dwUser);

    /**
     * 打开日志功能
     *
     * @param pstLogPrintInfo 日志参数
     * @return 是否开启成功
     */
    boolean CLIENT_LogOpen(LOG_SET_PRINT_INFO pstLogPrintInfo);

    /**
     * 设置连接设备等待时间
     *
     * @param nWaitTime 连接设备等待时间（单位毫秒）
     * @param nTryTimes 连接次数
     */
    void CLIENT_SetConnectTime(int nWaitTime, int nTryTimes);

    /**
     * 设置网络参数
     *
     * @param pNetParam 网络参数
     */
    void CLIENT_SetNetworkParam(NET_PARAM pNetParam);

    /**
     * 登录设备
     *
     * @param pchDVRIP     设备IP
     * @param wDVRPort     设备端口
     * @param pchUserName  用户名
     * @param pchPassword  用户密码
     * @param nSpecCap     设备支持的能力，值为2表示主动侦听模式下的用户登陆(车载dvr登录),请参阅EM_LOGIN_SPAC_CAP_TYPE
     * @param pCapParam    对nSpecCap 的补充参数，nSpecCap = 2时，pCapParam填充设备序列号字串(车载dvr登录)
     * @param lpDeviceInfo 设备信息，属于输出参数，填NULL效果类似nSpecCap=10
     * @param error        (当函数返回成功时,该参数的值无意义)，返回登录错误码
     * @return 成功返回非0(登录句柄)，失败返回0。
     */
    LLong CLIENT_LoginEx2(String pchDVRIP, int wDVRPort, String pchUserName, String pchPassword, int nSpecCap, Pointer pCapParam, NET_DEVICEINFO_Ex lpDeviceInfo, IntByReference error);

    /**
     * 指定回调数据类型 实施监视(预览)
     *
     * @param lLoginID    登陆句柄
     * @param pstInParam  输入参数
     * @param pstOutParam 输出参数
     * @param dwWaitTime  等待时间
     * @return 成功返回句柄，失败返回0。
     */
    LLong CLIENT_RealPlayByDataType(LLong lLoginID, NET_IN_REALPLAY_BY_DATA_TYPE pstInParam, NET_OUT_REALPLAY_BY_DATA_TYPE pstOutParam, int dwWaitTime);

    /**
     * 停止实时监视
     *
     * @param lRealHandle 预览句柄
     * @return 成功返回TRUE，失败返回FALSE
     */
    boolean CLIENT_StopRealPlay(LLong lRealHandle);

    /**
     * 登出设备
     *
     * @param lLoginID 登陆句柄
     * @return 成功返回TRUE，失败返回FALSE。
     */
    boolean CLIENT_Logout(LLong lLoginID);

    /**
     * 获取函数执行的错误码
     *
     * @return 返回错误码
     */
    int CLIENT_GetLastError();










    /**
     * 日志打印参数
     */
    class LOG_SET_PRINT_INFO extends Structure {
        public int dwSize;
        public int bSetFilePath;//是否重设日志路径, BOOL类型，取值0或1
        public byte[] szLogFilePath = new byte[MAX_LOG_PATH_LEN];//日志路径(默认"./sdk_log/sdk_log.log")
        public int bSetFileSize;//是否重设日志文件大小, BOOL类型，取值0或1
        public int nFileSize;//每个日志文件的大小(默认大小10240),单位:比特, 类型为unsigned int
        public int bSetFileNum;//是否重设日志文件个数, BOOL类型，取值0或1
        public int nFileNum;//绕接日志文件个数(默认大小10), 类型为unsigned int
        public int bSetPrintStrategy;//是否重设日志打印输出策略, BOOL类型，取值0或1
        public int nPrintStrategy;//日志输出策略,0:输出到文件(默认); 1:输出到窗口, 类型为unsigned int
        public byte[] byReserved = new byte[4];                            // 字节对齐
        public Pointer cbSDKLogCallBack;                        // 日志回调，需要将sdk日志回调出来时设置，默认为NULL
        public Pointer dwUser;                                    // 用户数据

        public LOG_SET_PRINT_INFO() {
            this.dwSize = this.size();
        }
    }

    /**
     * 登陆网络环境
     */
    class NET_PARAM extends Structure {

        /**
         * 等待超时时间(毫秒为单位)，为0默认5000ms
         */
        public int nWaittime;

        /**
         * 连接超时时间(毫秒为单位)，为0默认1500ms
         */
        public int nConnectTime;

        /**
         * 连接尝试次数，为0默认1次
         */
        public int nConnectTryNum;

        /**
         * 子连接之间的等待时间(毫秒为单位)，为0默认10ms
         */
        public int nSubConnectSpaceTime;

        /**
         * 获取设备信息超时时间，为0默认1000ms
         */
        public int nGetDevInfoTime;

        /**
         * 每个连接接收数据缓冲大小(字节为单位)，为0默认250*1024
         */
        public int nConnectBufSize;

        /**
         * 获取子连接信息超时时间(毫秒为单位)，为0默认1000ms
         */
        public int nGetConnInfoTime;

        /**
         * 按时间查询录像文件的超时时间(毫秒为单位),为0默认为3000ms
         */
        public int nSearchRecordTime;

        /**
         * 检测子链接断线等待时间(毫秒为单位)，为0默认为60000ms
         */
        public int nsubDisconnetTime;

        /**
         * 网络类型, 0-LAN, 1-WAN
         */
        public byte byNetType;

        /**
         * 回放数据接收缓冲大小（M为单位），为0默认为4M
         */
        public byte byPlaybackBufSize;

        /**
         * 心跳检测断线时间(单位为秒),为0默认为60s,最小时间为2s
         */
        public byte bDetectDisconnTime;

        /**
         * 心跳包发送间隔(单位为秒),为0默认为10s,最小间隔为2s
         */
        public byte bKeepLifeInterval;

        /**
         * 实时图片接收缓冲大小（字节为单位），为0默认为2*1024*1024
         */
        public int nPicBufSize;

        /**
         * 保留字段字段
         */
        public byte[] bReserved = new byte[4];
    }

    /**
     * 设备类型
     */
    class NET_DEVICE_TYPE extends Structure {

        /**
         *
         */
        public static final int NET_PRODUCT_NONE = 0;

        /**
         * 非实时MACE
         */
        public static final int NET_DVR_NONREALTIME_MACE = 1;

        /**
         * 非实时
         */
        public static final int NET_DVR_NONREALTIME = 2;

        /**
         * 网络视频服务器
         */
        public static final int NET_NVS_MPEG1 = 3;

        /**
         * MPEG1二路录像机
         */
        public static final int NET_DVR_MPEG1_2 = 4;

        /**
         * MPEG1八路录像机
         */
        public static final int NET_DVR_MPEG1_8 = 5;

        /**
         * MPEG4八路录像机
         */
        public static final int NET_DVR_MPEG4_8 = 6;

        /**
         * MPEG4十六路录像机
         */
        public static final int NET_DVR_MPEG4_16 = 7;

        /**
         * LB系列录像机
         */
        public static final int NET_DVR_MPEG4_SX2 = 8;

        /**
         * GB系列录像机
         */
        public static final int NET_DVR_MEPG4_ST2 = 9;

        /**
         * HB系列录像机
         */
        public static final int NET_DVR_MEPG4_SH2 = 10;

        /**
         * GBE系列录像机
         */
        public static final int NET_DVR_MPEG4_GBE = 11;

        /**
         * II代网络视频服务器
         */
        public static final int NET_DVR_MPEG4_NVSII = 12;

        /**
         * 新标准配置协议
         */
        public static final int NET_DVR_STD_NEW = 13;

        /**
         * DDNS服务器
         */
        public static final int NET_DVR_DDNS = 14;

        /**
         * ATM机
         */
        public static final int NET_DVR_ATM = 15;

        /**
         * 二代非实时NB系列机器
         */
        public static final int NET_NB_SERIAL = 16;

        /**
         * LN系列产品
         */
        public static final int NET_LN_SERIAL = 17;

        /**
         * BAV系列产品
         */
        public static final int NET_BAV_SERIAL = 18;

        /**
         * SDIP系列产品
         */
        public static final int NET_SDIP_SERIAL = 19;

        /**
         * IPC系列产品
         */
        public static final int NET_IPC_SERIAL = 20;

        /**
         * NVS B系列
         */
        public static final int NET_NVS_B = 21;

        /**
         * NVS H系列
         */
        public static final int NET_NVS_C = 22;

        /**
         * NVS S系列
         */
        public static final int NET_NVS_S = 23;

        /**
         * NVS E系列
         */
        public static final int NET_NVS_E = 24;

        /**
         * 从QueryDevState中查询设备类型,以字符串格式
         */
        public static final int NET_DVR_NEW_PROTOCOL = 25;

        /**
         * 解码器
         */
        public static final int NET_NVD_SERIAL = 26;

        /**
         * N5
         */
        public static final int NET_DVR_N5 = 27;

        /**
         * 混合DVR
         */
        public static final int NET_DVR_MIX_DVR = 28;

        /**
         * SVR系列
         */
        public static final int NET_SVR_SERIAL = 29;

        /**
         * SVR-BS
         */
        public static final int NET_SVR_BS = 30;

        /**
         * NVR系列
         */
        public static final int NET_NVR_SERIAL = 31;

        /**
         * N51
         */
        public static final int NET_DVR_N51 = 32;

        /**
         * ITSE 智能分析盒
         */
        public static final int NET_ITSE_SERIAL = 33;

        /**
         * 智能交通像机设备
         */
        public static final int NET_ITC_SERIAL = 34;

        /**
         * 雷达测速仪HWS
         */
        public static final int NET_HWS_SERIAL = 35;

        /**
         * 便携式音视频录像机
         */
        public static final int NET_PVR_SERIAL = 36;

        /**
         * IVS（智能视频服务器系列）
         */
        public static final int NET_IVS_SERIAL = 37;

        /**
         * 通用智能视频侦测服务器
         */
        public static final int NET_IVS_B = 38;

        /**
         * 人脸识别服务器
         */
        public static final int NET_IVS_F = 39;

        /**
         * 视频质量诊断服务器
         */
        public static final int NET_IVS_V = 40;

        /**
         * 矩阵
         */
        public static final int NET_MATRIX_SERIAL = 41;

        /**
         * N52
         */
        public static final int NET_DVR_N52 = 42;

        /**
         * N56
         */
        public static final int NET_DVR_N56 = 43;

        /**
         * ESS
         */
        public static final int NET_ESS_SERIAL = 44;

        /**
         * 人数统计服务器
         */
        public static final int NET_IVS_PC = 45;

        /**
         * pc-nvr
         */
        public static final int NET_PC_NVR = 46;

        /**
         * 大屏控制器
         */
        public static final int NET_DSCON = 47;

        /**
         * 网络视频存储服务器
         */
        public static final int NET_EVS = 48;

        /**
         * 嵌入式智能分析视频系统
         */
        public static final int NET_EIVS = 49;

        /**
         * DVR-N6
         */
        public static final int NET_DVR_N6 = 50;

        /**
         * 万能解码器
         */
        public static final int NET_UDS = 51;

        /**
         * 银行报警主机
         */
        public static final int NET_AF6016 = 52;

        /**
         * 视频网络报警主机
         */
        public static final int NET_AS5008 = 53;

        /**
         * 网络报警主机
         */
        public static final int NET_AH2008 = 54;

        /**
         * 报警主机系列
         */
        public static final int NET_A_SERIAL = 55;

        /**
         * 门禁系列产品
         */
        public static final int NET_BSC_SERIAL = 56;

        /**
         * NVS系列产品
         */
        public static final int NET_NVS_SERIAL = 57;

        /**
         * VTO系列产品
         */
        public static final int NET_VTO_SERIAL = 58;

        /**
         * VTNC系列产品
         */
        public static final int NET_VTNC_SERIAL = 59;

        /**
         * TPC系列产品, 即热成像设备
         */
        public static final int NET_TPC_SERIAL = 60;

        /**
         * 无线中继设备
         */
        public static final int NET_ASM_SERIAL = 61;

        /**
         * 管理机
         */
        public static final int NET_VTS_SERIAL = 62;
    }

    /**
     * 设备信息
     */
    class NET_DEVICEINFO_Ex extends Structure {

        /**
         * 序列号
         */
        public byte[] sSerialNumber = new byte[NET_SERIALNO_LEN];

        /**
         * DVR报警输入个数
         */
        public int byAlarmInPortNum;

        /**
         * DVR报警输出个数
         */
        public int byAlarmOutPortNum;

        /**
         * DVR硬盘个数
         */
        public int byDiskNum;

        /**
         * DVR类型,见枚举NET_DEVICE_TYPE
         */
        public int byDVRType;

        /**
         * DVR通道个数
         */
        public int byChanNum;

        /**
         * 在线超时时间,为0表示不限制登陆,非0表示限制的分钟数
         */
        public byte byLimitLoginTime;

        /**
         * 当登陆失败原因为密码错误时,通过此参数通知用户,剩余登陆次数,为0时表示此参数无效
         */
        public byte byLeftLogTimes;

        /**
         * 保留字节,字节对齐
         */
        public byte[] bReserved = new byte[2];

        /**
         * 当登陆失败,用户解锁剩余时间（秒数）, -1表示设备未设置该参数
         */
        public int byLockLeftTime;

        /**
         * 保留
         */
        public byte[] Reserved = new byte[24];
    }

    /**
     * 登录类型
     */
    class EM_LOGIN_SPAC_CAP_TYPE extends Structure {
        /**
         * TCP登陆, 默认方式
         */
        public static final int EM_LOGIN_SPEC_CAP_TCP = 0;

        /**
         * 无条件登陆
         */
        public static final int EM_LOGIN_SPEC_CAP_ANY = 1;

        /**
         * 主动注册的登入
         */
        public static final int EM_LOGIN_SPEC_CAP_SERVER_CONN = 2;

        /**
         * 组播登陆
         */
        public static final int EM_LOGIN_SPEC_CAP_MULTICAST = 3;

        /**
         * UDP方式下的登入
         */
        public static final int EM_LOGIN_SPEC_CAP_UDP = 4;

        /**
         * 只建主连接下的登入
         */
        public static final int EM_LOGIN_SPEC_CAP_MAIN_CONN_ONLY = 6;

        /**
         * SSL加密方式登陆
         */
        public static final int EM_LOGIN_SPEC_CAP_SSL = 7;

        /**
         * 登录智能盒远程设备
         */
        public static final int EM_LOGIN_SPEC_CAP_INTELLIGENT_BOX = 9;

        /**
         * 登录设备后不做取配置操作
         */
        public static final int EM_LOGIN_SPEC_CAP_NO_CONFIG = 10;

        /**
         * 用U盾设备的登入
         */
        public static final int EM_LOGIN_SPEC_CAP_U_LOGIN = 11;

        /**
         * LDAP方式登录
         */
        public static final int EM_LOGIN_SPEC_CAP_LDAP = 12;

        /**
         * AD（ActiveDirectory）登录方式
         */
        public static final int EM_LOGIN_SPEC_CAP_AD = 13;

        /**
         * Radius 登录方式
         */
        public static final int EM_LOGIN_SPEC_CAP_RADIUS = 14;

        /**
         * Socks5登陆方式
         */
        public static final int EM_LOGIN_SPEC_CAP_SOCKET_5 = 15;

        /**
         * 云登陆方式
         */
        public static final int EM_LOGIN_SPEC_CAP_CLOUD = 16;

        /**
         * 二次鉴权登陆方式
         */
        public static final int EM_LOGIN_SPEC_CAP_AUTH_TWICE = 17;

        /**
         * TS码流客户端登陆方式
         */
        public static final int EM_LOGIN_SPEC_CAP_TS = 18;

        /**
         * 为P2P登陆方式
         */
        public static final int EM_LOGIN_SPEC_CAP_P2P = 19;

        /**
         * 手机客户端登陆
         */
        public static final int EM_LOGIN_SPEC_CAP_MOBILE = 20;
    }


    /**
     * 开始实时监视并指定回调数据格式入参
     */
    class NET_IN_REALPLAY_BY_DATA_TYPE extends Structure {
        /**
         * 结构体大小，用于初始化,必须赋值
         */
        public int dwSize;

        /**
         * 预览通道编号
         */
        public int nChannelID;

        /**
         * 窗口句柄, HWND类型
         */
        public Pointer hWnd;

        /**
         * 码流类型 ，参考NET_RealPlayType
         */
        public int rType;

        /**
         * 数据回调函数
         */
        public fRealDataCallBackEx cbRealData;

        /**
         * 回调的数据类型，参考EM_REAL_DATA_TYPE
         */
        public int emDataType;

        /**
         * 用户自定义数据
         */
        public Pointer dwUser;

        /**
         * 转换后的文件名
         */
        public String szSaveFileName;

        /**
         * 数据回调函数-扩展
         */
        public fRealDataCallBackEx2 cbRealDataEx;

        /**
         * 音频格式,对应枚举EM_AUDIO_DATA_TYPE
         */
        public int emAudioType;

        public NET_IN_REALPLAY_BY_DATA_TYPE() {
            this.dwSize = this.size();
        }
    }

    /**
     * 开始实时监视并指定回调数据格式出参
     */
    class NET_OUT_REALPLAY_BY_DATA_TYPE extends Structure {
        /**
         * 结构体大小
         */
        public int dwSize;

        public NET_OUT_REALPLAY_BY_DATA_TYPE() {
            this.dwSize = this.size();
        }
    }

    /**
     * 预览类型
     */
    class NET_RealPlayType extends Structure {

        /**
         * 实时预览
         */
        public static final int NET_RType_Realplay = 0;

        /**
         * 多画面预览
         */
        public static final int NET_RType_Multiplay = 1;

        /**
         * 实时监视-主码流 ,等同于NET_RType_Realplay
         */
        public static final int NET_RType_Realplay_0 = 2;

        /**
         * 实时监视-从码流1
         */
        public static final int NET_RType_Realplay_1 = 3;

        /**
         * 实时监视-从码流2
         */
        public static final int NET_RType_Realplay_2 = 4;


        /**
         * 实时监视-从码流3
         */
        public static final int NET_RType_Realplay_3 = 5;

        /**
         * 多画面预览－1画面
         */
        public static final int NET_RType_Multiplay_1 = 6;

        /**
         * 多画面预览－4画面
         */
        public static final int NET_RType_Multiplay_4 = 7;

        /**
         * 多画面预览－8画面
         */
        public static final int NET_RType_Multiplay_8 = 8;

        /**
         * 多画面预览－9画面
         */
        public static final int NET_RType_Multiplay_9 = 9;

        /**
         * 多画面预览－16画面
         */
        public static final int NET_RType_Multiplay_16 = 10;

        /**
         * 多画面预览－6画面
         */
        public static final int NET_RType_Multiplay_6 = 11;

        /**
         * 多画面预览－12画面
         */
        public static final int NET_RType_Multiplay_12 = 12;

        /**
         * 多画面预览－25画面
         */
        public static final int NET_RType_Multiplay_25 = 13;

        /**
         * 多画面预览－36画面
         */
        public static final int NET_RType_Multiplay_36 = 14;

        /**
         * 多画面预览－64画面
         */
        public static final int NET_RType_Multiplay_64 = 15;

        /**
         * 不修改当前预览通道数
         */
        public static final int NET_RType_Multiplay_255 = 16;

        /**
         * 只拉音频, 非通用
         */
        public static final int NET_RType_Realplay_Audio = 17;

        /**
         * 带宽测试码流
         */
        public static final int NET_RType_Realplay_Test = 255;
    }

    /**
     * 实时监视回调数据类型
     */
    class EM_REAL_DATA_TYPE extends Structure {
        /**
         * 私有码流
         */
        public static final int EM_REAL_DATA_TYPE_PRIVATE = 0;

        /**
         * 国标PS码流
         */
        public static final int EM_REAL_DATA_TYPE_GBPS = 1;

        /**
         * TS码流
         */
        public static final int EM_REAL_DATA_TYPE_TS = 2;

        /**
         * MP4文件(从回调函数出来的是私有码流数据,参数dwDataType值为0)
         */
        public static final int EM_REAL_DATA_TYPE_MP4 = 3;

        /**
         * 裸H264码流
         */
        public static final int EM_REAL_DATA_TYPE_H264 = 4;          //

        /**
         * 流式FLV
         */
        public static final int EM_REAL_DATA_TYPE_FLV_STREAM = 5;     //
    }

    /**
     * 具体码流类型
     */
    class EM_REALDATA_FLAG extends Structure {
        /**
         * 原始数据标志
         */
        public static final int REALDATA_FLAG_RAW_DATA = 0;

        /**
         * 带有帧信息的数据标志
         */
        public static final int REALDATA_FLAG_DATA_WITH_FRAME_INFO = 1;

        /**
         * YUV数据标志
         */
        public static final int REALDATA_FLAG_YUV_DATA = 2;

        /**
         * PCM 音频数据标志
         */
        public static final int REALDATA_FLAG_PCM_AUDIO_DATA = 3;
    }

    /**
     * 断线回调函数
     */
    interface fDisConnect extends StdCallCallback {

        /**
         * 短线回调
         *
         * @param lLoginID 登陆句柄
         * @param pchDVRIP 设备IP
         * @param nDVRPort 设备端口
         * @param dwUser   用户数据，就是上面输入的用户数据
         */
        void invoke(LLong lLoginID, String pchDVRIP, int nDVRPort, Pointer dwUser);
    }

    /**
     * 回调函数：用于传出多种类型的实时数据
     */
    interface fRealDataCallBackEx extends StdCallCallback {

        /**
         * 回调方法
         *
         * @param lRealHandle 实时监视ID
         * @param dwDataType  标识回调出来的数据类型，只有dwFlag设置标识的数据才会回调出来： 具体码流类型请参考EM_REALDATA_FLAG; 转码流时dwDataType值请参考NET_DATA_CALL_BACK_VALUE说明
         * @param pBuffer     回调数据，根据数据类型的不同每次回调不同的长度的数据，除类型0，其他数据类型都是按帧，每次回调一帧数据
         * @param dwBufSize   回调数据参数结构体，根据不同的类型，参数结构也不一致，当类型为0(原始数据)和2(YUV数据) 时为0。当回调的数据类型为帧数据时param为一个tagVideoFrameParam结构体指针。当数据类型是PCM数据的时候param也是一个tagCBPCMDataParam结构体指针
         * @param param       回调数据的长度，根据不同的类型，长度也不同(单位字节)
         * @param dwUser      用户数据
         */
        void invoke(LLong lRealHandle, int dwDataType, Pointer pBuffer, int dwBufSize, int param, Pointer dwUser);
    }

    /**
     * 回调函数：实时监视数据回调函数原形--扩展
     */
    interface fRealDataCallBackEx2 extends StdCallCallback {

        /**
         * 回调方法
         *
         * @param lRealHandle 实时监视ID
         * @param dwDataType  标识回调出来的数据类型，只有dwFlag设置标识的数据才会回调出来： 具体码流类型请参考EM_REALDATA_FLAG; 转码流时dwDataType值请参考NET_DATA_CALL_BACK_VALUE说明
         * @param pBuffer     回调数据，根据数据类型的不同每次回调不同的长度的数据，除类型0，其他数据类型都是按帧，每次回调一帧数据
         * @param dwBufSize   回调数据参数结构体，根据不同的类型，参数结构也不一致，当类型为0(原始数据)和2(YUV数据) 时为0。当回调的数据类型为帧数据时param为一个tagVideoFrameParam结构体指针。当数据类型是PCM数据的时候param也是一个tagCBPCMDataParam结构体指针
         * @param param       回调数据的长度，根据不同的类型，长度也不同(单位字节)
         * @param dwUser      用户数据
         */
        void invoke(LLong lRealHandle, int dwDataType, Pointer pBuffer, int dwBufSize, LLong param, Pointer dwUser);
    }

    /**
     * 解决windows与linux类型长度不一致问题
     */
    class LLong extends IntegerType {
        private static final long serialVersionUID = 1L;

        public static int size;

        static {
            size = Native.LONG_SIZE;
            String arch = System.getProperty("os.arch").toLowerCase();
            if (arch.contains("64")) {
                size = 8;
            } else {
                size = 4;
            }
        }

        public LLong() {
            this(0);
        }

        public LLong(long value) {
            super(size, value);
        }
    }
}