package com.example.lxc.android_video.help;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SQLite数据库操作类
 * @author yqh
 */
public class DBHelper extends SQLiteOpenHelper {
	private final static String TAG = "DBHelper";
	private static final int VERSION = 1;
	private static final String DBNAME = "video_lxc";
	private static SQLiteDatabase db;
	private static DBHelper dbHelper;
	
	/**
	 * @param context 应用程序上下文
	 * @param dbName 数据库的名字
	 * @param factory 查询数据库的游标工厂 一般情况下 用sdk默认的
	 * @param version 数据库的版本 版本号必须不小1
	 */
	public DBHelper(Context context, String dbName, CursorFactory factory, int version) {
		super(context, dbName, factory, version);
	}
	
	public synchronized static DBHelper getInstance(Context context) {
        if (dbHelper == null) {
        	dbHelper = new DBHelper(context, DBNAME, null, VERSION);
        }
        return dbHelper;
	}
	
	public synchronized static void destoryInstance() {
		if (dbHelper != null) {
			dbHelper.close();
		}
	}
	
	public SQLiteDatabase getDatabase() {
		if (null == db) {
			db = this.getWritableDatabase();
        }
		return db;
	}
	
	public void close() {
		db.close();
	}

	/**
	 * 程序第一次安装时执行
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table if not exists t_setting ("
				+" set_name text primary key not null, set_value text " 
				+");");

		//历史学习表
		db.execSQL("create table if not exists t_study ("
				+" set_id integer PRIMARY KEY autoincrement, set_lesson_id text,set_lesson text, set_detail text,set_date text "
				+");");

		//下载信息表--state：1正在下载 ，2暂停 ，3等待下载 4下载完成
		/**db.execSQL("create table download_info(set_id integer primary key autoincrement," +
				"resID text, name text, url text, current text, total text,img_url text，state text)");**/

	}
	
	public void clearDB(SQLiteDatabase db) {
		db.execSQL("drop table if exists t_setting");
		db.execSQL("drop table if exists t_study");
		db.execSQL("drop table if exists download_info");
	}
	
	/**
	 * VERSION的版本号改变时执行
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.e(TAG, "SQLite oldVersion:" + oldVersion + " newVersion:" + newVersion);
		if(oldVersion != newVersion) {
			clearDB(db);
			onCreate(db);
			db.setVersion(newVersion);
		}
	}

	public synchronized String getString(String tableName, String column, String where, String[] values) {
		SQLiteDatabase db = this.getDatabase();
		
		if(db != null) {
			Cursor cursor = db.query(tableName, new String[] {column}, where, values, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				return cursor.getString(cursor.getColumnIndex(column));
			}
			cursor.close();
		}
		return "";
	}
	
	/**
	 * 像oracle一样，执行create , insert , update，delete操作
	 * @param
	 * @param sql 要执行的sql语句 inesrt into tableName(name,age) values('shen','25');
	 */
	public synchronized boolean execAddUpdDelSql(String sql, Object[] bindArgs) {
		SQLiteDatabase db = this.getDatabase();
		boolean b = false;
		if(db != null) {
			if(bindArgs!=null){
				db.execSQL(sql, bindArgs);
			}else{
				db.execSQL(sql);
			}
			b = true;
		}
		return b;
	}

	/**
	 * 插入数据
	 * @param
	 * @param tableName 表名
	 * Insert()方法用于添加数据，各个字段的数据使用ContentValues进行存放。
	 * ContentValues类似于MAP，相对于MAP，它提供了存取数据对应的put(String key, Xxx value)和getAsXxx(String key)方法， 
	 * key为字段名称，value为字段值，Xxx指的是各种常用的数据类型，如：String、Integer等
	 */
	public synchronized long insert(String tableName, ContentValues ColsAndVals) {
		SQLiteDatabase db = this.getDatabase();
		return db.insert(tableName, null, ColsAndVals); 
	}

	/**
	 * 删除数据
	 * @param
	 * @param tableName 表名
	 * @param where where条件，如 where id=？
	 * @param values，Stirng[]数组，用于填充id的占位符
	 */
	public synchronized int delete(String tableName, String where, String[] values) {
		SQLiteDatabase db = this.getDatabase();
		return db.delete(tableName, where, values);
	}

	/**
	 * 更新数据
	 * @param
	 * @param tableName 表名
	 * @param ColsAndVals 要更新的值
	 * @param where where条件，如 where id=？
	 * @param values，Stirng[]数组，用于填充id的占位符
	 */
	public synchronized int update(String tableName, ContentValues ColsAndVals, String where, String[] values) {
		SQLiteDatabase db = this.getDatabase();
		int result = db.update(tableName, ColsAndVals, where, values);
		return result;
	}

	/**
	 * 查询列表
	 * @param
	 * @param tableName String：表名   
	 * @param columns String[]:要查询的列名
	 * @param where String：查询条件
	 * @param values String[]：查询条件的参数
	 * @return
	 */
	public synchronized List<Map<String, String>> getList(String tableName, String[] columns, String where, String[] values) {
		return getList(tableName, columns, where, values, null, null, null);
	}
	
	/**
	 * 查询列表
	 * @param
	 * @param tableName String：表名   
	 * @param columns String[]:要查询的列名
	 * @param where String：查询条件
	 * @param values String[]：查询条件的参数
	 * @param groupSql String:对查询的结果进行分组 
	 * @param havingSql String：对分组的结果进行限制 
	 * @param orderSql String：对查询的结果进行排序
	 * @return
	 */
	public synchronized List<Map<String, String>> getList(String tableName, String[] columns, String where, String[] values, String groupSql, String havingSql, String orderSql) {
		SQLiteDatabase db = this.getDatabase();
		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		if(db != null) {
			Cursor cursor = db.query(tableName, columns, where,values,null,null,null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Map<String,String> map = new HashMap<String,String>();
				for(String columnName : columns){
					map.put(columnName.toUpperCase(Locale.getDefault()), cursor.getString(cursor.getColumnIndex(columnName)));
				}
				list.add(map);
				cursor.moveToNext();
			}
			// 查询结束要关闭
			cursor.close();
		}
		return list;
	}
}
