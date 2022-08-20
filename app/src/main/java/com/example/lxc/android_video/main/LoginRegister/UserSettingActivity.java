package com.example.lxc.android_video.main.LoginRegister;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lxc.android_video.R;
import com.example.lxc.android_video.base.ActivityCollector;
import com.example.lxc.android_video.base.BaseActivity;
import com.example.lxc.android_video.base.HttpUrl;
import com.example.lxc.android_video.base.SystemProperty;
import com.example.lxc.android_video.help.AppStore;
import com.example.lxc.android_video.help.DownloadImageTask;
import com.example.lxc.android_video.help.StringHelper;
import com.example.lxc.android_video.help.download_img.LocalCacheUtils;
import com.example.lxc.android_video.main.MainActivity;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.alibaba.fastjson.util.IOUtils.decodeBase64;

/**
 * Created by lxc on 19-6-6.
 */

public class UserSettingActivity extends BaseActivity implements View.OnClickListener{

    private ImageView head_img;

    private RelativeLayout header, nike_name;

    private TextView nike_text;

    private Button qute_btn;

    /* 头像文件名 */
    private static final String IMAGE_FILE_NAME = "origin_head_img.jpg";
    private static final String HEAD_IMAGE_NAME = "cut_head_img.jpg";

    /* 请求识别码 */
    private static final int CODE_GALLERY_REQUEST = 0;
    private static final int CODE_CAMERA_REQUEST = 1;
    private static final int CODE_RESULT_REQUEST = 2;

    // 裁剪后图片的宽(X)和高(Y),100 X 100的正方形。
    private static int output_X = 250;
    private static int output_Y = 250;

    //选择识别码(相册,相机)
    private int SELECT_PICTURE = 0;
    private int SELECT_CAMERA = 1;

    //头像Uri
    private Uri img_uri;

    //头像path
    private String img_src;

    private int user_id;

    private int isLogin =1 ; //默认登录,0未登录

    Bitmap photo ;//默认的像素图

