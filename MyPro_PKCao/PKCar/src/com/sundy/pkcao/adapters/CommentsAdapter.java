package com.sundy.pkcao.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.androidquery.AQuery;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.sundy.pkcao.R;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.tools.CircleImageView;
import com.sundy.pkcao.vo.Comment;
import com.sundy.pkcao.vo.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sundy on 15/5/26.
 */
public class CommentsAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List list = new ArrayList();

    public CommentsAdapter() {
    }

    public CommentsAdapter(Context context, LayoutInflater inflater) {
        this.context = context;
        this.inflater = inflater;
    }

    public void setData(List list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        if (list != null)
            return list.size();
        return 0;
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
            view = inflater.inflate(R.layout.item_comments, viewGroup, false);
            holder = new ViewHolder();
            AQuery aq = new AQuery(view);
            holder.img = (CircleImageView) aq.id(R.id.img).getImageView();
            holder.txt_username = aq.id(R.id.txt_username).getTextView();
            holder.txt_comment = aq.id(R.id.txt_comment).getTextView();
            holder.txt_date = aq.id(R.id.txt_date).getTextView();
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        try {
            AVObject comment = (AVObject) list.get(i);
            if (comment != null) {
                AVObject user = comment.getAVObject(Comment.author);
                AVFile img_file = user.getAVFile(User.user_img);
                String user_img = img_file.getUrl();
                AQuery aq_img = new AQuery(holder.img);
                if (user_img != null && user_img.length() != 0) {
                    aq_img.id(R.id.img).image(user_img);
                } else {
                    aq_img.id(R.id.img).image(R.drawable.icon_profile);
                }

                holder.txt_username.setText(user.getString(User.username));
                holder.txt_comment.setText((comment.getString(Comment.content)));
                holder.txt_date.setText(CommonUtility.formatDate2String(comment.getCreatedAt()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    static class ViewHolder {
        TextView txt_username, txt_comment, txt_date;
        CircleImageView img;
    }
}