package com.example.lxc.android_video.main.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lxc.android_video.R;
import com.example.lxc.android_video.base.HttpUrl;
import com.example.lxc.android_video.entity.ExampleBean;
import com.example.lxc.android_video.entity.HeadMessageBase;
import com.example.lxc.android_video.entity.HeadMessageBean;
import com.example.lxc.android_video.entity.VideoBean;
import com.example.lxc.android_video.help.StringHelper;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lxc on 18-4-27.
 */

public class HomeFragment extends Fragment implements View.OnClickListener{

    private ImageView left_guide_img,search_img,history_img;

    private DrawerLayout drawerLayout;

    private GridLayoutManager layoutManager;

    private RecyclerView home_recycler;

    private SwipeRefreshLayout swipeRefreshLayout;

    private HomeDataAdapter adapter;

    //适配器总数据
    private List<Object> VideoList = new ArrayList<>();

    private List<VideoBean> videos = new ArrayList<>();

    private List<HeadMessageBean> headvideos = new ArrayList<>();

    private HeadMessageBase headMessageBase = new HeadMessageBase();

    private int lastVisibleItem;

    //数据索引
    protected int start=0;

    //每页的数据量
    public final static int limit = 10;

    public static boolean ishead = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        left_guide_img = (ImageView) view.findViewById(R.id.left_guide_img);
        left_guide_img.setOnClickListener(this);

        search_img = (ImageView) view.findViewById(R.id.search_img);
        search_img.setOnClickListener(this);

        history_img = (ImageView) view.findViewById(R.id.history_img);
        history_img.setOnClickListener(this);

