package com.example.lxc.android_video.entity;

/**
 * Created by lxc on 18-12-29.
 * 视频播放信息
 */

public class VideoPlayData {

    private int data_id;
    private int video_name;
    private String video_describe;
    private String video_url;
    private String video_thumbnail_url;
    private int introduction_id;

    public int getData_id() {
        return data_id;
    }

    public void setData_id(int data_id) {
        this.data_id = data_id;
    }

    public int getVideo_name() {
        return video_name;
    }

    public void setVideo_name(int video_name) {
        this.video_name = video_name;
    }

    public String getVideo_describe() {
        return video_describe;
    }

    public void setVideo_describe(String video_describe) {
        this.video_describe = video_describe;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getVideo_thumbnail_url() {
        return video_thumbnail_url;
    }

    public void setVideo_thumbnail_url(String video_thumbnail_url) {
        this.video_thumbnail_url = video_thumbnail_url;
    }

    public int getIntroduction_id() {
        return introduction_id;
    }

    public void setIntroduction_id(int introduction_id) {
        this.introduction_id = introduction_id;
    }


    @Override
    public String toString() {
        return "VideoPlayData{" +
                "data_id=" + data_id +
                ", video_name=" + video_name +
                ", video_describe='" + video_describe + '\'' +
                ", video_url='" + video_url + '\'' +
                ", video_thumbnail_url='" + video_thumbnail_url + '\'' +
                ", introduction_id=" + introduction_id +
                '}';
    }
}
