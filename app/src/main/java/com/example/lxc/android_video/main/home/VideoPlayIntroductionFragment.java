package com.example.lxc.android_video.main.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.lxc.android_video.base.HttpUrl;
import com.example.lxc.android_video.entity.VideoPlayData;
import com.example.lxc.android_video.entity.VideoPlayDataBean;
import com.example.lxc.android_video.help.download_img.BitmapUtils;
import com.example.lxc.android_video.help.StringHelper;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lxc on 18-12-27.
 */

public class VideoPlayIntroductionFragment extends Fragment {


    private RecyclerView play_recyclerView;

    private TextView detail_describe,detail_text;

    private ImageView play_video_img;

    private List<VideoPlayData> DataList = new ArrayList<>();

    private VideoPlayIntroductionDataAdapter adapter;

    private int video_id = 0; //这个剧的id

    private String introduction_content,video_name,video_img;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videoplay_introduction, container, false);

        //课程ID
        video_id = VideoPlayMainActivity.getInstance().video_id;
        video_img = VideoPlayMainActivity.getInstance().video_img;
        video_name = VideoPlayMainActivity.getInstance().video_name;
        introduction_content = VideoPlayMainActivity.getInstance().introduction_content;
        Log.e("lxc","video_id==="+video_id+",video_name=="+video_name+",video_img=="+video_img+",introduction_content=="+introduction_content);


        detail_text = (TextView) view.findViewById(R.id.detail_text);
        detail_describe = (TextView) view.findViewById(R.id.detail_describe);
        detail_text.setText(video_name);
        detail_describe.setText(introduction_content);

        play_video_img = (ImageView) view.findViewById(R.id.play_video_img);
        new BitmapUtils().disPlay(play_video_img,video_img);

        //获取数据
        RequestData(video_id);

        play_recyclerView = (RecyclerView) view.findViewById(R.id.play_episodes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL); //横向布局
        play_recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new VideoPlayIntroductionDataAdapter(getActivity(),DataList);
        play_recyclerView.setAdapter(adapter);

        return view;
    }


    public void RequestData(final int video_id) {
        //请求地址
        String url = HttpUrl.PlayUrl;    //注①
        String tag = "PlayData";    //注②

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
                            //StringHelper.showShortMessage(getActivity(),"ssss="+response);
                            //结果转换
                            Log.e("lxc","response==="+response);
                            Gson gson = new Gson();
                            VideoPlayDataBean Result = gson.fromJson(response,VideoPlayDataBean.class);
                            int result = Result.getCode();
                            List<VideoPlayData> videos = new ArrayList<>();
                            if (result == 0) {  //注⑤
                                //做自己的登录成功操作，如页面跳转
                                videos = Result.getData();
                                int length = videos.size();
                                if(length == 0){
                                    videos.add(setData());
                                    Log.e("lxc","无数据,添加播放测试数据");
                                }
                                DataList.addAll(videos);
                                adapter.notifyDataSetChanged();

                            } else {
                                StringHelper.showShortMessage(getActivity(),"查询失败，请下拉刷新");
                                //做自己的登录失败操作，如Toast提示
                            }
                        } catch (Exception e) {
                            //做自己的请求异常操作，如Toast提示（“无网络连接”等）
                            StringHelper.showShortMessage(getActivity(),"无网络连接");
                            Log.e("TAG", e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //做自己的响应错误操作，如Toast提示（“请稍后重试”等）
                StringHelper.showShortMessage(getActivity(),"服务器无响应，请稍后重试");
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("introduction_id", video_id+"");  //注⑥
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }


    /**
     * 如果无数据,填充测试视频
     * @return
     */
    private VideoPlayData setData(){
        VideoPlayData videoPlayData = new VideoPlayData();
        videoPlayData.setData_id(0);
        videoPlayData.setVideo_name(1);
        videoPlayData.setVideo_describe("第一集");
        videoPlayData.setVideo_url("http://flv2.bn.netease.com/videolib3/1611/28/GbgsL3639/SD/movie_index.m3u8");//测试视频
        return videoPlayData;
    }
}
