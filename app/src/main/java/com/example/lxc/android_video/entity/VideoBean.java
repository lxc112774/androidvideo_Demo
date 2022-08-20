package com.example.lxc.android_video.entity;

/**
 * Created by lxc on 18-11-3.
 * 首页视频显示信息
 */

public class VideoBean {
    private int introduction_id;
    private String video_name;
    private String video_showupdate;
    private String video_img;
    private int video_playnumber;
    private int video_commentnumber;
    private String video_introduction;
    private int video_kind;
    private int is_recommend;

    public int getIntroduction_id() {
        return introduction_id;
    }

    public void setIntroduction_id(int introduction_id) {
        this.introduction_id = introduction_id;
    }

    public String getVideo_name() {
        return video_name;
    }

    public void setVideo_name(String video_name) {
        this.video_name = video_name;
    }

    public String getVideo_showupdate() {
        return video_showupdate;
    }

    public void setVideo_showupdate(String video_showupdate) {
        this.video_showupdate = video_showupdate;
    }

    public String getVideo_img() {
        return video_img;
    }

    public void setVideo_img(String video_img) {
        this.video_img = video_img;
    }

    public int getVideo_playnumber() {
        return video_playnumber;
    }

    public void setVideo_playnumber(int video_playnumber) {
        this.video_playnumber = video_playnumber;
    }

    public int getVideo_commentnumber() {
        return video_commentnumber;
    }

    public void setVideo_commentnumber(int video_commentnumber) {
        this.video_commentnumber = video_commentnumber;
    }

    public String getVideo_introduction() {
        return video_introduction;
    }

    public void setVideo_introduction(String video_introduction) {
        this.video_introduction = video_introduction;
    }

    public int getVideo_kind() {
        return video_kind;
    }

    public void setVideo_kind(int video_kind) {
        this.video_kind = video_kind;
    }

    public int getIs_recommend() {
        return is_recommend;
    }

    public void setIs_recommend(int is_recommend) {
        this.is_recommend = is_recommend;
    }

    @Override
    public String toString() {
        return "VideoBean{" +
                "introduction_id=" + introduction_id +
                ", video_name='" + video_name + '\'' +
                ", video_showupdate='" + video_showupdate + '\'' +
                ", video_img='" + video_img + '\'' +
                ", video_playnumber=" + video_playnumber +
                ", video_commentnumber=" + video_commentnumber +
                ", video_introduction='" + video_introduction + '\'' +
                ", video_kind=" + video_kind +
                ", is_recommend=" + is_recommend +
                '}';
    }
}
