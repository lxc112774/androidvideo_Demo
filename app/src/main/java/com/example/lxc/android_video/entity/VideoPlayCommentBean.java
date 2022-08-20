package com.example.lxc.android_video.entity;

import java.util.List;

/**
 * Created by lxc on 18-11-3.
 */

public class VideoPlayCommentBean {
    private int code;
    private String msg;
    private List<VideoPlayComment> data;


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

    public List<VideoPlayComment> getData() {
        return data;
    }

    public void setData(List<VideoPlayComment> data) {
        this.data = data;
    }
}
