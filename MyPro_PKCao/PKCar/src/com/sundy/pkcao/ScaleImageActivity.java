package com.sundy.pkcao;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.androidquery.AQuery;
import com.sundy.pkcao.adapters.ImageViewPagerAdapter;

import java.util.ArrayList;

/**
 * Created by sundy on 15/3/10.
 */
public class ScaleImageActivity extends _AbstractActivity {

    private final String TAG = "ScaleImageActivity";
    private ArrayList<String> list;
    private View v;
    private ViewPager viewpager;
    private ImageViewPagerAdapter adapter;
    private int current_position = 1;

    public ScaleImageActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scale_image);

        aq = new AQuery(this);
        init();
    }

    private void init() {
        list = getIntent().getStringArrayListExtra("images");

        aq.id(R.id.btn_perious).clicked(onClickListener);
        aq.id(R.id.btn_next).clicked(onClickListener);
        aq.id(R.id.btnBack).clicked(onClickListener);

        viewpager = (ViewPager) aq.id(R.id.viewpager).getView();
        adapter = new ImageViewPagerAdapter(this);
        viewpager.setAdapter(adapter);
        viewpager.setOnPageChangeListener(pageChangeListener);

        if (list != null) {
            aq.id(R.id.txt_count).text(current_position + "/" + list.size());
            adapter.setData(list);
            adapter.notifyDataSetChanged();
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_perious:
                    showPerious();
                    break;
                case R.id.btn_next:
                    showNext();
                    break;
                case R.id.btnBack:
                    finish();
                    break;
            }
        }
    };

    private void showPerious() {
        if (current_position == 1)
            return;
        current_position = current_position - 1;
        int position = current_position;
        aq.id(R.id.txt_count).text(position + "/" + list.size());
        viewpager.setCurrentItem(position - 1);
    }

    private void showNext() {
        if (current_position == list.size())
            return;
        current_position = current_position + 1;
        int position = current_position;
        aq.id(R.id.txt_count).text(position + "/" + list.size());
        viewpager.setCurrentItem(position - 1);
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            current_position = i + 1;
            aq.id(R.id.txt_count).text(current_position + "/" + list.size());
        }

        @Override
        public void onPageScrollStateChanged(int i) {

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
    public void onDestroy() {
        super.onDestroy();
    }
}