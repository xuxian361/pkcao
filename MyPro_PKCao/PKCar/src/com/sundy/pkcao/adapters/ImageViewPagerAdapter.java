package com.sundy.pkcao.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.androidquery.AQuery;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sundy on 15/2/28.
 */
public class ImageViewPagerAdapter extends PagerAdapter {

    private LinkedList<View> views = new LinkedList<View>();
    private Context context;
    private List list;
    private List<JSONObject> arr;

    public ImageViewPagerAdapter() {
    }

    public ImageViewPagerAdapter(Context context) {
        this.context = context;
    }

    public void setData(List list) {
        this.list = list;
    }

    public void setData(List list, List<JSONObject> arr) {
        this.list = list;
        this.arr = arr;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (views.size() == 4) {
            View view = views.remove(0);
            container.removeView(view);
        }
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        ImageView view = new ImageView(context);
        try {
            if (views.size() == 4) {
                View v = views.remove(0);
                container.removeView(v);
            }
            try {
                view.setScaleType(ImageView.ScaleType.FIT_CENTER);
                AQuery aq1 = new AQuery(view);
                aq1.image((String) list.get(position));
                views.add(view);
                container.addView(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public int getCount() {
        if (list != null)
            return list.size();
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return (view == o);
    }

}

