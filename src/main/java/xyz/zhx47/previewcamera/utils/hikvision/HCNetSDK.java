package xyz.zhx47.previewcamera.utils.hikvision;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.examples.win32.W32API.HWND;
import com.sun.jna.ptr.ByteByReference;

import java.io.IOException;

public interface HCNetSDK extends Library {

    /**
     * 设备地址最大长度
     */
    int NET_DVR_DEV_ADDRESS_MAX_LEN = 129;

    /**
     * 设备登陆用户名最大长度
     */
    int NET_DVR_LOGIN_USERNAME_MAX_LEN = 64;

    /**
     * 设备登陆用户密码最大长度
     */
    int NET_DVR_LOGIN_PASSWD_MAX_LEN = 64;

    /**
     * 序列号长度
     */
    int SERIALNO_LEN = 48;

    /**
     * 预览回调: 视频流数据（包括复合流和音视频分开的视频流数据）
     */
    int NET_DVR_STREAMDATA = 2;

    /**
     * 文件路径长度
     */
    int NET_SDK_MAX_FILE_PATH = 256;

    /**
     * 初始化SDK，调用其他SDK函数的前提。
     *
     * @return TRUE表示成功，FALSE表示失败。接口返回失败请调用NET_DVR_GetLastError获取错误码，通过错误码判断出错原因。
     */
    boolean NET_DVR_Init();

    /**
     * 返回最后操作的错误码
     *
     * @return 返回值为错误码。错误码主要分为网络通讯库、RTSP通讯库、软硬解库、语音对讲库等错误码
     */
    int NET_DVR_GetLastError();

    /**
     * 用户注销
     *
     * @param lUserID 用户ID号，NET_DVR_Login_V40等登录接口的返回值
     * @return TRUE表示成功，FALSE表示失败
     */
    boolean NET_DVR_Logout(int lUserID);

    /**
     * 注册接收异常、重连等消息的窗口句柄或回调函数。
     *
     * @param nMessage           消息，Linux下该参数保留
     * @param hWnd               接收异常信息消息的窗口句柄，Linux下该参数保留
     * @param fExceptionCallBack 接收异常消息的回调函数，回调当前异常的相关信息
     * @param pUser              用户数据
     * @return TRUE表示成功，FALSE表示失败
     */
    boolean NET_DVR_SetExceptionCallBack_V30(int nMessage, int hWnd, FExceptionCallBack fExceptionCallBack, Pointer pUser);

    /**
     * 异常回调接口
     */
    interface FExceptionCallBack extends Callback {
        /**
         * 回调方法，自定义业务代码
         *
         * @param dwType  异常或重连等消息的类型
         * @param lUserID 登录ID
         * @param lHandle 出现异常的相应类型的句柄
         * @param pUser   用户数据
         */
        void invoke(int dwType, int lUserID, int lHandle, Pointer pUser);
    }

    /**
     * 用户注册设备（支持异步登录）
     *
     * @param pLoginInfo   登录参数，包括设备地址、登录用户、密码等
     * @param lpDeviceInfo 设备信息(同步登录即pLoginInfo中bUseAsynLogin为0时有效)
     * @return 异步登录的状态、用户ID和设备信息通过NET_DVR_USER_LOGIN_INFO结构体中设置的回调函数(fLoginResultCallBack)返回。对于同步登录，接口返回-1表示登录失败，其他值表示返回的用户ID值。用户ID具有唯一性，后续对设备的操作都需要通过此ID实现
     */
    int NET_DVR_Login_V40(NET_DVR_USER_LOGIN_INFO pLoginInfo, NET_DVR_DEVICEINFO_V40 lpDeviceInfo);

    /**
     * 用户登录参数结构体
     */
    class NET_DVR_USER_LOGIN_INFO extends Structure {
        /**
         * 设备地址，IP 或者普通域名
         */
        public byte[] sDeviceAddress = new byte[NET_DVR_DEV_ADDRESS_MAX_LEN];

        /**
         * 是否启用能力集透传：0- 不启用透传，默认；1- 启用透传
         */
        public byte byUseTransport;

        /**
         * 设备端口号，例如：8000
         */
        public short wPort;

