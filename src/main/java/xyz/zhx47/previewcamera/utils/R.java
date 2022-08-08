package xyz.zhx47.previewcamera.utils;

import xyz.zhx47.previewcamera.common.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 响应信息主体
 *
 * @author zhx47
 */
public class R extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public R() {
        put("code", HttpStatus.SUCCESS);
        put("body", new HashMap<>());
        put("desc", "");
    }

    public static R ok() {
        return new R();
    }

    public static R ok(String desc) {
        R r = new R();
        r.put("desc", desc);
        return r;
    }

    public static R ok(Integer code, String desc) {
        R r = new R();
        r.put("code", code);
        r.put("desc", desc);
        return r;
    }

    public static R error(String desc) {
        return error(HttpStatus.ERROR, desc);
    }

    public static R error(Integer code, String desc) {
        R r = new R();
        r.put("code", code);
        r.put("desc", desc);
        return r;
    }

    @Override
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public R putBodyByMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        super.put("body", map);
        return this;
    }

    public R putBodyByObject(Object value) {
        super.put("body", value);
        return this;
    }
}