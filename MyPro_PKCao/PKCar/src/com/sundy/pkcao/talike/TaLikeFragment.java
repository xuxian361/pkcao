package com.sundy.pkcao.talike;

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

/**
 * Created by sundy on 15/3/22.
 */
public class TaLikeFragment extends _AbstractFragment {

    private final String TAG = "TaLikeFragment";
    private Fragment fragment;
    private View v;
    private ListView lv_talike;
    private CaoListAdapter adapter;


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
        lv_talike = aq.id(R.id.lv_talike).getListView();
        adapter = new CaoListAdapter(context, inflater);
        lv_talike.setAdapter(adapter);
        lv_talike.setOnItemClickListener(onItemClickListener);
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