        /**
         * 登录用户名，例如：admin
         */
        public byte[] sUserName = new byte[NET_DVR_LOGIN_USERNAME_MAX_LEN];

        /**
         * 登录密码，例如：12345
         */
        public byte[] sPassword = new byte[NET_DVR_LOGIN_PASSWD_MAX_LEN];

        /**
         * 登录状态回调函数，bUseAsynLogin 为1时有效
         */
        public FLoginResultCallBack cbLoginResult;

        /**
         * 用户数据
         */
        public Pointer pUser;

        /**
         * 是否异步登录：0- 否，1- 是
         */
        public boolean bUseAsynLogin;

        /**
         * 代理服务器类型：0- 不使用代理，1- 使用标准代理，2- 使用EHome代理
         */
        public byte byProxyType;

        /**
         * 是否使用UTC时间：0- 不进行转换，默认；1- 输入输出UTC时间，SDK进行与设备时区的转换；2- 输入输出平台本地时间，SDK进行与设备时区的转换
         */
        public byte byUseUTCTime;

        /**
         * 登录模式(不同模式具体含义详见“Remarks”说明)：0- SDK私有协议，1- ISAPI协议，2- 自适应（设备支持协议类型未知时使用，一般不建议）
         */
        public byte byLoginMode;

        /**
         * ISAPI协议或海康私有协议登录时是否启用TLS(即byLoginMode为0或1均有效)：0-不使用TLS，1-使用TLS 2-自适应（自适应登录时，会对性能有较大影响，自适应时要同时发起HTTP和HTTPS，设备支持协议类型未知时使用，一般不建议。）
         */
        public byte byHttps;

        /**
         * 代理服务器序号，添加代理服务器信息时相对应的服务器数组下表值
         */
        public int iProxyID;

        /**
         * 认证方式: <br/>
         * 0-不认证 <br/>
         * 1-双向认证（暂不支持） <br/>
         * 2-单向认证；使用海康私有协议登录且使用TLS链路时有效（即SDK Over TLS，byLoginMode为0且byHttps为1） <br/>
         * <br/>
         * 选择0时，无需加载CA证书 <br/>
         * 择2时，需要调用接口NET_DVR_SetSDKLocalCfg加载CA证书，枚举值为NET_SDK_LOCAL_CFG_CERTIFICATION。
         */
        public byte byVerifyMode;

        /**
         * 保留，置为0
         */
        public byte[] byRes3 = new byte[119];
    }

    /**
     * 设备参数结构体
     */
    class NET_DVR_DEVICEINFO_V40 extends Structure {
        /**
         * 设备参数
         */
        public NET_DVR_DEVICEINFO_V30 struDeviceV30 = new NET_DVR_DEVICEINFO_V30();

        /**
         * 设备是否支持锁定功能，bySupportLock为1时，dwSurplusLockTime和byRetryLoginTime有效
         */
        public byte bySupportLock;

        /**
         * 剩余可尝试登陆的次数，用户名、密码错误时，此参数有效
         */
        public byte byRetryLoginTime;

        /**
         * 密码安全等级：0- 无效，1- 默认密码，2- 有效密码，3- 风险较高的密码，当管理员用户的密码为出厂默认密码（12345）或者风险较高的密码时，建议上层客户端提示用户更改密码
         */
        public byte byPasswordLevel;

        /**
         * 代理服务器类型：0- 不使用代理，1- 使用标准代理，2- 使用EHome代理
         */
        public byte byProxyType;

        /**
         * 剩余时间，单位：秒，用户锁定时此参数有效。在锁定期间，用户尝试登陆，不管用户名密码输入对错，设备锁定剩余时间重新恢复到30分钟
         */
        public int dwSurplusLockTime;

        /**
         * 字符编码类型（SDK所有接口返回的字符串编码类型，透传接口除外）：0- 无字符编码信息(老设备)，1- GB2312(简体中文)，2- GBK，3- BIG5(繁体中文)，4- Shift_JIS(日文)，5- EUC-KR(韩文)，6- UTF-8，7- ISO8859-1，8- ISO8859-2，9- ISO8859-3，…，依次类推，21- ISO8859-15(西欧)
         */
        public byte byCharEncodeType;

