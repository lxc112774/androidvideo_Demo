package com.example.lxc.android_video.main.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.lxc.android_video.base.SystemProperty;
import com.example.lxc.android_video.entity.VideoPlayComment;
import com.example.lxc.android_video.entity.VideoPlayCommentBean;
import com.example.lxc.android_video.help.BroadCastManager;
import com.example.lxc.android_video.help.StringHelper;
import com.example.lxc.android_video.main.LoginRegister.LoginActivity;
import com.example.lxc.android_video.main.LoginRegister.UserBean;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lxc on 18-12-27.
 */

public class VideoPlayCommentFragment extends Fragment implements View.OnClickListener{

    private RecyclerView play_comment;

    private ImageView comment_user_img;

    private TextView comment_edit;

    private VideoPlayCommentAdapter adapter;

    private List<VideoPlayComment> DataList = new ArrayList<>();

    private int data_id;

    private SwipeRefreshLayout swipeRefreshLayout;

    private LocalReceiver mReceiver;

    private SendCommentDialog sendCommentDialog;

    private static LinearLayout comment_send;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videoplay_comment, container, false);

        initview(view);

        return view;
    }


    private void initview(View view){

        //??????url??????
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction("fragment_play_url");
            mReceiver = new LocalReceiver();
            BroadCastManager.getInstance().registerReceiver(this.getActivity(), mReceiver, filter);//?????????????????????
        } catch (Exception e) {
            e.printStackTrace();
        }

        comment_send = (LinearLayout) view.findViewById(R.id.comment_send);

        play_comment = (RecyclerView) view.findViewById(R.id.play_comment);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        play_comment.setLayoutManager(linearLayoutManager);
        adapter = new VideoPlayCommentAdapter(DataList);
        play_comment.setAdapter(adapter);

        comment_user_img = (ImageView) view.findViewById(R.id.comment_user_img);
        comment_user_img.setOnClickListener(this);

        //???????????????USER??????,????????????
        SystemProperty.getInstance(getActivity()).setUserNull();
        if(SystemProperty.isLogin(getActivity())){
            UserBean userdatas= SystemProperty.getInstance(getActivity()).getUser(getActivity());
            String user_img_url = userdatas.getUserimg();
            if(user_img_url == null || user_img_url.equals("")){
                comment_user_img.setImageResource(R.drawable.lxc);
            }else{
                // Base64????????????
                byte[] imageByteArray = Base64.decode(user_img_url , 0);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                comment_user_img.setImageBitmap(bitmap);
            }
        }else {
            comment_user_img.setImageResource(R.drawable.user_header);
        }

        comment_edit = (TextView) view.findViewById(R.id.comment_edit);
        comment_edit.setOnClickListener(this);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.comment_swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.main_bottom_select);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //????????????
                refreshData();
            }
        });

        // ????????????????????????????????????????????????????????????????????????
        /** swipeRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue
         .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
         .getDisplayMetrics()));**/
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.comment_user_img :
                break;
            case R.id.comment_edit:
                if(SystemProperty.isLogin(getActivity())) {
                    sendCommentDialog = new SendCommentDialog(this.getActivity(), data_id, SystemProperty.getUserId(getActivity()));
                    sendCommentDialog.show();
                    comment_send.setVisibility(View.GONE);
                }else {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivityForResult(intent,1);
                }
                break;
        }
    }

    public static void changComent(){
        comment_send.setVisibility(View.VISIBLE);
    }

    public void refreshData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);//???????????????????????????????????????????????????
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {//????????????????????????
                    @Override
                    public void run() {
                        //????????????
                        DataList.clear();
                        adapter.notifyDataSetChanged();
                        RequestData(data_id);
                        //swipeRefreshLayout.setRefreshing(false);//????????????????????????????????????

                    }
                });
            }
        }).start();
    }

    private void RequestData(final int data_id){
        //????????????
        String url = HttpUrl.PlayComment;    //??????
        String tag = "PlayComment";    //??????

        //??????????????????
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        //????????????????????????????????????tag?????????????????????
        requestQueue.cancelAll(tag);

        //??????StringRequest??????????????????????????????????????????POST(?????????????????????????????????GET??????)
        final StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //StringHelper.showShortMessage(getActivity(),"ssss="+response);
                            //????????????
                            Log.e("lxc","response==="+response);
                            Gson gson = new Gson();
                            VideoPlayCommentBean Result = gson.fromJson(response,VideoPlayCommentBean.class);
                            int result = Result.getCode();
                            List<VideoPlayComment> comments = new ArrayList<>();
                            if (result == 0) {  //??????
                                //????????????????????????????????????????????????
                                comments = Result.getData();
                                Log.e("lxc","comments=="+comments.get(0).getUsername());
                                DataList.addAll(comments);
                                adapter.notifyDataSetChanged();
                            } else {

                            }
                        } catch (Exception e) {
                            //????????????????????????????????????Toast????????????????????????????????????
                        }
                        swipeRefreshLayout.setRefreshing(false);//????????????????????????????????????
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //????????????????????????????????????Toast????????????????????????????????????
                swipeRefreshLayout.setRefreshing(false);//????????????????????????????????????
                StringHelper.showShortMessage(getActivity(),"????????????????????????????????????");
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("data_id", data_id+"");  //??????
                return params;
            }
        };

        //??????Tag??????
        request.setTag(tag);

        //???????????????????????????
        requestQueue.add(request);

    }


    class LocalReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            data_id = intent.getIntExtra("data_id",0);
            Log.e("lxc","data_id="+data_id);
            DataList.clear();
            adapter.notifyDataSetChanged();
            //????????????
            RequestData(data_id);
        }
    }

    @Override
    public void onDestroy() {
        BroadCastManager.getInstance().unregisterReceiver(this.getActivity(),mReceiver);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // operation succeeded. ????????????-1
        if(resultCode == 2){//ok
            if(requestCode == 1){
                String user_img_url = data.getStringExtra("user_img");//????????????

                if(SystemProperty.isLogin(this.getContext())) {
                    if (user_img_url == null || user_img_url.equals("")) {
                        comment_user_img.setImageResource(R.drawable.lxc);
                    } else {
                        // Base64????????????
                        byte[] imageByteArray = Base64.decode(user_img_url, 0);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                        if (bitmap != null) comment_user_img.setImageBitmap(bitmap);

                    }
                }else {
                    comment_user_img.setImageResource(R.drawable.user_header);
                }
            }
        }
    }
}

