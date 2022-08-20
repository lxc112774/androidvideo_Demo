package com.example.lxc.android_video.entity;

/**
 * Created by lxc on 18-11-3.
 * 首页头部轮转信息
 */

public class HeadMessageBean {
    private int head_id;
    private String title;
    private String img_url;
    private String img_link_address;
    private int video_kind;

    public int getHead_id() {
        return head_id;
    }
    public void setHead_id(int head_id) {
        this.head_id = head_id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getImg_url() {
        return img_url;
    }
    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }
    public String getImg_link_address() {
        return img_link_address;
    }
    public void setImg_link_address(String img_link_address) {
        this.img_link_address = img_link_address;
    }
    public int getVideo_kind() {
        return video_kind;
    }
    public void setVideo_kind(int video_kind) {
        this.video_kind = video_kind;
    }


}
