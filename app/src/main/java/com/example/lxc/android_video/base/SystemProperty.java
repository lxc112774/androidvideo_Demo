package com.example.lxc.android_video.base;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.example.lxc.android_video.help.AppStore;
import com.example.lxc.android_video.help.StringHelper;
import com.example.lxc.android_video.main.LoginRegister.BaserBena;
import com.example.lxc.android_video.main.LoginRegister.UserBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * 系统的相关的参数
 * Created by yangqianhua on 16/5/10.
 */
public class SystemProperty {

    //单例
    private static SystemProperty systemProperty;
    //当前登录用户的信息
    private UserBean user;
    //手机型号
    private String mobileModel = "";
    //系统版本
    private String versionAndroid = "";
    //客户端版本
    private String versionName = "";
    //客户端版本号
    private String versionCode = "";

    /***********************getter和setter信息**************************/
    public String getMobileModel() {
        return mobileModel;
    }

    public void setMobileModel(String mobileModel) {
        this.mobileModel = mobileModel;
    }

    public String getVersionAndroid() {
        return versionAndroid;
    }

    public void setVersionAndroid(String versionAndroid) {
        this.versionAndroid = versionAndroid;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    /**
     * 创建单例
     * @param context
     * @return
     */
    public static SystemProperty getInstance(Context context) {
        try{
            if (systemProperty == null){
                systemProperty = new SystemProperty();
                systemProperty.init(context);
            }
        }catch(Exception e){
        }
        return systemProperty;
    }

    /**
     * 初始化
     * @param context
     * @throws Exception
     */
    public static void init(Context context) throws Exception {
        TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        PackageInfo info = context.getPackageManager().getPackageInfo("com.example.lxc.android_video", 0);
        systemProperty = new SystemProperty();
        //手机型号
        systemProperty.setMobileModel(Build.MODEL.replace(" " , "-"));
        //Android版本
        systemProperty.setVersionAndroid(Build.VERSION.RELEASE);
        //系统版本号
        systemProperty.setVersionCode(info.versionCode+"");
        //系统版本名称
        systemProperty.setVersionName(info.versionName+"");
        //清除用户信息
        systemProperty.setUserNull();
    }

    /**
     * 获取登陆用户信息
     * @param context
     * @return
     */
    public UserBean getUser(Context context){
        try{
            if (this.user==null){
                String loginRes = AppStore.getSettingValue(context,"login_result","");
                //结果转换
                Type resType = new TypeToken<BaserBena>(){}.getType();
                Gson gson = new Gson();
                BaserBena loginResult = gson.fromJson(loginRes, resType);
                //获取登陆用户信息
                this.user = loginResult.getData();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return this.user;
    }

    /**
     * 保存用户信息
     * @param user
     */
    public void saveUser(Context context, UserBean user){
        BaserBena result = new BaserBena();
        result.setCode(2);
        result.setData(user);
        //结果转换
        Type resType = new TypeToken<BaserBena>(){}.getType();
        Gson gson = new Gson();
        String resultStr= gson.toJson(result,resType);
        AppStore.putSettingValue(context,"login_result",resultStr);
        this.setUserNull();
    }

    /**
     * 将用户信息置为空
     */
    public void setUserNull(){
        this.user = null;
    }

    /**
     * 获取登陆的ID
     * @param context
     * @return
     */
    public static int getUserId(Context context){
        if(SystemProperty.getInstance(context).getUser(context)!=null){
            return SystemProperty.getInstance(context).getUser(context).getId();
        }else{
            return -1;
        }
    }

    /**
     * 获取用户ID转换成MD5的字符串
     * @param context
     * @return
     */
    public static String userIdMd5(Context context){
        return StringHelper.md5(String.valueOf(SystemProperty.getUserId(context)));
    }

    /**
     * 判断是否已经登录登录
     * @param context
     * @return
     */
    public static boolean isLogin(Context context) {
        String isLogin = AppStore.getSettingValue(context,"isLogin","0");
        if(isLogin.equals("1")) {
            return true;
        }else{
            return false;
        }
    }

    /**
     * 设置是否登录
     * @param context
     * @param isLogin
     */
    public static void setIsLogin(Context context, boolean isLogin){
        AppStore.putSettingValue(context,"isLogin",isLogin?"1":"0");   //1为已登录,0为未登录
    }

    /**
     * 判断是否已认证 4:未认证 1：已认证 2:等待审核 3:审核未通过
     * @param context
     * @return
     */
    /**public static int getAuthState(Context context){
        try{
            MoiUser user = SystemProperty.getInstance(context).getUser(context);
            return StringHelper.toInt(user.getStatus());
        }catch(Exception e){
            e.printStackTrace();
            return  0;
        }
    }**/
}