        /**
         * 支持v50版本的设备参数获取，设备名称和设备类型名称长度扩展为64字节
         */
        public byte bySupportDev5;

        /**
         * 登录模式 0-Private登录 1-ISAPI登录
         */
        public byte byLoginMode;

        /**
         * 保留，置为0
         */
        public int dwOEMCode;

        /**
         * 该用户密码剩余有效天数，单位：天，返回负值，表示密码已经超期使用，例如“-3表示密码已经超期使用3天”
         */
        public int iResidualValidity;

        /**
         * iResidualValidity字段是否有效，0-无效，1-有效
         */
        public byte byResidualValidity;

        /**
         * 独立音轨接入的设备，起始接入通道号，0-为保留字节，无实际含义，音轨通道号不能从0开始
         */
        public byte bySingleStartDTalkChan;

        /**
         * 独立音轨接入的设备的通道总数，0-表示不支持
         */
        public byte bySingleDTalkChanNums;

        /**
         * 0-无效，1-管理员创建一个非管理员用户为其设置密码，该非管理员用户正确登录设备后要提示“请修改初始登录密码”，未修改的情况下，用户每次登入都会进行提醒；2-当非管理员用户的密码被管理员修改，该非管理员用户再次正确登录设备后，需要提示“请重新设置登录密码”，未修改的情况下，用户每次登入都会进行提醒。
         */
        public byte byPassWordResetLevel;

        /**
         * 能力集扩展，位与结果：0- 不支持，1- 支持 bySupportStreamEncrypt & 0x1:表示是否支持RTP/TLS取流 bySupportStreamEncrypt & 0x2: 表示是否支持SRTP/UDP取流 bySupportStreamEncrypt & 0x4: 表示是否支持SRTP/MULTICAST取流
         */
        public byte bySupportStreamEncrypt;

        /**
         * 0-无效（未知类型）,1-经销型，2-行业型
         */
        public byte byMarketType;

        /**
         * 保留，置为0
         */
        public byte[] byRes2 = new byte[238];
    }

    /**
     * 用户登录回调函数
     */
    interface FLoginResultCallBack extends Callback {
        /**
         * 回调方法，自定义业务代码
         *
         * @param lUserID      用户ID，NET_DVR_Login_V40的返回值
         * @param dwResult     登录状态：0- 异步登录失败，1- 异步登录成功
         * @param lpDeviceinfo 设备信息，设备序列号、通道、能力等参数
         * @param pUser        用户数据
         * @return
         */
        int invoke(int lUserID, int dwResult, NET_DVR_DEVICEINFO_V30 lpDeviceinfo, Pointer pUser);
    }

    /**
     * 设备参数结构体
     */
    class NET_DVR_DEVICEINFO_V30 extends Structure {
        /**
         * 序列号
         */
        public byte[] sSerialNumber = new byte[SERIALNO_LEN];

        /**
         * 模拟报警输入个数
         */
        public byte byAlarmInPortNum;

        /**
         * 模拟报警输出个数
         */
        public byte byAlarmOutPortNum;

        /**
         * 硬盘个数
         */
        public byte byDiskNum;

        /**
         * 设备类型, 1:DVR 2:ATM DVR 3:DVS ......
         */
        public byte byDVRType;

        /**
         * 设备模拟通道个数，数字（IP）通道最大个数为byIPChanNum + byHighDChanNum*256
         */
        public byte byChanNum;

        /**
         * 模拟通道的起始通道号，从1开始。数字通道的起始通道号见下面参数byStartDChan
         */
        public byte byStartChan;

        /**
         * 设备语音对讲通道数
         */
        public byte byAudioChanNum;

        /**
         * 设备最大数字通道个数，低8位，高8位见byHighDChanNum。可以根据IP通道个数来判断是否调用NET_DVR_GetDVRConfig
         */
        public byte byIPChanNum;

        /**
         * 零通道编码个数
         */
        public byte byZeroChanNum;

