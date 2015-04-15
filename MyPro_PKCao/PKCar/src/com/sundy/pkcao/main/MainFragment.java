package com.sundy.pkcao.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.androidquery.AQuery;
import com.androidquery.util.Common;
import com.avos.avoscloud.*;
import com.sundy.pkcao.R;
import com.sundy.pkcao._AbstractFragment;
import com.sundy.pkcao.adapters.CaoListAdapter;
import com.sundy.pkcao.caodian.AddCaoDianFragment;
import com.sundy.pkcao.caodian.CaoDetailFragment;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.tools.xlistview.XListView;
import com.sundy.pkcao.vo.Caodian;
import com.sundy.pkcao.vo.Caodian_Img;
import com.sundy.pkcao.vo.User;

import java.util.ArrayList;
import java.util.HashMap;
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
    private LinearLayout linear_filter;
    private boolean isFilterLeftVisible = false;
    private boolean isFilterRightVisible = false;

    private List list = new ArrayList();
    private List tempList = new ArrayList();
    private int curPage = 1;
    private int pageNum = 10;
    private boolean ishasMore = true;
    private boolean isRefreshing = false;
    private String last_updated_time = "";

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

        init();
        return v;
    }

    private void init() {
        last_updated_time = getString(R.string.just_now);
        lv_main = (XListView) aq.id(R.id.lv_main).getListView();
        adapter = new CaoListAdapter(context, inflater);
        lv_main.setAdapter(adapter);
        lv_main.setOnItemClickListener(onItemClickListener);
        lv_main.setPullLoadEnable(true);
        lv_main.setPullRefreshEnable(true);
        lv_main.setXListViewListener(ixListViewListener);

        linear_filter = (LinearLayout) aq.id(R.id.linear_filter).getView();

        aq.id(R.id.btn_filter_left).clicked(onClick);
        aq.id(R.id.btn_filter_right).clicked(onClick);
        aq.id(R.id.btnAdd).clicked(onClick);

        if (list != null)
            list.clear();

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
                getCaodians();
            }
            onLoad();
        }
    };

    private void getCaodians() {
        mCallback.onLoading();
        final AVQuery<AVObject> caoidan_img = AVQuery.getQuery(Caodian_Img.table_name);
        caoidan_img.orderByDescending(Caodian_Img.createdAt);
        caoidan_img.setLimit(pageNum);
        caoidan_img.include(Caodian_Img.caodian);
        if (curPage > 1) {
            caoidan_img.setSkip((curPage - 1) * pageNum);
        }
        caoidan_img.findInBackground(new FindCallback<AVObject>() {
            public void done(List<AVObject> avObjectList, AVException e) {
                mCallback.finishLoading();
                isRefreshing = false;
                onLoad();
                try {
                    if (e == null) {
                        if (avObjectList != null && avObjectList.size() != 0) {
                            for (AVObject caodian_img : avObjectList) {
                                if (caodian_img != null) {
                                    AVObject caodian = caodian_img.getAVObject(Caodian_Img.caodian);
                                    if (caodian != null) {
                                        String oid = caodian.getObjectId();
                                        if (!tempList.contains(oid)) {
                                            tempList.add(oid);
                                            list.add(caodian_img);
                                        }
                                    }
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
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_filter_left:
                    if (linear_filter != null)
                        linear_filter.removeAllViews();
                    if (isFilterLeftVisible == false) {
                        View filter_left = inflater.inflate(R.layout.view_filter_left, null);
                        AQuery aq1 = new AQuery(filter_left);
                        aq1.id(R.id.btn_filter_mine).clicked(onClick);
                        aq1.id(R.id.btn_filter_other).clicked(onClick);
                        aq1.id(R.id.btn_filter_all).clicked(onClick);
                        linear_filter.addView(filter_left);
                        isFilterLeftVisible = true;
                    } else {
                        isFilterLeftVisible = false;
                    }
                    isFilterRightVisible = false;
                    break;
                case R.id.btn_filter_right:
                    if (linear_filter != null)
                        linear_filter.removeAllViews();
                    if (isFilterRightVisible == false) {
                        View filter_right = inflater.inflate(R.layout.view_filter_right, null);
                        AQuery aq1 = new AQuery(filter_right);
                        aq1.id(R.id.btn_filter_latest).clicked(onClick);
                        aq1.id(R.id.btn_filter_hot).clicked(onClick);
                        linear_filter.addView(filter_right);
                        isFilterRightVisible = true;
                    } else {
                        isFilterRightVisible = false;
                    }
                    isFilterLeftVisible = false;
                    break;
                case R.id.btn_filter_all:
                    if (linear_filter != null)
                        linear_filter.removeAllViews();
                    isFilterLeftVisible = false;
                    aq.id(R.id.btn_filter_left).text("全部");

                    break;
                case R.id.btn_filter_mine:
                    if (linear_filter != null)
                        linear_filter.removeAllViews();
                    isFilterLeftVisible = false;
                    aq.id(R.id.btn_filter_left).text("我的");

                    break;
                case R.id.btn_filter_other:
                    if (linear_filter != null)
                        linear_filter.removeAllViews();
                    isFilterLeftVisible = false;
                    aq.id(R.id.btn_filter_left).text("其他人");

                    break;
                case R.id.btn_filter_hot:
                    if (linear_filter != null)
                        linear_filter.removeAllViews();
                    isFilterRightVisible = false;
                    aq.id(R.id.btn_filter_right).text("最热");
                    break;
                case R.id.btn_filter_latest:
                    if (linear_filter != null)
                        linear_filter.removeAllViews();
                    isFilterRightVisible = false;
                    aq.id(R.id.btn_filter_right).text("最新");
                    break;
                case R.id.btnAdd:
                    if (CommonUtility.isLogin(context)) {
                        mCallback.addContent(new AddCaoDianFragment(MainFragment.this));
                    } else {
                        Toast.makeText(context, getString(R.string.please_login), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (list != null && list.size() != 0) {
                AVObject item = (AVObject) list.get(i - 1);
                mCallback.addContent(new CaoDetailFragment(MainFragment.this, item));
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

