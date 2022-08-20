package com.example.lxc.android_video.main.LoginRegister;

/**
 * Created by lxc on 18-10-31.
 */

public class BaserBena {

    private int code; //0用户不存在，1密码不对，2登录成功
    private String msg;
    private UserBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public UserBean getData() {
        return data;
    }

    public void setData(UserBean data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BaserBena{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
