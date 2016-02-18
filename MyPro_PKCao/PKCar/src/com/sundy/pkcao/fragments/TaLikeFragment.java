package com.sundy.pkcao.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
 * Created by sundy on 15/3/22.
 */
public class TaLikeFragment extends _AbstractFragment {

    private final String TAG = "TaLikeFragment";
    private Fragment fragment;
    private View v;
    private XListView lv_talike;
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

    public TaLikeFragment() {
    }

    public TaLikeFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        v = inflater.inflate(R.layout.talike, container, false);
        aq = new AQuery(v);

        init();
        return v;
    }

    private void init() {
        aq.id(R.id.txt_header_title).text(getString(R.string.ta_like));

        lv_talike = (XListView) aq.id(R.id.lv_talike).getListView();
        adapter = new CaoListAdapter(context, inflater);
        lv_talike.setAdapter(adapter);
        lv_talike.setOnItemClickListener(onItemClickListener);
        lv_talike.setPullLoadEnable(true);
        lv_talike.setPullRefreshEnable(true);
        lv_talike.setXListViewListener(ixListViewListener);

        if (list != null)
            list.clear();

        caodian_query = AVQuery.getQuery(Caodian.table_name);
        sharedPreferences = context.getSharedPreferences(CommonUtility.APP_NAME, Context.MODE_PRIVATE);
        user_id = sharedPreferences.getString(User.objectId, "");
        getCaodianTalike();
    }

    private void onLoad() {
        lv_talike.stopRefresh();
        lv_talike.stopLoadMore();
        lv_talike.setRefreshTime(last_updated_time);
    }

    private XListView.IXListViewListener ixListViewListener = new XListView.IXListViewListener() {
        @Override
        public void onRefresh() {
            rtLog(TAG, "------->isRefreshing = " + isRefreshing);

            ishasMore = true;
            if (isRefreshing)
                return;
            if (list != null)
                list.clear();
            lv_talike.setAdapter(adapter);
            curPage = 1;
            last_updated_time = CommonUtility.getLastUpdatedTime();

            getCaodianTalike();
            onLoad();
        }

        @Override
        public void onLoadMore() {
            isRefreshing = false;
            if (ishasMore) {
                if (list.size() / pageNum == curPage - 1)
                    return;
                curPage++;
                getCaodianTalike();
            }
            onLoad();
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (list.size() % pageNum != 0) {
                        ishasMore = false;
                    }
                    adapter.setData(list);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    };

    private void getCaodianTalike() {
        rtLog(TAG, "-------->getCaodianTalike ");
        showProgress(progressbar);
        if (caodian_query != null)
            caodian_query = null;
        caodian_query = AVQuery.getQuery(Caodian.table_name);
        caodian_query.orderByDescending(Caodian.createdAt);
        int skip = (curPage - 1) * pageNum;
        rtLog(TAG, "-------->curPage = " + curPage);
        rtLog(TAG, "-------->skip = " + skip);
        rtLog(TAG, "-------->user_id = " + user_id);

        caodian_query.setSkip(skip);
        caodian_query.setLimit(10);
        caodian_query.whereEqualTo(Caodian.creater, user_id);
        caodian_query.findInBackground(new FindCallback<AVObject>() {
            public void done(List<AVObject> caodianlist, AVException e) {
                isRefreshing = false;
                stopProgress(progressbar);
                onLoad();
                try {
                    rtLog(TAG, "-------->e = " + e);
                    rtLog(TAG, "-------->caodianlist = " + caodianlist.size());

                    if (e == null) {
                        if (caodianlist != null && caodianlist.size() != 0) {
                            for (int k = 0; k < caodianlist.size(); k++) {
                                final AVObject caodian = caodianlist.get(k);
                                if (caodian != null) {
                                    AVRelation<AVObject> relation = caodian.getRelation(Caodian.likes);
                                    AVQuery<AVObject> avQuery = relation.getQuery();
                                    avQuery.findInBackground(new FindCallback<AVObject>() {
                                        @Override
                                        public void done(List<AVObject> userlist, AVException e) {
                                            if (e == null) {
                                                if (userlist != null && userlist.size() != 0) {
                                                    List<String> users = new ArrayList<String>();
                                                    for (AVObject user : userlist) {
                                                        String oid = user.getObjectId();
                                                        users.add(oid);
                                                    }
                                                    for (int i = 0; i < users.size(); i++) {
                                                        if (!users.equals(user_id)) {
                                                            if (!list.contains(caodian))
                                                                list.add(caodian);
                                                        }
                                                    }
                                                    rtLog(TAG, "-------->list = " + list.size());

                                                    handler.sendEmptyMessage(0);
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        } else {
                            ishasMore = false;
                            lv_talike.setFooterViewText(getString(R.string.no_result));
                        }
                    } else {
                        ishasMore = false;
                        lv_talike.setFooterViewText(getString(R.string.no_result));
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
                String type = "1";
                String uid = sharedPreferences.getString(User.objectId, "");
                if (uid.equals(create_id)) {
                    type = "2";
                }
                mCallback.addContent(new CaoDetailFragment(TaLikeFragment.this, item, type));
            }
        }
    };

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

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
        if (progressbar != null) {
            progressbar = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
