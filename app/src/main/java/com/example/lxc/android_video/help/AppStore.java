package com.example.lxc.android_video.help;

import android.content.ContentValues;
import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局变量操作类，sqllite数据库读取
 * @author yqh
 *
 */
public class AppStore {
	private static Map<String, String> cache = new HashMap<String, String>();
	
	/**
	 * 数据库获取全局参数
	 * @param context
	 * @param key
	 * @param default_value
	 * @return
	 */
	public static synchronized String getSettingValue(Context context, String key, String default_value) {
		String value = cache.get(key);
		//System.out.println("cacheValue:"+value);
		if(value == null) { 
			DBHelper dbHelper = DBHelper.getInstance(context);
			value = dbHelper.getString("t_setting", "set_value", "set_name = ?", new String[] {key});
			//System.out.println("dbValue:"+value);
			if(value == null || "".equals(value))
				value = default_value;
			else
				cache.put(key, value);
		}
		return value;
	}


	/**
	 * 数据库存放全局参数
	 * @param context
	 * @param key
	 * @param value
	 * @return
	 */
	public static synchronized void putSettingValue(Context context, String key, String value) {
		DBHelper dbHelper = DBHelper.getInstance(context);
		cache.put(key, value);
		ContentValues cv = new ContentValues();
		cv.put("set_value", value);
		long cnt = dbHelper.update("t_setting", cv, "set_name = ?", new String[]{key});
		if(cnt == 0) {
			cv.put("set_name", key);
			cnt = dbHelper.insert("t_setting", cv);
		}
		//Log.e("===DB===", "putSettingValue('" + key + "','" + value + "') ===> " + cnt);
	}

}
