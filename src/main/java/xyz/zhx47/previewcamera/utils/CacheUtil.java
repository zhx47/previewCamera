package xyz.zhx47.previewcamera.utils;

import xyz.zhx47.previewcamera.entity.YCamera;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 转码缓存工具类
 */
public final class CacheUtil {
	/*
	 * 保存已经开始推的流
	 */
	public static Map<String, YCamera> STREATMAP = new ConcurrentHashMap<>();

	/*
	 * 保存push
	 */
	public static Map<String, CameraPush> PUSHMAP = new ConcurrentHashMap<>();
}
