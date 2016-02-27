package com.sundy.pkcao.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.androidquery.AQuery;
import com.avos.avoscloud.*;
import com.sundy.pkcao.R;
import com.sundy.pkcao.adapters.CaoListAdapter;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.tools.ProgressWheel;
import com.sundy.pkcao.tools.xlistview.XListView;
import com.sundy.pkcao.vo.Caodian;
import com.sundy.pkcao.vo.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sundy on 15/3/21.
 */
public class MainFragment extends _AbstractFragment {

    private final String TAG = "MainFragment";
    private Fragment fragment;
    private View v;
    private XListView lv_main;
    private CaoListAdapter adapter;

    private List list = new ArrayList();
    private int curPage = 1;
    private int pageNum = 10;
    private boolean ishasMore = true;
    private boolean isRefreshing = false;
    private String last_updated_time = "";
    private ProgressWheel progressbar;
    private AVQuery<AVObject> caodian_query;
    private SharedPreferences sharedPreferences;
    private String user_id;
    private String currentType = "1";
    private final int HIDDEN = 0;
    private final int SHOW = 1;
    private LinearLayout linear_main_bottom;

    public MainFragment() {
    }

    public MainFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        v = inflater.inflate(R.layout.main, container, false);
        aq = new AQuery(v);
        curPage = 1;

