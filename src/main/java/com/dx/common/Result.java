package com.dx.common;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;


@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 失败 */
    public static final int FAIL = 500;

    /**
     * 成功标志
     */
    private boolean success = true;

    /**
     * 返回处理消息
     */
    private String message = "操作成功！";

    /**
     * 返回代码
     */
    private Integer code = 200;

    /**
     * 返回数据对象 data
     */
    private T result;

    /**
     * 时间戳
     */
    private long timestamp = System.currentTimeMillis();

    public Result() {

    }

    public Result(Result result) {
        this.success = result.success;
        this.code = result.code;
        this.message = result.message;
    }

    public Result(Integer code, String message) {
        this.success = false;
        this.code = code;
        this.message = message;
    }

    public Result<T> success(String message) {
        this.message = message;
        this.code = 0;
        this.success = true;
        return this;
    }


    public static Result<Object> ok() {
        Result<Object> r = new Result<Object>();
        r.setSuccess(true);
        r.setCode(0);
        r.setMessage("成功");
        return r;
    }

    public static Result<Object> ok(String msg) {
        Result<Object> r = new Result<Object>();
        r.setSuccess(true);
        r.setCode(0);
        r.setMessage(msg);
        return r;
    }

    public static Result<Object> ok(Object data) {
        Result<Object> r = new Result<Object>();
        r.setSuccess(true);
        r.setCode(0);
        r.setResult(data);
        return r;
    }

    public Result<T> error(String msg) {
        return error(Constant.SC_INTERNAL_SERVER_ERROR_500, msg);
    }

    public Result<T> error(int code, String msg) {
        this.message = msg;
        this.code = code;
        this.success = false;
        return this;
    }

    public Result<T> error500(String message) {
        this.message = message;
        this.code = Constant.SC_INTERNAL_SERVER_ERROR_500;
        this.success = false;
        return this;
    }

    public String toJSONNoResult(){
        return "{\"success\":" + success + ",\"code\":" + code + ",\"message\":\"" + message + "\"}";
    }
    /**
     * 无权限访问返回结果
     */
    public Result<T> noauth(String msg) {
        return error(HttpStatus.UNAUTHORIZED.value(), msg);
    }

    public Result<T> fillErr(ResultMsg msg) {
        this.success = false;
        this.code = msg.getCode();
        this.message = msg.getMessage();
        return this;
    }
}
