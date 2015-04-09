package com.sundy.pkcao.ilike;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.androidquery.AQuery;
import com.sundy.pkcao.R;
import com.sundy.pkcao._AbstractFragment;
import com.sundy.pkcao.adapters.CaoListAdapter;
import com.sundy.pkcao.caodian.CaoDetailFragment;
import com.sundy.pkcao.register.RegisterFragment;

import java.util.List;

/**
 * Created by sundy on 15/3/22.
 */
public class ILikeFragment extends _AbstractFragment {

    private final String TAG = "ILikeFragment";
    private Fragment fragment;
    private View v;
    private ListView lv_ilike;
    private CaoListAdapter adapter;


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
        lv_ilike = aq.id(R.id.lv_ilike).getListView();
        adapter = new CaoListAdapter(context, inflater);
        lv_ilike.setAdapter(adapter);
        lv_ilike.setOnItemClickListener(onItemClickListener);
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mCallback.addContent(new CaoDetailFragment());
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
