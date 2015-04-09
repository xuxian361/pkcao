package com.sundy.pkcao.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.androidquery.AQuery;
import com.sundy.pkcao.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sundy on 15/3/21.
 */
public class CaoListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List list = new ArrayList();

    public CaoListAdapter() {
    }

    public CaoListAdapter(Context context, LayoutInflater inflater) {
        this.context = context;
        this.inflater = inflater;
    }

    public void setData(List list) {
        this.list = list;
    }

    @Override
    public int getCount() {
//        if (list != null)
//            return list.size();
        return 15;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            view = inflater.inflate(R.layout.cao_item, viewGroup, false);
            holder = new ViewHolder();
            AQuery aq = new AQuery(view);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        return view;
    }

    class ViewHolder {
        TextView txt_points;
    }
}