        /**
         * 主码流传输协议类型：0- private，1- rtsp，2- 同时支持私有协议和rtsp协议取流（默认采用私有协议取流）
         */
        public byte byMainProto;

        /**
         * 子码流传输协议类型：0- private，1- rtsp，2- 同时支持私有协议和rtsp协议取流（默认采用私有协议取流）
         */
        public byte bySubProto;

        /**
         * 能力，位与结果为0表示不支持，1表示支持 <br/>
         * bySupport & 0x1，表示是否支持智能搜索 <br/>
         * bySupport & 0x2，表示是否支持备份 <br/>
         * bySupport & 0x4，表示是否支持压缩参数能力获取 <br/>
         * bySupport & 0x8, 表示是否支持双网卡 <br/>
         * bySupport & 0x10, 表示支持远程SADP <br/>
         * bySupport & 0x20, 表示支持Raid卡功能 <br/>
         * bySupport & 0x40, 表示支持IPSAN目录查找 <br/>
         * bySupport & 0x80, 表示支持rtp over rtsp <br/>
         */
        public byte bySupport;

        /**
         * 能力集扩充，位与结果为0表示不支持，1表示支持 <br/>
         * bySupport1 & 0x1, 表示是否支持snmp v30 <br/>
         * bySupport1 & 0x2, 表示是否支持区分回放和下载 <br/>
         * bySupport1 & 0x4, 表示是否支持布防优先级 <br/>
         * bySupport1 & 0x8, 表示智能设备是否支持布防时间段扩展 <br/>
         * bySupport1 & 0x10,表示是否支持多磁盘数（超过33个） <br/>
         * bySupport1 & 0x20,表示是否支持rtsp over http <br/>
         * bySupport1 & 0x80,表示是否支持车牌新报警信息，且还表示是否支持NET_DVR_IPPARACFG_V40配置
         */
        public byte bySupport1;

        /**
         * 能力集扩充，位与结果为0表示不支持，1表示支持 <br/>
         * bySupport2 & 0x1, 表示解码器是否支持通过URL取流解码 <br/>
         * bySupport2 & 0x2, 表示是否支持FTPV40 <br/>
         * bySupport2 & 0x4, 表示是否支持ANR(断网录像) <br/>
         * bySupport2 & 0x20, 表示是否支持单独获取设备状态子项 <br/>
         * bySupport2 & 0x40, 表示是否是码流加密设备
         */
        public byte bySupport2;

        /**
         * 设备型号
         */
        public short wDevType;

        /**
         * 能力集扩展，位与结果：0- 不支持，1- 支持  <br/>
         * bySupport3 & 0x1, 表示是否支持多码流  <br/>
         * bySupport3 & 0x4, 表示是否支持按组配置，具体包含通道图像参数、报警输入参数、IP报警输入/输出接入参数、用户参数、设备工作状态、JPEG抓图、定时和时间抓图、硬盘盘组管理等  <br/>
         * bySupport3 & 0x20, 表示是否支持通过DDNS域名解析取流  <br/>
         */
        public byte bySupport3;

        /**
         * 是否支持多码流，按位表示，位与结果：0-不支持，1-支持  <br/>
         * byMultiStreamProto & 0x1, 表示是否支持码流3  <br/>
         * byMultiStreamProto & 0x2, 表示是否支持码流4  <br/>
         * byMultiStreamProto & 0x40,表示是否支持主码流  <br/>
         * byMultiStreamProto & 0x80,表示是否支持子码流
         */
        public byte byMultiStreamProto;

        /**
         * 起始数字通道号，0表示无数字通道，比如DVR或IPC
         */
        public byte byStartDChan;

        /**
         * 起始数字对讲通道号，区别于模拟对讲通道号，0表示无数字对讲通道
         */
        public byte byStartDTalkChan;

        /**
         * 数字通道个数，高8位
         */
        public byte byHighDChanNum;

        /**
         * 能力集扩展，按位表示，位与结果：0- 不支持，1- 支持 <br/>
         * bySupport4 & 0x01, 表示是否所有码流类型同时支持RTSP和私有协议 <br/>
         * bySupport4 & 0x10, 表示是否支持域名方式挂载网络硬盘
         */
        public byte bySupport4;

