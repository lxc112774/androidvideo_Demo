package com.example.lxc.android_video.main.LoginRegister;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lxc.android_video.R;
import com.example.lxc.android_video.base.BaseActivity;
import com.example.lxc.android_video.base.HttpUrl;
import com.example.lxc.android_video.base.SystemProperty;
import com.example.lxc.android_video.help.AppStore;
import com.example.lxc.android_video.help.StringHelper;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;



/**
 * Created by lxc on 18-10-30.
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    //返回
    private ImageView back;

    //注册按钮
    private TextView registerText;

    //邮箱号输入框
    private EditText edtMail;

    //密码输入框号码输入框
    private EditText edtPassword;

    //登陆按钮
    private Button loginBtn;

    /**
     * 构造函数
     */
    public LoginActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logintab);

        initView();
    }

    private void initView(){
        back = (ImageView) findViewById(R.id.back);
        registerText = (TextView) findViewById(R.id.register_text);
        edtMail = (EditText) findViewById(R.id.edt_mail);
        edtPassword = (EditText) findViewById(R.id.edt_password);
        loginBtn = (Button) findViewById(R.id.loginBtn);

        back.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
        registerText.setOnClickListener(this);

    }

    /**
     * 点击事件
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.back://返回
                LoginActivity.this.finish();
                break;
            case R.id.loginBtn://登陆按钮点击事件
                //登陆
                login();
                break;
            case R.id.forget_password://忘记密码按钮点击事件
                //忘记密码
                getPwd();
                break;
            case R.id.register_text://注册按钮点击事件
                register();
                break;
        }
    }

    /**
     * 登陆
     */
    public void login(){
        //手机邮箱号
        final String name =edtMail.getText().toString().trim();
        //密码
        final String pass=edtPassword.getText().toString().trim();

        if(TextUtils.isEmpty(name)) {
            showShortMessage(this,"手机号不可为空！");
            return;
        }else if(TextUtils.isEmpty(pass)) {
            showShortMessage(this,"密码不可为空！");
            return;
        }else if(pass.length()<3||pass.length()>12) {
            showShortMessage(this,"密码长度应该在3-12位之间！");
            return;
        }

        //请求HTTP验证登陆
        //显示Loading框
        //showLoading("正在登录...",false);
        LoginRequest(name,pass);

    }


    public void LoginRequest(final String phonenumber, final String password) {
        //请求地址
        String url = HttpUrl.LoginUrl;    //注①
        String tag = "Login";    //注②

        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);

        //防止重复请求，所以先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest，定义字符串请求的请求方式为POST(省略第一个参数会默认为GET方式)
        final StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //showShortMessage(getApplicationContext(),"sss"+response);
                            //结果转换
                            Gson gson = new Gson();
                            BaserBena Result = gson.fromJson(response,BaserBena.class);
                            int result = Result.getCode();
                            UserBean user = Result.getData();
                            int user_id = user.getId();
                            String phone_number = user.getPhonenumber();
                            String user_name = user.getUsername();
                            String user_img = user.getUserimg();
                            Log.e("lxc","user_img=="+user_img);
                            String password = user.getPassword();
                            if (result == 2) {  //注⑤
                                //做自己的登录成功操作，如页面跳转

                                //保存到数据库
                                //保存登陆成功信息
                                AppStore.putSettingValue(LoginActivity.this,"login_result", response);
                                //保存用户ID
                                AppStore.putSettingValue(LoginActivity.this,"user_id", String.valueOf(user_id));
                                //保存登陆手机号和MD5加密后的密码
                                AppStore.putSettingValue(LoginActivity.this,"phone",phone_number);
                                AppStore.putSettingValue(LoginActivity.this,"pwd", StringHelper.md5(password));

                                //清除之前的USER信息
                                SystemProperty.getInstance(LoginActivity.this).setUserNull();
                                //设置已登陆
                                SystemProperty.setIsLogin(LoginActivity.this,true);
                                //将获取的值回传回去
                                Intent intent = new Intent();
                                intent.putExtra("user_id", user_id);
                                intent.putExtra("user_name", user_name);
                                intent.putExtra("user_img", user_img);
                                //通过intent对象返回结果，必须要调用一个setResult方法，
                                //setResult(resultCode, data);第一个参数表示结果返回码，一般只要大于1就可以
                                setResult(2, intent);
                                finish();

                            } else {
                                showShortMessage(getApplicationContext(),"手机号或密码不对，请重新输入");
                                //做自己的登录失败操作，如Toast提示
                            }
                        } catch (Exception e) {
                            //做自己的请求异常操作，如Toast提示（“无网络连接”等）
                            showShortMessage(getApplicationContext(),"无网络连接");
                            Log.e("TAG", e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //做自己的响应错误操作，如Toast提示（“请稍后重试”等）
                showShortMessage(getApplicationContext(),"服务器无响应，请稍后重试");
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("phonenumber", phonenumber);  //注⑥
                params.put("password", password);
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }


    /**
     * 忘记密码
     */
    public void getPwd(){

    }

    /**
     * 注册按钮
     */
    public void register(){
        Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }


}