        //在Fragment中使用Activity中控件的方式,可以打开左侧管理界面
        FragmentActivity activity = (FragmentActivity) getActivity();
        drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawelayout);

        home_recycler = (RecyclerView) view.findViewById(R.id.home_recycler);
        // 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        home_recycler.setHasFixedSize(true);
        home_recycler.setItemAnimator(new DefaultItemAnimator());
        layoutManager = new GridLayoutManager(getActivity(),2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {

                return setSpanSize(position,VideoList);
            }
        });

        home_recycler.setLayoutManager(layoutManager);
        adapter = new HomeDataAdapter(VideoList);
        home_recycler.setAdapter(adapter);


        //上拉
        home_recycler.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView,
                                             int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem + 1 == adapter.getItemCount()) {
                    //swipeRefreshLayout.setRefreshing(true);
                    //上拉刷新
                    // 此处在现实项目中，请换成网络请求数据代码，sendRequest .....
                    //refreshData();
                    StringHelper.showShortMessage(getActivity(),"上拉刷新");
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            }

        });


        //下拉
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.home_swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.main_bottom_select);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //更新数据
                refreshData();
            }
        });

        swipeRefreshLayout.setRefreshing(true);


        //功能：为了保存用户已经看到了哪里，下次进入从这开始
        SharedPreferences sp=getActivity().getSharedPreferences("refresh", Context.MODE_PRIVATE);//refresh存储文件名，MODE_PRIVATE只允许自己程序访问
        int start_position =sp.getInt("start_position", 0); // 使用getString方法获得value，注意第2个参数是value的默认值
        android.util.Log.e("lxc","start_position=="+start_position);
        if(start_position == 0) {
            android.util.Log.e("lxc","1111111");
        }else {
            start = start_position;
            android.util.Log.e("lxc","22222");
        }


        //打开页面就获取数据
        refreshData();

        return view;
    }


    private int setSpanSize(int position, List<Object> list) {
        int count;
        if(list == null || list.size()==0){
            return 2; //2/2=1,每行一列
        }
        if (list.get(position) instanceof HeadMessageBase || (list.get(position) instanceof VideoBean &&  ((VideoBean) list.get(position)).getVideo_kind()==10)) {
            count = 2; //2/2=1,每行一列
        } else {
            count = 1; //2/1=2,每行两列
        }

        return count;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.left_guide_img:
                if (drawerLayout.isDrawerOpen(Gravity.LEFT)){
                    drawerLayout.closeDrawer(Gravity.LEFT);
                }else{
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
                break;
            case R.id.search_img:
                Intent intent = new Intent(getActivity(), SearcherActivity.class);
                startActivity(intent);
                //动画效果一定要放在startActivity和finish之后才能调用
                getActivity().overridePendingTransition(R.anim.slide_bottom_in,R.anim.activity_stay); //R.anim.activity_stay解决打开Acticity的时候会黑屏一下
                break;
            case R.id.history_img:
                Intent intent2 = new Intent(getActivity(), HistoryActivity.class);
                startActivity(intent2);
                break;
        }

    }


    public void RequestData(final int data_start, final int data_limit, final boolean ishead) {
        //请求地址
        String url = HttpUrl.HomeUrl;    //注①
        String tag = "HomeData";    //注②

        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        //防止重复请求，所以先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest，定义字符串请求的请求方式为POST(省略第一个参数会默认为GET方式)
        final StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            /**layoutManager = new GridLayoutManager(getActivity(),2);
                            home_recycler.setLayoutManager(layoutManager);**/
                            android.util.Log.e("lxc","response=="+response);
                            //StringHelper.showShortMessage(getActivity(),"ssss="+response);
                            //结果转换
                            Gson gson = new Gson();
                            ExampleBean Result = gson.fromJson(response,ExampleBean.class);
                            int result = Result.getCode();
                            if (result == 0) {  //注⑤
                                //做自己的登录成功操作，如页面跳转
                                videos = Result.getVideodata();
                                int length = videos.size();
                                android.util.Log.e("lxc","length=="+length);
                                headvideos = Result.getHeadImgdata();
                                int headlength = headvideos.size();
                                if (length == 0) {
                                    //关闭加载更多
                                    return;
                                }
                                if ((videos!=null && videos.size()>0)){
                                    SharedPreferences sp=getActivity().getSharedPreferences("refresh", Context.MODE_PRIVATE);//login存储文件名，MODE_PRIVATE只允许自己程序访问
                                    SharedPreferences.Editor editor=sp.edit();
                                    editor.putInt("start_position", start);
                                    editor.commit();  //提交
                                    android.util.Log.e("lxc","start111=="+start);
                                    //添加数据
                                    VideoList.addAll(0,videos);
                                    if (headlength != 0 && headlength >0){
                                        headMessageBase.setBeanList(headvideos);
                                        VideoList.add(0,headMessageBase);
                                    }
                                    adapter.notifyDataSetChanged();
                                    start+=videos.size();
                                    android.util.Log.e("lxc","start222=="+start);
                                }
                                if(videos.size()<limit){
                                    //关闭加载更多，说明数据库没有数据了，重头开始
                                    start = 0;
                                    android.util.Log.e("lxc","start333=="+start);
                                }
                            } else {
                                //StringHelper.showShortMessage(getActivity(),"查询失败，请下拉刷新");
                                //做自己的登录失败操作，如Toast提示
                            }
                        } catch (Exception e) {
                            //做自己的请求异常操作，如Toast提示（“无网络连接”等）
                            //StringHelper.showShortMessage(getActivity(),"无网络连接");
                            Log.e("TAG", e.getMessage(), e);
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //做自己的响应错误操作，如Toast提示（“请稍后重试”等）
                swipeRefreshLayout.setRefreshing(false);//刷新结束，隐藏刷新进度条
                StringHelper.showShortMessage(getActivity(),"电波无法到达~");
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("start_position", data_start+"");  //注⑥
                params.put("limit_number", data_limit+"");
                params.put("is_headimg", ishead+"");
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }


    public void refreshData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);//本地刷新太快，不沉睡看不到刷新过程
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {//将线程切回主线程
                    @Override
                    public void run() {
                        android.util.Log.e("lxc", "start444===" + start);
                        android.util.Log.e("lxc", "limit444===" + limit);
                        //获取数据
                        RequestData(start, limit, ishead);
                        ishead = false;
                    }
                });
            }
        }).start();
    }



}
