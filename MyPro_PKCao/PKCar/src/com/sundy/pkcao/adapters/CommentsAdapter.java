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
        try {
            ViewHolder holder = null;
            AVObject comment = (AVObject) list.get(i);
            AVObject user = comment.getAVObject(Comment.author);
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            if (view == null) {
                AVUser currentUser = AVUser.getCurrentUser();
                if (currentUser != null) {
                    if (currentUser.equals(user)) {
                        view = inflater.inflate(R.layout.comments_to_item, null);
                    } else {
                        view = inflater.inflate(R.layout.comments_from_item, null);
                    }
                } else {
                    view = inflater.inflate(R.layout.comments_from_item, null);
                }
                holder = new ViewHolder();
                AQuery aq = new AQuery(view);
                holder.img = (CircleImageView) aq.id(R.id.img).getView();
                holder.txt_content = aq.id(R.id.txt_content).getTextView();
                holder.txt_date = aq.id(R.id.txt_date).getTextView();

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            if (comment != null) {
                holder.txt_content.setText(comment.getString(Comment.content));
                holder.txt_date.setText(CommonUtility.formatDate2String(comment.getCreatedAt()));
                AQuery aq_img = new AQuery(holder.img);

                AVFile img_file = user.getAVFile(User.user_img);
                String user_img = img_file.getUrl();
                if (user_img != null && user_img.length() != 0) {
                    aq_img.image(user_img);
                } else {
                    aq_img.image(R.drawable.icon_profile);
                }
                aq_img.clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e("sundy", "------------click ");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    class ViewHolder {
        TextView txt_content, txt_date;
        CircleImageView img;
    }
}