        /**
         * 支持语种能力，按位表示，位与结果：0- 不支持，1- 支持 <br/>
         * byLanguageType ==0，表示老设备，不支持该字段 <br/>
         * byLanguageType & 0x1，表示是否支持中文 <br/>
         * byLanguageType & 0x2，表示是否支持英文
         */
        public byte byLanguageType;

        /**
         * 音频输入通道数
         */
        public byte byVoiceInChanNum;

        /**
         * 音频输入起始通道号，0表示无效
         */
        public byte byStartVoiceInChanNo;

        /**
         * 保留，置为0
         */
        public byte byRes3;

        /**
         * 镜像通道个数，录播主机中用于表示导播通道
         */
        public byte byMirrorChanNum;

        /**
         * 起始镜像通道号
         */
        public byte wStartMirrorChanNo;

        /**
         * 保留，置为0
         */
        public byte byRes2;
    }

    /**
     * 实时预览（支持多码流）
     *
     * @param lUserID               NET_DVR_Login_V40等登录接口的返回值
     * @param lpPreviewInfo         预览参数
     * @param fRealDataCallBack_V30 码流数据回调函数
     * @param pUser                 用户数据
     * @return -1表示失败，其他值作为NET_DVR_StopRealPlay等函数的句柄参数。接口返回失败请调用NET_DVR_GetLastError获取错误码，通过错误码判断出错原因
     */
    int NET_DVR_RealPlay_V40(int lUserID, NET_DVR_PREVIEWINFO lpPreviewInfo, FRealDataCallBack_V30 fRealDataCallBack_V30, Pointer pUser);

    /**
     * 停止预览
     *
     * @param lRealHandle 预览句柄，NET_DVR_RealPlay或者NET_DVR_RealPlay_V30的返回值
     * @return TRUE表示成功，FALSE表示失败
     */
    boolean NET_DVR_StopRealPlay(int lRealHandle);

    /**
     * 预览参数结构体  <br/>
     * <br/>
     * dwStreamType(码流类型)、dwLinkMode(连接方式)、bPassbackRecord(录像回传)、byPreviewMode(延迟预览模式)、byStreamID(流ID)这些参数的取值需要设备支持。 <br/>
     * NET_DVR_RealPlay_V40支持多播方式预览（dwLinkMode设为2），不需要传多播组地址，底层自动从设备获取已配置的多播组地址（NET_DVR_NETCFG_V50中的参数struMulticastIpAddr）并以该多播组地址实现多播。  <br/>
     * 设备码流类型详细介绍可以参考“帮助”->“常见问题解答”中的Question 33。  <br/>
     * 当dwLinkMode == 7的时候，同时byProtoType == 1的时候，表示RTP over HTTPS预览。
     */
    class NET_DVR_PREVIEWINFO extends Structure {
        /**
         * 通道号，目前设备模拟通道号从1开始，数字通道的起始通道号通过NET_DVR_GetDVRConfig（配置命令NET_DVR_GET_IPPARACFG_V40）获取（dwStartDChan）
         */
        public int lChannel;

        /**
         * 码流类型：0-主码流，1-子码流，2-三码流，3-虚拟码流，以此类推
         */
        public int dwStreamType;

        /**
         * 连接方式：0- TCP方式，1- UDP方式，2- 多播方式，3- RTP方式，4- RTP/RTSP，5- RTP/HTTP，6- HRUDP（可靠传输） ，7- RTSP/HTTPS，8- NPQ
         */
        public int dwLinkMode;

        /**
         * 播放窗口的句柄，为NULL表示不解码显示。
         */
        public HWND hPlayWnd;

        /**
         * 0- 非阻塞取流，1- 阻塞取流 <br/>
         * 若设为不阻塞，表示发起与设备的连接就认为连接成功，如果发生码流接收失败、播放失败等情况以预览异常的方式通知上层。在循环播放的时候可以减短停顿的时间，与NET_DVR_RealPlay处理一致。 <br/>
         * 若设为阻塞，表示直到播放操作完成才返回成功与否，网络异常时SDK内部connect失败将会有5s的超时才能够返回，不适合于轮询取流操作。
         */
        public int bBlocked;

