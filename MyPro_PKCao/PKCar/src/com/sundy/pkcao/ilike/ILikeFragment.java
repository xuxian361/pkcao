package com.sundy.pkcao.ilike;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.androidquery.AQuery;
import com.avos.avoscloud.*;
import com.sundy.pkcao.R;
import com.sundy.pkcao._AbstractFragment;
import com.sundy.pkcao.adapters.CaoListAdapter;
import com.sundy.pkcao.caodian.CaoDetailFragment;
import com.sundy.pkcao.register.RegisterFragment;
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
public class ILikeFragment extends _AbstractFragment {

    private final String TAG = "ILikeFragment";
    private Fragment fragment;
    private View v;
    private XListView lv_ilike;
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


    public ILikeFragment() {
    }

    public ILikeFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        v = inflater.inflate(R.layout.ilike, container, false);
        aq = new AQuery(v);

        init();
        return v;
    }

    private void init() {
        aq.id(R.id.txt_header_title).text(getString(R.string.i_like));
        progressbar = (ProgressWheel) aq.id(R.id.progressbar).getView();

        last_updated_time = getString(R.string.just_now);
        lv_ilike = (XListView) aq.id(R.id.lv_ilike).getListView();
        adapter = new CaoListAdapter(context, inflater);
        lv_ilike.setAdapter(adapter);
        lv_ilike.setOnItemClickListener(onItemClickListener);
        lv_ilike.setPullLoadEnable(true);
        lv_ilike.setPullRefreshEnable(true);
        lv_ilike.setXListViewListener(ixListViewListener);

        if (list != null)
            list.clear();

        sharedPreferences = context.getSharedPreferences(CommonUtility.APP_NAME, Context.MODE_PRIVATE);
        getCaodianIlike();
    }

    private void onLoad() {
        lv_ilike.stopRefresh();
        lv_ilike.stopLoadMore();
        lv_ilike.setRefreshTime(last_updated_time);
    }

    private XListView.IXListViewListener ixListViewListener = new XListView.IXListViewListener() {
        @Override
        public void onRefresh() {
            ishasMore = true;
            if (isRefreshing)
                return;
            if (list != null)
                list.clear();
            lv_ilike.setAdapter(adapter);
            curPage = 1;
            last_updated_time = CommonUtility.getLastUpdatedTime();

            getCaodianIlike();
            onLoad();
        }

        @Override
        public void onLoadMore() {
            isRefreshing = false;
            if (ishasMore) {
                if (list.size() / pageNum == curPage - 1)
                    return;
                curPage++;
                getCaodianIlike();
            }
            onLoad();
        }
    };

    private void getCaodianIlike() {
        showProgress(progressbar);
        user_id = sharedPreferences.getString(User.objectId, "");
        AVQuery<AVObject> user_query = AVQuery.getQuery(User.table_name);
        user_query.getInBackground(user_id, new GetCallback<AVObject>() {
            @Override
            public void done(AVObject user, AVException e) {
                if (e == null) {
                    if (caodian_query != null)
                        caodian_query = null;
                    caodian_query = AVRelation.reverseQuery(Caodian.table_name, Caodian.likes, user);
                    caodian_query.orderByDescending(Caodian.createdAt);
                    int skip = (curPage - 1) * pageNum;
                    caodian_query.setSkip(skip);
                    caodian_query.setLimit(10);
                    caodian_query.findInBackground(new FindCallback<AVObject>() {
                        @Override
                        public void done(List<AVObject> caodianlist, AVException e) {
                            isRefreshing = false;
                            stoProgress(progressbar);
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
                                        lv_ilike.setFooterViewText(getString(R.string.no_result));
                                    }
                                } else {
                                    Toast.makeText(context, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                } else {
                    Toast.makeText(context, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
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
                mCallback.addContent(new CaoDetailFragment(ILikeFragment.this, item, type));
                user_id = "";
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
