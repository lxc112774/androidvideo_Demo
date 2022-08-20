package com.example.lxc.android_video.help;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 插件工具和帮助类
 *
 *
 */
public class AssistUtil {

	private static StorageUtil storage;


	/**
	 * 判断SD卡是否存在
	 * @return
	 */
	public static boolean CheckSdCard() {
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			return true;
		}else{
			return false;
		}

	}

	/**
	 * 判断SD卡是否存在,通过反射，很精确
	 * @return
	 */
	public static boolean CheckSdCard2(Activity activity) {
		storage = new StorageUtil(activity);
		if(storage.isSDMounted()){
			return true;
		}else{
			return false;
		}

	}



	//SD剩余空间
	public static long getSDFreeSize(){
		//取得SD卡文件路径
		//File path = Environment.getExternalStorageDirectory();
		String path = storage.path2;
		StatFs sf = new StatFs(path);
		//获取单个数据块的大小(Byte)
		long blockSize = sf.getBlockSize();
		//空闲的数据块的数量
		long freeBlocks = sf.getAvailableBlocks();
		//返回SD卡空闲大小
		return freeBlocks * blockSize;  //单位Byte
		//return (freeBlocks * blockSize)/1024;   //单位KB
		//return (freeBlocks * blockSize)/1024 /1024; //单位MB
	}


	//SD总容量
	public static long getSDAllSize(){
		//取得SD卡文件路径
		String path = storage.path2;
		StatFs sf = new StatFs(path);
		//获取单个数据块的大小(Byte)
		long blockSize = sf.getBlockSize();
		//获取所有数据块数
		long allBlocks = sf.getBlockCount();
		//返回SD卡大小
		return allBlocks * blockSize; //单位Byte
		//return (allBlocks * blockSize)/1024; //单位KB
		//return (allBlocks * blockSize)/1024/1024; //单位MB
	}


	//SD文件路径
	public static String getSDpath(){
		//取得SD卡文件路径
		if(storage.path2 != null) {
			String path = storage.path2;
			return path;
		}
		return storage.path2;
	}


	//内部文件路径
	public static String getInternalpath(){
		//取得内部文件路径
		String path = storage.path1;
		return path;
	}




	/**
	 * 获取手机存储剩余空间
	 *
	 */
	public static long getAvailableInternalMemorySize(){
		//取得SD卡文件路径
		String path = storage.path1;
		StatFs sf = new StatFs(path);
		//获取单个数据块的大小(Byte)
		long blockSize = sf.getBlockSize();
		//空闲的数据块的数量
		long availableBlocks = sf.getAvailableBlocks();
		//返回SD卡空闲大小
		return availableBlocks * blockSize;  //单位Byte
		//return (freeBlocks * blockSize)/1024;   //单位KB
		//return (freeBlocks * blockSize)/1024 /1024; //单位MB
	}


	/**
	 * 获取手机内部总的存储空间
	 *
	 * @return
	 */
	public static long getTotalInternalMemorySize() {
		String path = storage.path1;
		StatFs sf = new StatFs(path);
		long blockSize = sf.getBlockSize();
		long totalBlocks = sf.getBlockCount();
		return totalBlocks * blockSize; //单位Byte
	}


	/**
	 *
	 * 获得当前手机的屏幕宽
	 *
	 * */
	public static int getScreenWidth(Context context) {
		// 获取当前屏幕
		DisplayMetrics dm =context.getResources().getDisplayMetrics();
		int w_screen = dm.widthPixels;
		return w_screen;
	}


	public static int getScreenHeight(Context context) {
		// 获取当前屏幕高
		DisplayMetrics dm =context.getResources().getDisplayMetrics();
		int h_screen = dm.heightPixels;
		return h_screen;
	}


	/**
	 * 获取底部导航栏虚拟键的高度
	 * @param activity
	 * @return
	 */
	public static int getNavigationBarHeight(Context activity) {
		if (!checkDeviceHasNavigationBar(activity)) {
			return 0;
		}
		Resources resources = activity.getResources();
		int resourceId = resources.getIdentifier("navigation_bar_height",
				"dimen", "android");
		//获取NavigationBar的高度
		int height = resources.getDimensionPixelSize(resourceId);
		return height;
	}


	/**
	 * 获取手机状态栏高度
	 * @param activity
	 * @return
	 */
	public static int getStateBarHeight(Context activity){
		int result = 0;
		int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = activity.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}


	/**
	 * 反射获取手机状态栏高度
	 * @param activity
	 * @return
	 */
	public static int getStateBarHeight2(Context activity) {
		int statusBarHeight = 0;
		Class c = null;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			Object obj = c.newInstance();
			Field field = c.getField("status_bar_height");
			int x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = activity.getResources().getDimensionPixelSize(x);
			return statusBarHeight;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusBarHeight;
	}

	/**
	 * 检测是否有虚拟键
	 * @param context
	 * @return
	 */
	public static boolean checkDeviceHasNavigationBar(Context context) {
		boolean hasNavigationBar = false;
		Resources rs = context.getResources();
		int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
		if (id > 0) {
			hasNavigationBar = rs.getBoolean(id);
		}
		try {
			Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
			Method m = systemPropertiesClass.getMethod("get", String.class);
			String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
			if ("1".equals(navBarOverride)) {
				hasNavigationBar = false;
			} else if ("0".equals(navBarOverride)) {
				hasNavigationBar = true;
			}
		} catch (Exception e) {

		}
		return hasNavigationBar;
	}

	/**
	 * 隐藏虚拟按键，并且全屏
	 */
	public static void hideBottomUIMenu(Activity activity){
		//隐藏虚拟按键，并且全屏
		if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
			View v = activity.getWindow().getDecorView();
			v.setSystemUiVisibility(View.GONE);
		} else if (Build.VERSION.SDK_INT >= 19) {
			//for new api versions.
			View decorView = activity.getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//          | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
					| View.SYSTEM_UI_FLAG_IMMERSIVE;
			decorView.setSystemUiVisibility(uiOptions);
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
	}


	/*
	 *设置状态栏颜色
	 */
	public static void setStatusBarColor(Activity activity, int statusColor) {
		Window window = activity.getWindow();
		//取消状态栏透明
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		//添加Flag把状态栏设为可绘制模式
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		//设置状态栏颜色
		window.setStatusBarColor(statusColor);
		//设置系统状态栏处于可见状态
		window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		//让view不根据系统窗口来调整自己的布局
		ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
		View mChildView = mContentView.getChildAt(0);
		if (mChildView != null) {
			ViewCompat.setFitsSystemWindows(mChildView, false);
			ViewCompat.requestApplyInsets(mChildView);
		}
	}


}
