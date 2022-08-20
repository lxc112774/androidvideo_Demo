package com.example.lxc.android_video.help;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lxc on 18-10-30.
 */

public class StringHelper {

    /**
     * 判断输入的手机号码是否合法
     * @param mobiles
     * @return
     */
    public static boolean isPhoneNumber(String mobiles) {
        String telRegex = "[1][4358]\\d{9}";
        if (TextUtils.isEmpty(mobiles)) {
            return false;
        } else{
            return mobiles.matches(telRegex);
        }
    }


    /**
     * 判断邮箱是否合法
     * @param email
     * @return
     */
    public static boolean isEmail(String email){
        if (null==email || "".equals(email)) return false;
        //Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p =  Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }


    /**
     * md5加密
     * @param source
     * @return
     */
    public static String md5(String source) {

        StringBuffer sb = new StringBuffer(32);

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(source.getBytes("UTF-8"));

            for (int i = 0; i < array.length; i++) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
        } catch (Exception e) {
            return "";
        }
        return sb.toString();
    }


    /**
     * 吐司显示提示信息
     * @param msg
     */
    public static void showShortMessage(Context context, String msg){
        Toast toast = null;
        if (toast==null){
            toast = Toast.makeText(context,msg,Toast.LENGTH_SHORT);
        }else{
            toast.setText(msg);
        }
        toast.show();
    }


    /**
     * 获取格式化当前手机时间
     * @return
     */
    public static String getCurFormatTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        return sdf.format(new Date(System.currentTimeMillis()));
    }


}