    private String user_name,phone_number,user_head_img;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usersettings);

        initview();

    }

    private void initview(){
        head_img = (ImageView) findViewById(R.id.hd_img);

        header = (RelativeLayout) findViewById(R.id.header);

        nike_name = (RelativeLayout) findViewById(R.id.nike_name);

        nike_text = (TextView) findViewById(R.id.nike_text);

        qute_btn = (Button) findViewById(R.id.quit_btn);

        getUser();

        header.setOnClickListener(this);
        nike_name.setOnClickListener(this);
        head_img.setOnClickListener(this);
        qute_btn.setOnClickListener(this);
    }

    //从数据库读图片
    public void getUser(){

        UserBean userdatas= SystemProperty.getInstance(this).getUser(UserSettingActivity.this);
        user_id = userdatas.getId();
        user_name = userdatas.getUsername();
        phone_number = userdatas.getPhonenumber();
        user_head_img = userdatas.getUserimg();
        nike_text.setText(user_name);

        if(user_head_img != null){
            // Base64解码图片
            byte[] imageByteArray = Base64.decode(user_head_img , 0);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            if(bitmap !=null){
                photo = bitmap;
                head_img.setImageBitmap(bitmap);
            }
        }else {
            photo = BitmapFactory.decodeResource(getResources(), R.drawable.lxc);
            head_img.setImageResource(R.drawable.lxc);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.header:
                //更换头像
                setHeader();
                break;
            case R.id.nike_name:
                setName();
                break;
            case R.id.hd_img:

                break;
            case R.id.quit_btn:
                quit();
            default:
                break;
        }
    }


    private void setName(){
        final EditText et = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("修改昵称")
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //按下确定键后的事件
                        user_name = et.getText().toString();
                        if(user_name.isEmpty()){
                            showShortMessage(UserSettingActivity.this,"昵称不能为空");
                        }else {
                            //开始上传
                            SettingsRequest(user_head_img, user_name, phone_number,isLogin);
                        }
                    }
                }).setNegativeButton("取消",null).show();
    }


    private void setHeader(){
        CharSequence[] items = {"相册", "相机"};
        new AlertDialog.Builder(this)
                .setTitle("选择图片来源")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == SELECT_PICTURE) {
                            choseHeadImageFromGallery();//相册
                        } else {
                            choseHeadImageFromCameraCapture();//相机
                        }
                    }
                }).create().show();
    }


    // 从本地相册选取图片作为头像
    private void choseHeadImageFromGallery() {
        Intent intentFromGallery = new Intent(Intent.ACTION_PICK, null);
        // 设置文件类型
        intentFromGallery.setType("image/*");
        startActivityForResult(intentFromGallery, CODE_GALLERY_REQUEST);//回调识别
    }


    // 启动手机相机拍摄照片作为头像
    private void choseHeadImageFromCameraCapture() {
        Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File img_file = new File(LocalCacheUtils.CACHE_PATH, phone_number+"_"+IMAGE_FILE_NAME);
        //通过得到文件的父文件，判断父文件是否存在
        File parentFile = img_file.getParentFile();
        if (!parentFile.exists()){
            parentFile.mkdirs();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  //Android7.0以上
            img_uri = FileProvider.getUriForFile(this, "com.example.lxc.android_video.opencamera", img_file);//通过FileProvider创建一个content类型的Uri
            intentFromCapture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
        } else { //Android7.0以下
            img_uri = Uri.fromFile(img_file);
        }

        intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, img_uri);//设置照相返回图片保存路径
        startActivityForResult(intentFromCapture, CODE_CAMERA_REQUEST);

    }


    //回调图片
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case CODE_GALLERY_REQUEST://相册图片
                if(resultCode == RESULT_OK && intent != null)
                    cropRawPhoto(intent.getData());//裁剪原始图片方法

                break;

            case CODE_CAMERA_REQUEST://相机图片
                if (resultCode == RESULT_OK)
                    cropRawPhoto(img_uri);//裁剪原始图片方法

                break;

            case CODE_RESULT_REQUEST:
                if (intent != null) {
                    setImageToHeadView(intent);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }


    //裁剪原始的图片
    public void cropRawPhoto(Uri uri) {
        try {
            if (uri == null) {
                Log.i("tag", "The head img uri is not exist.");
            }

            File CropPhoto = new File(LocalCacheUtils.CACHE_PATH, phone_number+"_"+HEAD_IMAGE_NAME);//这个是创建一个截取后的图片路径和名称,一定要创建，不然相册的剪切没地方存储
            try {
                if (CropPhoto.exists()) {
                    CropPhoto.delete();
                }
                CropPhoto.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            img_uri = Uri.fromFile(CropPhoto);

            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(uri, "image/*");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            // 设置裁剪
            intent.putExtra("crop", "true");
            // aspectX , aspectY :宽高的比例
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            // outputX , outputY : 裁剪图片宽高
            intent.putExtra("outputX", output_X);
            intent.putExtra("outputY", output_Y);
            //取消人脸识别
            intent.putExtra("noFaceDetection", true);
            intent.putExtra("return-data", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, img_uri);//一定输出
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());//缩略图
            startActivityForResult(intent, CODE_RESULT_REQUEST);
        }catch (ActivityNotFoundException ex){
            showShortMessage(getApplicationContext(),"你的设备不支持裁剪行为~~~");
        }

    }


    //提取保存裁剪之后的图片数据，并设置头像部分的View
    private void setImageToHeadView(Intent intent) {

        Uri extras = intent.getData(); //Bundle extras = intent.getExtras();
        if (extras != null) {
            //photo = extras.getParcelable("data");

            img_src = extras.getPath();//这是本机的图片路径

            try {
                photo=BitmapFactory.decodeStream(this.getContentResolver().openInputStream(img_uri));


                /**String[] proj = {MediaStore.Images.Media.DATA};
                 CursorLoader loader = new CursorLoader(this.getApplication(), img_uri, proj, null, null, null);
                 Cursor cursor = loader.loadInBackground();
                 android.util.Log.e("lxc","cursor=="+cursor);
                 if (cursor != null) {
                 int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                 cursor.moveToFirst();

                 img_src = cursor.getString(column_index);//图片实际路径
                 cursor.close();
                 }**/


                // os 是定义的字节输出流
                //compress() 方法是将Bitmap压缩成指定格式和质量的输出流
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG,100,os);
                byte[] byte_arr = os.toByteArray();
                // Base64图片转码为String
                user_head_img = Base64.encodeToString(byte_arr, 0);

                Log.e("lxc","user_head_img=="+user_head_img);
                //开始上传图片
                SettingsRequest(user_head_img,user_name,phone_number,isLogin);

            } catch (FileNotFoundException e) {
                Log.e("Exception", e.getMessage(), e);
            }

        }
    }


    public void SettingsRequest(final String user_head_img, final String user_name,final String phone_number,final int isLogin) {
        //请求地址
        String url = HttpUrl.UserSettingsUrl;    //注①
        String tag = "UserSettings";    //注②

        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(UserSettingActivity.this);

        //防止重复请求，所以先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest，定义字符串请求的请求方式为POST(省略第一个参数会默认为GET方式)
        final StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //showShortMessage(getApplicationContext(),"sss"+response);
                            Log.e("lxc","sss=="+response);
                            //结果转换
                            Gson gson = new Gson();
                            BaserBena Result = gson.fromJson(response,BaserBena.class);
                            int result = Result.getCode();
                            Log.e("lxc","result=="+result);
                            UserBean user = Result.getData();
                            if (result == 0) {  //注⑤
                                //保存成功信息
                                UserBean userdatas= SystemProperty.getInstance(UserSettingActivity.this).getUser(UserSettingActivity.this);
                                userdatas.setUserimg(user.getUserimg());
                                userdatas.setPhonenumber(user.getPhonenumber());
                                userdatas.setUsername(user.getUsername());
                                SystemProperty.getInstance(UserSettingActivity.this).saveUser(UserSettingActivity.this,userdatas);

                                /* 将Bitmap设定到ImageView */
                                head_img.setImageBitmap(photo);
                                nike_text.setText(user.getUsername());

                                showShortMessage(UserSettingActivity.this,"修改成功");
                            } else {

                            }
                        } catch (Exception e) {
                            //做自己的请求异常操作，如Toast提示（“无网络连接”等）
                            showShortMessage(UserSettingActivity.this,"无网络连接");
                            Log.e("TAG", e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //做自己的响应错误操作，如Toast提示（“请稍后重试”等）
                showShortMessage(UserSettingActivity.this,"服务器无响应，请稍后重试");
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("phone_number", phone_number);  //注⑥
                params.put("user_name", user_name);
                params.put("user_head_img", user_head_img);
                params.put("isLogin", isLogin+"");
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }


    //返回退出
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            //将获取的值回传回去
            Intent intent = new Intent();
            intent.putExtra("user_id", user_id);
            intent.putExtra("user_name", user_name);
            intent.putExtra("user_img", user_head_img);
            //通过intent对象返回结果，必须要调用一个setResult方法，
            //setResult(resultCode, data);第一个参数表示结果返回码，一般只要大于1就可以
            setResult(2, intent);
            finish();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }


    private void quit(){

        new AlertDialog.Builder(this)
                .setTitle("是否确认退出?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //按下确定键后的事件
                        isLogin = 0;
                        //退出
                        SettingsRequest(user_head_img,user_name,phone_number,isLogin);

                        //清除数据库
                        //清除登陆成功信息
                        AppStore.putSettingValue(UserSettingActivity.this,"login_result","");
                        //清除用户ID
                        AppStore.putSettingValue(UserSettingActivity.this,"user_id", "");
                        //清除登陆手机号和MD5加密后的密码
                        AppStore.putSettingValue(UserSettingActivity.this,"phone","");
                        AppStore.putSettingValue(UserSettingActivity.this,"pwd", "");

                        //清除USER信息
                        SystemProperty.getInstance(UserSettingActivity.this).setUserNull();
                        //设置未登陆
                        SystemProperty.setIsLogin(UserSettingActivity.this,false);

                        Intent intent = new Intent();
                        intent.putExtra("user_id", "");
                        intent.putExtra("user_name", "");
                        intent.putExtra("user_img", "");
                        setResult(2, intent);
                        finish();
                    }
                }).setNegativeButton("取消",null).show();

    }

}
