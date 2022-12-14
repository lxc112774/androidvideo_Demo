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

    /* ??????????????? */
    private static final String IMAGE_FILE_NAME = "origin_head_img.jpg";
    private static final String HEAD_IMAGE_NAME = "cut_head_img.jpg";

    /* ??????????????? */
    private static final int CODE_GALLERY_REQUEST = 0;
    private static final int CODE_CAMERA_REQUEST = 1;
    private static final int CODE_RESULT_REQUEST = 2;

    // ?????????????????????(X)??????(Y),100 X 100???????????????
    private static int output_X = 250;
    private static int output_Y = 250;

    //???????????????(??????,??????)
    private int SELECT_PICTURE = 0;
    private int SELECT_CAMERA = 1;

    //??????Uri
    private Uri img_uri;

    //??????path
    private String img_src;

    private int user_id;

    private int isLogin =1 ; //????????????,0?????????

    Bitmap photo ;//??????????????????

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

    //?????????????????????
    public void getUser(){

        UserBean userdatas= SystemProperty.getInstance(this).getUser(UserSettingActivity.this);
        user_id = userdatas.getId();
        user_name = userdatas.getUsername();
        phone_number = userdatas.getPhonenumber();
        user_head_img = userdatas.getUserimg();
        nike_text.setText(user_name);

        if(user_head_img != null){
            // Base64????????????
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
                //????????????
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
                .setTitle("????????????")
                .setView(et)
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //???????????????????????????
                        user_name = et.getText().toString();
                        if(user_name.isEmpty()){
                            showShortMessage(UserSettingActivity.this,"??????????????????");
                        }else {
                            //????????????
                            SettingsRequest(user_head_img, user_name, phone_number,isLogin);
                        }
                    }
                }).setNegativeButton("??????",null).show();
    }


    private void setHeader(){
        CharSequence[] items = {"??????", "??????"};
        new AlertDialog.Builder(this)
                .setTitle("??????????????????")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == SELECT_PICTURE) {
                            choseHeadImageFromGallery();//??????
                        } else {
                            choseHeadImageFromCameraCapture();//??????
                        }
                    }
                }).create().show();
    }


    // ???????????????????????????????????????
    private void choseHeadImageFromGallery() {
        Intent intentFromGallery = new Intent(Intent.ACTION_PICK, null);
        // ??????????????????
        intentFromGallery.setType("image/*");
        startActivityForResult(intentFromGallery, CODE_GALLERY_REQUEST);//????????????
    }


    // ??????????????????????????????????????????
    private void choseHeadImageFromCameraCapture() {
        Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File img_file = new File(LocalCacheUtils.CACHE_PATH, phone_number+"_"+IMAGE_FILE_NAME);
        //????????????????????????????????????????????????????????????
        File parentFile = img_file.getParentFile();
        if (!parentFile.exists()){
            parentFile.mkdirs();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  //Android7.0??????
            img_uri = FileProvider.getUriForFile(this, "com.example.lxc.android_video.opencamera", img_file);//??????FileProvider????????????content?????????Uri
            intentFromCapture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //???????????????????????????????????????????????????Uri??????????????????
        } else { //Android7.0??????
            img_uri = Uri.fromFile(img_file);
        }

        intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, img_uri);//????????????????????????????????????
        startActivityForResult(intentFromCapture, CODE_CAMERA_REQUEST);

    }


    //????????????
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case CODE_GALLERY_REQUEST://????????????
                if(resultCode == RESULT_OK && intent != null)
                    cropRawPhoto(intent.getData());//????????????????????????

                break;

            case CODE_CAMERA_REQUEST://????????????
                if (resultCode == RESULT_OK)
                    cropRawPhoto(img_uri);//????????????????????????

                break;

            case CODE_RESULT_REQUEST:
                if (intent != null) {
                    setImageToHeadView(intent);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }


    //?????????????????????
    public void cropRawPhoto(Uri uri) {
        try {
            if (uri == null) {
                Log.i("tag", "The head img uri is not exist.");
            }

            File CropPhoto = new File(LocalCacheUtils.CACHE_PATH, phone_number+"_"+HEAD_IMAGE_NAME);//??????????????????????????????????????????????????????,??????????????????????????????????????????????????????
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
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //???????????????????????????????????????????????????Uri??????????????????
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            // ????????????
            intent.putExtra("crop", "true");
            // aspectX , aspectY :???????????????
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            // outputX , outputY : ??????????????????
            intent.putExtra("outputX", output_X);
            intent.putExtra("outputY", output_Y);
            //??????????????????
            intent.putExtra("noFaceDetection", true);
            intent.putExtra("return-data", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, img_uri);//????????????
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());//?????????
            startActivityForResult(intent, CODE_RESULT_REQUEST);
        }catch (ActivityNotFoundException ex){
            showShortMessage(getApplicationContext(),"?????????????????????????????????~~~");
        }

    }


    //??????????????????????????????????????????????????????????????????View
    private void setImageToHeadView(Intent intent) {

        Uri extras = intent.getData(); //Bundle extras = intent.getExtras();
        if (extras != null) {
            //photo = extras.getParcelable("data");

            img_src = extras.getPath();//???????????????????????????

            try {
                photo=BitmapFactory.decodeStream(this.getContentResolver().openInputStream(img_uri));


                /**String[] proj = {MediaStore.Images.Media.DATA};
                 CursorLoader loader = new CursorLoader(this.getApplication(), img_uri, proj, null, null, null);
                 Cursor cursor = loader.loadInBackground();
                 android.util.Log.e("lxc","cursor=="+cursor);
                 if (cursor != null) {
                 int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                 cursor.moveToFirst();

                 img_src = cursor.getString(column_index);//??????????????????
                 cursor.close();
                 }**/


                // os ???????????????????????????
                //compress() ????????????Bitmap??????????????????????????????????????????
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG,100,os);
                byte[] byte_arr = os.toByteArray();
                // Base64???????????????String
                user_head_img = Base64.encodeToString(byte_arr, 0);

                Log.e("lxc","user_head_img=="+user_head_img);
                //??????????????????
                SettingsRequest(user_head_img,user_name,phone_number,isLogin);

            } catch (FileNotFoundException e) {
                Log.e("Exception", e.getMessage(), e);
            }

        }
    }


    public void SettingsRequest(final String user_head_img, final String user_name,final String phone_number,final int isLogin) {
        //????????????
        String url = HttpUrl.UserSettingsUrl;    //??????
        String tag = "UserSettings";    //??????

        //??????????????????
        RequestQueue requestQueue = Volley.newRequestQueue(UserSettingActivity.this);

        //????????????????????????????????????tag?????????????????????
        requestQueue.cancelAll(tag);

        //??????StringRequest??????????????????????????????????????????POST(?????????????????????????????????GET??????)
        final StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //showShortMessage(getApplicationContext(),"sss"+response);
                            Log.e("lxc","sss=="+response);
                            //????????????
                            Gson gson = new Gson();
                            BaserBena Result = gson.fromJson(response,BaserBena.class);
                            int result = Result.getCode();
                            Log.e("lxc","result=="+result);
                            UserBean user = Result.getData();
                            if (result == 0) {  //??????
                                //??????????????????
                                UserBean userdatas= SystemProperty.getInstance(UserSettingActivity.this).getUser(UserSettingActivity.this);
                                userdatas.setUserimg(user.getUserimg());
                                userdatas.setPhonenumber(user.getPhonenumber());
                                userdatas.setUsername(user.getUsername());
                                SystemProperty.getInstance(UserSettingActivity.this).saveUser(UserSettingActivity.this,userdatas);

                                /* ???Bitmap?????????ImageView */
                                head_img.setImageBitmap(photo);
                                nike_text.setText(user.getUsername());

                                showShortMessage(UserSettingActivity.this,"????????????");
                            } else {

                            }
                        } catch (Exception e) {
                            //????????????????????????????????????Toast????????????????????????????????????
                            showShortMessage(UserSettingActivity.this,"???????????????");
                            Log.e("TAG", e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //????????????????????????????????????Toast????????????????????????????????????
                showShortMessage(UserSettingActivity.this,"????????????????????????????????????");
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("phone_number", phone_number);  //??????
                params.put("user_name", user_name);
                params.put("user_head_img", user_head_img);
                params.put("isLogin", isLogin+"");
                return params;
            }
        };

        //??????Tag??????
        request.setTag(tag);

        //???????????????????????????
        requestQueue.add(request);
    }


    //????????????
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            //???????????????????????????
            Intent intent = new Intent();
            intent.putExtra("user_id", user_id);
            intent.putExtra("user_name", user_name);
            intent.putExtra("user_img", user_head_img);
            //??????intent??????????????????????????????????????????setResult?????????
            //setResult(resultCode, data);?????????????????????????????????????????????????????????1?????????
            setResult(2, intent);
            finish();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }


    private void quit(){

        new AlertDialog.Builder(this)
                .setTitle("???????????????????")
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //???????????????????????????
                        isLogin = 0;
                        //??????
                        SettingsRequest(user_head_img,user_name,phone_number,isLogin);

                        //???????????????
                        //????????????????????????
                        AppStore.putSettingValue(UserSettingActivity.this,"login_result","");
                        //????????????ID
                        AppStore.putSettingValue(UserSettingActivity.this,"user_id", "");
                        //????????????????????????MD5??????????????????
                        AppStore.putSettingValue(UserSettingActivity.this,"phone","");
                        AppStore.putSettingValue(UserSettingActivity.this,"pwd", "");

                        //??????USER??????
                        SystemProperty.getInstance(UserSettingActivity.this).setUserNull();
                        //???????????????
                        SystemProperty.setIsLogin(UserSettingActivity.this,false);

                        Intent intent = new Intent();
                        intent.putExtra("user_id", "");
                        intent.putExtra("user_name", "");
                        intent.putExtra("user_img", "");
                        setResult(2, intent);
                        finish();
                    }
                }).setNegativeButton("??????",null).show();

    }

}
