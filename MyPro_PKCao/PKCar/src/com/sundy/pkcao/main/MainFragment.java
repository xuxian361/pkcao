package com.sundy.pkcao.main;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.androidquery.AQuery;
import com.sundy.pkcao.R;
import com.sundy.pkcao._AbstractFragment;
import com.sundy.pkcao.adapters.CaoListAdapter;
import com.sundy.pkcao.caodian.AddCaoDianFragment;
import com.sundy.pkcao.caodian.CaoDetailFragment;

/**
 * Created by sundy on 15/3/21.
 */
public class MainFragment extends _AbstractFragment {

    private final String TAG = "MainFragment";
    private Fragment fragment;
    private View v;
    private ListView lv_main;
    private CaoListAdapter adapter;
    private LinearLayout linear_filter;
    private boolean isFilterLeftVisible = false;
    private boolean isFilterRightVisible = false;

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
        //Test
        lv_main = aq.id(R.id.lv_main).getListView();
        adapter = new CaoListAdapter(context, inflater);
        lv_main.setAdapter(adapter);
        lv_main.setOnItemClickListener(onItemClickListener);

        linear_filter = (LinearLayout) aq.id(R.id.linear_filter).getView();

        aq.id(R.id.btn_filter_left).clicked(onClick);
        aq.id(R.id.btn_filter_right).clicked(onClick);
        aq.id(R.id.btnAdd).clicked(onClick);
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
                    mCallback.addContent(new AddCaoDianFragment());
                    break;
            }
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mCallback.addContent(new CaoDetailFragment());
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

