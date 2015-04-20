package com.sundy.pkcao.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.androidquery.AQuery;
import com.avos.avoscloud.*;
import com.sundy.pkcao.R;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.vo.Caodian;
import com.sundy.pkcao.vo.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
            view = inflater.inflate(R.layout.cao_item, viewGroup, false);
            holder = new ViewHolder();
            AQuery aq = new AQuery(view);
            holder.txt_title = aq.id(R.id.txt_title).getTextView();
            holder.txt_count = aq.id(R.id.txt_count).getTextView();
            holder.img = aq.id(R.id.img).getImageView();
            holder.img_play = aq.id(R.id.img_play).getImageView();
            holder.btn_add = aq.id(R.id.btn_add).getTextView();
            holder.btn_share = aq.id(R.id.btn_share).getTextView();

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final AVObject caodian = (AVObject) list.get(i);
        if (caodian != null) {
            holder.txt_title.setText(caodian.getString(Caodian.title));
            AQuery aq_img = new AQuery(holder.img);
            AQuery aq_img_play = new AQuery(holder.img_play);
            final AVFile video = caodian.getAVFile(Caodian.caodian_video);
            if (video != null) {
                final String video_path = video.getUrl();
                AVFile video_thumbnail = caodian.getAVFile(Caodian.caodian_video_thumbnail);
                if (video_thumbnail != null) {
                    String video_thumbnail_img = video_thumbnail.getUrl();
                    if (video_thumbnail_img != null && video_thumbnail_img.length() != 0) {
                        aq_img.visible().image(video_thumbnail_img);
                    } else {
                        aq_img.visible().image(R.drawable.logo);
                    }
                } else {
                    aq_img.visible().image(R.drawable.logo);
                }

                aq_img_play.clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        String type = "video/mp4";
                        Uri uri = Uri.parse(video_path);
                        intent.setDataAndType(uri, type);
                        context.startActivity(intent);
                    }
                });
            } else {
                aq_img_play.gone();
                AVFile img1 = caodian.getAVFile(Caodian.img1);
                if (img1 != null) {
                    String img1Url = img1.getUrl();
                    if (img1Url != null && img1Url.length() != 0) {
                        aq_img.visible().image(img1Url);
                    } else {
                        aq_img.gone();
                    }
                }
            }

            holder.btn_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (CommonUtility.isLogin((Activity) context)) {
                        likeCaodian(caodian, view);
                    } else {
                        Toast.makeText(context, context.getString(R.string.please_login), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            holder.btn_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HashMap<String, String> data = new HashMap<String, String>();
                    data.put("title", caodian.getString(Caodian.title));
                    data.put("content", caodian.getString(Caodian.content));
                    if (video != null) {
                        AVFile video_thumbnail = caodian.getAVFile(Caodian.caodian_video_thumbnail);
                        if (video_thumbnail != null) {
                            String video_thumbnail_img = video_thumbnail.getUrl();
                            if (video_thumbnail_img != null && video_thumbnail_img.length() != 0) {
                                data.put("img_url", video_thumbnail_img);
                            } else {
                                AVFile img1 = caodian.getAVFile(Caodian.img1);
                                if (img1 != null) {
                                    data.put("img_url", img1.getUrl());
                                }
                            }
                        }
                    }
//                    data.put("url", "url");
                    CommonUtility.showShare(context, data);
                }
            });
        }
        return view;
    }


    public void likeCaodian(final AVObject caodian, View view) {
        final AQuery aQuery = new AQuery(view);
        SharedPreferences preferences = context.getSharedPreferences(CommonUtility.APP_NAME, Context.MODE_PRIVATE);
        String user_id = preferences.getString(User.objectId, "");
        //查询User
        AVQuery<AVObject> user_query = new AVQuery<AVObject>(User.table_name);
        user_query.getInBackground(user_id, new GetCallback<AVObject>() {
            @Override
            public void done(final AVObject user, AVException e) {
                if (e == null) {
                    if (user != null) {
                        //先找到改user 是否 已经+1 过
//                        final AVRelation<AVObject> relation = caodian.getRelation(Caodian.like);
//                        relation.getQuery().findInBackground(new FindCallback<AVObject>() {
//                            @Override
//                            public void done(List<AVObject> userlist, AVException e) {
//                                if (e == null) {
//                                    boolean isAdded = false;
//                                    if (userlist != null && userlist.size() != 0) {
//                                        for (int i = 0; i < userlist.size(); i++) {
//                                            AVObject user2 = userlist.get(i);
//                                            if (user.equals(user2)) {
//                                                isAdded = true;
//                                            } else {
//                                                isAdded = false;
//                                            }
//                                        }
//                                    } else {
//                                        isAdded = false;
//                                    }
//                                    if (isAdded) {
//                                        //点过 +1
//                                        aQuery.background(R.drawable.corner_all_light_blue_strok).enabled(false);
//                                    } else {
//                                        //未点过 +1
//                                        aQuery.background(R.drawable.corner_all_white2_strok).enabled(true);
//                                        relation.add(user);
//                                        caodian.saveInBackground(new SaveCallback() {
//                                            @Override
//                                            public void done(AVException e) {
//                                                if (e == null) {
//                                                    aQuery.background(R.drawable.corner_all_light_blue_strok).enabled(false);
//                                                } else {
//                                                    aQuery.background(R.drawable.corner_all_white2_strok).enabled(true);
//                                                    Toast.makeText(context, context.getString(R.string.server_error), Toast.LENGTH_SHORT).show();
//                                                }
//                                            }
//                                        });
//                                    }
//                                } else {
//                                    Toast.makeText(context, context.getString(R.string.server_error), Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    class ViewHolder {
        TextView txt_title, txt_count;
        ImageView img, img_play;
        TextView btn_add, btn_share;
    }
}
