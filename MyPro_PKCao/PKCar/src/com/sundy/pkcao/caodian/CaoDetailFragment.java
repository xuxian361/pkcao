package com.sundy.pkcao.caodian;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import com.androidquery.AQuery;
import com.sundy.pkcao.R;
import com.sundy.pkcao._AbstractFragment;
import com.sundy.pkcao.adapters.ImageHListAdapter;
import it.sephiroth.android.library.widget.HListView;

/**
 * Created by sundy on 15/3/22.
 */
public class CaoDetailFragment extends _AbstractFragment {

    private final String TAG = "CaoDetailFragment";
    private Fragment fragment;
    private View v;
    private HListView lv_images;
    private ImageHListAdapter adapter;

    public CaoDetailFragment() {
    }

    public CaoDetailFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        v = inflater.inflate(R.layout.cao_detail, container, false);
        aq = new AQuery(v);

        init();
        return v;
    }

    private void init() {
        lv_images = (HListView) aq.id(R.id.lv_images).getView();
        adapter = new ImageHListAdapter(context, inflater);
        lv_images.setAdapter(adapter);

        aq.id(R.id.btn_more).clicked(onClick);
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_more:
                    ScrollView scrollView_detial = (ScrollView) aq.id(R.id.scrollView_detial).getView();
                    if (scrollView_detial.getVisibility() == View.GONE) {
                        scrollView_detial.setVisibility(View.VISIBLE);
                    } else {
                        scrollView_detial.setVisibility(View.GONE);
                    }
                    break;
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

