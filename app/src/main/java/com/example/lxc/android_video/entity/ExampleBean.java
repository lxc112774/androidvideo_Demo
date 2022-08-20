package com.example.lxc.android_video.entity;

import java.util.List;

/**
 * Created by lxc on 18-11-3.
 * 首页所有信息
 */

public class ExampleBean {
    private int code;
    private String msg;
    private List<VideoBean> videodata;
    private List<HeadMessageBean> headImgdata;

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
    public List<VideoBean> getVideodata() {
        return videodata;
    }
    public void setVideodata(List<VideoBean> videodata) {
        this.videodata = videodata;
    }
    public List<HeadMessageBean> getHeadImgdata() {
        return headImgdata;
    }
    public void setHeadImgdata(List<HeadMessageBean> headImgdata) {
        this.headImgdata = headImgdata;
    }

}