        init();
        return v;
    }

    private void init() {
        progressbar = (ProgressWheel) aq.id(R.id.progressbar).getView();
        last_updated_time = getString(R.string.just_now);
        lv_main = (XListView) aq.id(R.id.lv_main).getListView();
        adapter = new CaoListAdapter(context, inflater);
        lv_main.setAdapter(adapter);
        lv_main.setOnItemClickListener(onItemClickListener);
        lv_main.setPullLoadEnable(true);
        lv_main.setPullRefreshEnable(true);
        lv_main.setXListViewListener(ixListViewListener);

        linear_main_bottom = (LinearLayout) aq.find(R.id.linear_main_bottom).getView();
        aq.id(R.id.btnAdd).clicked(onClick);
        aq.id(R.id.btn_filter_mine).clicked(onClick);
        aq.id(R.id.btn_filter_other).clicked(onClick);
        aq.id(R.id.btn_filter_all).clicked(onClick);

        if (list != null)
            list.clear();

        if (caodian_query != null)
            caodian_query = null;
        caodian_query = AVQuery.getQuery(Caodian.table_name);
        sharedPreferences = context.getSharedPreferences(CommonUtility.APP_NAME, Context.MODE_PRIVATE);
        getCaodians();
    }

    private void onLoad() {
        lv_main.stopRefresh();
        lv_main.stopLoadMore();
        lv_main.setRefreshTime(last_updated_time);
    }

    private XListView.IXListViewListener ixListViewListener = new XListView.IXListViewListener() {
        @Override
        public void onRefresh() {
            ishasMore = true;
            if (isRefreshing)
                return;
            if (list != null)
                list.clear();
            lv_main.setAdapter(adapter);
            curPage = 1;
            last_updated_time = CommonUtility.getLastUpdatedTime();

            if (caodian_query != null)
                caodian_query = null;
            caodian_query = AVQuery.getQuery(Caodian.table_name);
            if (currentType.equals("1")) {
            } else if (currentType.equals("2")) {
                user_id = sharedPreferences.getString(User.objectId, "");
                caodian_query.whereEqualTo(Caodian.creater, user_id);
            } else if (currentType.equals("3")) {
                user_id = sharedPreferences.getString(User.objectId, "");
                caodian_query.whereNotEqualTo(Caodian.creater, user_id);
            }
            getCaodians();
            onLoad();
        }

        @Override
        public void onLoadMore() {
            isRefreshing = false;
            if (ishasMore) {
                if (list.size() / pageNum == curPage - 1)
                    return;
                curPage++;
                if (caodian_query != null)
                    caodian_query = null;
                caodian_query = AVQuery.getQuery(Caodian.table_name);
                if (currentType.equals("1")) {
                } else if (currentType.equals("2")) {
                    user_id = sharedPreferences.getString(User.objectId, "");
                    caodian_query.whereEqualTo(Caodian.creater, user_id);
                } else if (currentType.equals("3")) {
                    user_id = sharedPreferences.getString(User.objectId, "");
                    caodian_query.whereNotEqualTo(Caodian.creater, user_id);
                }
                getCaodians();
            }
            onLoad();
        }
    };

    //获取槽点
    private void getCaodians() {
        showProgress(progressbar);
        caodian_query.orderByDescending(Caodian.createdAt);
        int skip = (curPage - 1) * pageNum;
        caodian_query.setSkip(skip);
        caodian_query.setLimit(pageNum);
        isRefreshing = true;
        caodian_query.findInBackground(new FindCallback<AVObject>() {
            public void done(List<AVObject> caodianlist, AVException e) {
                isRefreshing = false;
                stopProgress(progressbar);
                onLoad();
                try {
                    if (e == null) {
                        if (caodianlist != null && caodianlist.size() != 0) {
                            for (AVObject caodian : caodianlist) {
                                if (caodian != null) {
                                    list.add(caodian);
                                }
                            }
                            if (list.size() % pageNum != 0) {
                                ishasMore = false;
                            }

                            adapter.setData(list);
                            adapter.notifyDataSetChanged();
                        } else {
                            ishasMore = false;
                            lv_main.setFooterViewText(getString(R.string.no_result));
                        }
                    } else {
                        ishasMore = false;
                        Toast.makeText(context, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (list != null && list.size() != 0) {
                AVObject item = (AVObject) list.get(i - 1);
                String create_id = item.getString(Caodian.creater);
                user_id = sharedPreferences.getString(User.objectId, "");
                String type = "1";
                if (user_id.equals(create_id)) {
                    type = "2";
                }
                mCallback.addContent(new CaoDetailFragment(MainFragment.this, item, type));
                user_id = "";
            }
        }
    };

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_filter_all:
                    currentType = "1";
                    ishasMore = true;
                    if (isRefreshing)
                        return;
                    if (list != null)
                        list.clear();
                    lv_main.setAdapter(adapter);
                    curPage = 1;
                    last_updated_time = CommonUtility.getLastUpdatedTime();

                    if (caodian_query != null)
                        caodian_query = null;
                    caodian_query = AVQuery.getQuery(Caodian.table_name);
                    getCaodians();
                    break;
                case R.id.btn_filter_mine:
                    if (CommonUtility.isLogin(context)) {
                        currentType = "2";
                        ishasMore = true;
                        if (isRefreshing)
                            return;
                        if (list != null)
                            list.clear();
                        lv_main.setAdapter(adapter);
                        curPage = 1;
                        last_updated_time = CommonUtility.getLastUpdatedTime();

                        if (caodian_query != null)
                            caodian_query = null;
                        caodian_query = AVQuery.getQuery(Caodian.table_name);
                        user_id = sharedPreferences.getString(User.objectId, "");
                        caodian_query.whereEqualTo(Caodian.creater, user_id);
                        getCaodians();
                    } else {
                        mCallback.addContent(new LoginFragment());
                    }
                    break;
                case R.id.btn_filter_other:
                    if (CommonUtility.isLogin(context)) {
                        currentType = "3";
                        ishasMore = true;
                        if (isRefreshing)
                            return;
                        if (list != null)
                            list.clear();
                        lv_main.setAdapter(adapter);
                        curPage = 1;
                        last_updated_time = CommonUtility.getLastUpdatedTime();

                        if (caodian_query != null)
                            caodian_query = null;
                        caodian_query = AVQuery.getQuery(Caodian.table_name);
                        user_id = sharedPreferences.getString(User.objectId, "");
                        caodian_query.whereNotEqualTo(Caodian.creater, user_id);
                        getCaodians();
                    } else {
                        mCallback.addContent(new LoginFragment());
                    }
                    break;
                case R.id.btnAdd:
                    if (CommonUtility.isLogin(context)) {
                        mCallback.addContent(new AddCaoDianFragment(MainFragment.this));
                    } else {
                        mCallback.addContent(new LoginFragment());
                    }
                    break;
            }
        }
    };

    //更新用户状态
    private void refreshUserInfo() {
        if (CommonUtility.isLogin(context)) {
            getUserInfo();
        } else {
            aq.id(R.id.btnMenu).image(R.drawable.logo);
        }
    }

    //获取用户信息
    private void getUserInfo() {
        AVUser currentUser = AVUser.getCurrentUser();
        if (currentUser != null) {
            AVFile img_file = currentUser.getAVFile(User.user_img);
            String user_img = img_file.getUrl();
            if (user_img != null && user_img.length() != 0)
                aq.id(R.id.btnMenu).image(user_img);
            else {
                aq.id(R.id.btnMenu).image(R.drawable.logo);
            }
        } else {
            aq.id(R.id.btnMenu).image(R.drawable.logo);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("sundy", "-------->onResume");
        refreshUserInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressbar != null)
            progressbar = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

