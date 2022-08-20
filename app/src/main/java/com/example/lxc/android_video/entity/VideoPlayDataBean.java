package com.example.lxc.android_video.entity;

import java.util.List;

/**
 * Created by lxc on 18-11-3.
 */

public class VideoPlayDataBean {
    private int code;
    private String msg;
    private List<VideoPlayData> data;


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

    public List<VideoPlayData> getData() {
        return data;
    }

    public void setData(List<VideoPlayData> data) {
        this.data = data;
    }
}
