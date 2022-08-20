package com.example.lxc.android_video.base;

/**
 * Created by lxc on 18-10-30.
 */

/**
 * 测试数据
 */
public class HttpUrl {
    public static String SERVER_IP = "192.168.1.210";
    public static int SERVER_PORT = 8080;
    public static String SERVER_PATH = "http://" + SERVER_IP + ":" + SERVER_PORT + "/Video_Servers/";

    public static String LoginUrl = SERVER_PATH + "LoginServlet";
    public static String HomeUrl = SERVER_PATH + "VideoServlet";
    public static String PlayUrl = SERVER_PATH + "VideoPlayDataServlet";
    public static String UserSettingsUrl = SERVER_PATH + "UserSetting";
    public static String PlayComment = SERVER_PATH + "VideoPlayCommentServlet";
}
