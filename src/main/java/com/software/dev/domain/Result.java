package com.software.dev.domain;


import java.util.HashMap;
import java.util.Map;

public class Result extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    public Result() {
        put("code", 0);
    }

    public static Result error() {
        return error(500, "未知异常，请联系管理员");
    }

    public static Result error(String msg) {
        return error(500, msg);
    }

    public static Result error(int code, String msg) {
        Result r = new Result();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    public static Result page(Object msg,Integer pageNo,Integer pageSize,Integer total) {
        Result r = new Result();
        r.put("msg", msg);
        r.put("pageNo", pageNo);
        r.put("pageSize", pageSize);
        r.put("total", total);
        return r;
    }
//    public static Result page(IPage page) {
//        Result r = new Result();
//        r.put("msg", page.getRecords());
//        r.put("pageNo", page.getCurrent());
//        r.put("pageSize", page.getSize());
//        r.put("total", page.getTotal());
//        return r;
//    }
    public static Result ok(String msg) {
        Result r = new Result();
        r.put("msg", msg);
        return r;
    }
    public static Result ok(Object data) {
        Result r = new Result();
        r.put("data", data);
        return r;
    }

    public static Result ok(Map<String, Object> map) {
        Result r = new Result();
        r.putAll(map);
        return r;
    }

    public static Result ok() {
        return new Result();
    }

    @Override
    public Result put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