        /**
         * 是否启用录像回传：0-不启用录像回传，1-启用录像回传。ANR断网补录功能，客户端和设备之间网络异常恢复之后自动将前端数据同步过来，需要设备支持。
         */
        public int bPassbackRecord;

        /**
         * 延迟预览模式：0- 正常预览，1- 延迟预览
         */
        public byte byPreviewMode;

        /**
         * 流ID，为字母、数字和"_"的组合，lChannel为0xffffffff时启用此参数
         */
        public byte[] byStreamID = new byte[32];

        /**
         * 应用层取流协议：0- 私有协议，1- RTSP协议。主子码流支持的取流协议通过登录返回结构参数NET_DVR_DEVICEINFO_V30的byMainProto、bySubProto值得知。设备同时支持私协议和RTSP协议时，该参数才有效，默认使用私有协议，可选RTSP协议。
         */
        public byte byProtoType;

        /**
         * 保留，置为0
         */
        public byte byRes1;

        /**
         * 码流数据编解码类型：0- 通用编码数据，1- 热成像探测器产生的原始数据（温度数据的加密信息，通过去加密运算，将原始数据算出真实的温度值）
         */
        public byte byVideoCodingType;

        /**
         * 播放库播放缓冲区最大缓冲帧数，取值范围：1、6（默认，自适应播放模式）、15，置0时默认为1
         */
        public int dwDisplayBufNum;

        /**
         * NPQ模式：0- 直连模式，1-过流媒体模式
         */
        public byte byNPQMode;

        /**
         * 是否接收metadata数据，设备是否支持该功能通过GET /ISAPI/System/capabilities 中DeviceCap.SysCap.isSupportMetadata是否存在且为true
         */
        public byte byRecvMetaData;

        /**
         * 数据类型：0-码流数据，1-音频数据
         */
        public byte byDataType;

        /**
         * 保留，置为0
         */
        public byte[] byRes = new byte[213];
    }


    /**
     * 预览接口的回调
     */
    interface FRealDataCallBack_V30 extends Callback {
        /**
         * 回调方法，自定义业务代码
         *
         * @param lRealHandle 当前的预览句柄，NET_DVR_RealPlay_V40的返回值
         * @param dwDataType  数据类型
         * @param pBuffer     存放数据的缓冲区指针
         * @param dwBufSize   缓冲区大小
         * @param pUser       用户数据
         * @throws IOException 抛出异常
         */
        void invoke(int lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, Pointer pUser) throws IOException;
    }

    /**
     * 启用写日志文件
     *
     * @param nLogLevel 日志的等级（默认为0）：0-表示关闭日志，1-表示只输出ERROR错误日志，2-输出ERROR错误信息和DEBUG调试信息，3-输出ERROR错误信息、DEBUG调试信息和INFO普通信息等所有信息
     * @param strLogDir 日志文件的路径，windows默认值为"C:\\SdkLog\\"；linux默认值"/home/sdklog/"
     * @param bAutoDel  是否删除超出的文件数，默认值为TRUE
     * @return TRUE表示成功，FALSE表示失败
     */
    boolean NET_DVR_SetLogToFile(int nLogLevel, String strLogDir, boolean bAutoDel);

    /**
     * 设置SDK初始化参数
     *
     * @param enumType 初始化参数类型，不同的取值对应不同的参数
     * @param lpInBuff 输入参数，不同的参数类型
     * @return TRUE表示成功，FALSE表示失败
     */
    boolean NET_DVR_SetSDKInitCfg(int enumType, Pointer lpInBuff);

    /**
     * 组件库加载路径信息结构体
     */
    class NET_DVR_LOCAL_SDK_PATH extends Structure {
        /**
         * 组件库地址
         */
        public byte[] sPath = new byte[NET_SDK_MAX_FILE_PATH];

        /**
         * 保留
         */
        public byte[] byRes = new byte[128];
    }

    class BYTE_ARRAY extends Structure {
        public byte[] byValue;

        public BYTE_ARRAY(int iLen) {
            byValue = new byte[iLen];
        }
    }
}