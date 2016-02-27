package com.sundy.pkcao.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.androidquery.AQuery;
import com.avos.avoscloud.*;
import com.sundy.pkcao.R;
import com.sundy.pkcao.activitys.CommentsActivity;
import com.sundy.pkcao.activitys.ScaleImageViewActivity;
import com.sundy.pkcao.adapters.CommentsAdapter;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.tools.DensityUtil;
import com.sundy.pkcao.tools.EmotionEditText;
import com.sundy.pkcao.tools.ProgressWheel;
import com.sundy.pkcao.tools.SimpleTextWatcher;
import com.sundy.pkcao.tools.xlistview.XListView;
import com.sundy.pkcao.vo.Caodian;
import com.sundy.pkcao.vo.Comment;
import com.sundy.pkcao.vo.User;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by sundy on 15/3/22.
 */
public class CaoDetailFragment extends _AbstractFragment {

    private final String TAG = "CaoDetailFragment";
    private int Video_Image_Height = CommonUtility.SCREEN_WIDTH * 16 / 9;
    private AVObject item;
    private Fragment fragment;
    private View v;
    private SharedPreferences preferences;
    private String type = "1";
    private String image_url;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            HashMap<String, String> data = new HashMap<String, String>();
            data.put("title", item.getString(Caodian.title));
            data.put("content", item.getString(Caodian.content));
            if (image_url != null && image_url.length() != 0)
                data.put("img_url", image_url);
            CommonUtility.showShare(context, data);
        }
    };

    public CaoDetailFragment() {
    }

    public CaoDetailFragment(Fragment fragment, AVObject item, String type) {
        this.fragment = fragment;
        this.item = item;
        this.type = type;
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
        aq.id(R.id.txt_header_title).text(getString(R.string.detail));
        aq.id(R.id.btn_share).clicked(onClick);
        aq.id(R.id.btn_add).clicked(onClick);
        aq.id(R.id.btn_delete).clicked(onClick);
        aq.id(R.id.btn_comments).clicked(onClick);
        aq.id(R.id.txt_check_more_comments).clicked(onClick);
        Video_Image_Height = DensityUtil.px2dip(context, Video_Image_Height);
        try {
            if (type.equals("2"))
                aq.id(R.id.btn_delete).visible();
            else
                aq.id(R.id.btn_delete).gone();
            preferences = context.getSharedPreferences(CommonUtility.APP_NAME, Context.MODE_PRIVATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getLikesCount();
        showCaodian();
        getComments();
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_share:
                    share();
                    break;
                case R.id.btn_add:
                    if (CommonUtility.isLogin(context)) {
                        likeCaodian();
                    } else {
                        Toast.makeText(context, context.getString(R.string.please_login), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btn_delete:
                    delete();
                    break;
                case R.id.btn_comments:
                    goCommentsPage();
                    break;
                case R.id.txt_check_more_comments:
                    goCommentsPage();
                    break;
                default:
                    break;
            }
        }
    };

    //获取最多5条评论
    private void getComments() {
        AVQuery<AVObject> query = AVQuery.getQuery(Comment.table_name);
        query.orderByDescending(Comment.createdAt);
        query.whereEqualTo(Comment.post, item);
        query.include(Comment.author);
        query.setLimit(5);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> comments, AVException e) {
                try {
                    if (e == null) {
                        if (comments != null && comments.size() != 0) {
                            aq.id(R.id.linear_comments).visible();
                            showComments();

                        } else {
                            aq.id(R.id.linear_comments).invisible();
                        }
                    } else {
                        Toast.makeText(context, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    //显最多5条评论
    private void showComments() {
        LinearLayout linear_comments_content = (LinearLayout) aq.id(R.id.linear_comments_content).getView();
        linear_comments_content.removeAllViews();
        View view = inflater.inflate(R.layout.comments_to_item, null);
        

    }

    //获取"赞"数量
    private void getLikesCount() {
        AVRelation<AVObject> relation = item.getRelation(Caodian.likes);
        relation.getQuery().findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> userlist, AVException e) {
                if (e == null) {
                    aq.id(R.id.txt_count).text("( " + userlist.size() + "" + "+ )");
                    for (AVObject user : userlist) {
                        String objectId = user.getObjectId();
                        String user_id = preferences.getString(User.objectId, "");
                        if (objectId.equals(user_id)) {
                            aq.id(R.id.btn_add).background(R.drawable.corner_all_light_blue_strok).enabled(false);
                        }
                    }
                } else {
                    aq.id(R.id.btn_add).background(R.drawable.corner_all_white2_strok).enabled(true);
                }
            }
        });
    }

    //显示槽点内容
    private void showCaodian() {
        aq.id(R.id.txt_title).text(item.getString(Caodian.title));
        aq.id(R.id.txt_content).text(item.getString(Caodian.content));

        AVFile caodian_video = item.getAVFile(Caodian.caodian_video);
        if (caodian_video != null) {
            final String video_url = caodian_video.getUrl();
            if (video_url != null && video_url.length() != 0) {
                AVFile thumbnail = item.getAVFile(Caodian.caodian_video_thumbnail);
                if (thumbnail != null) {
                    String thumbnail_url = thumbnail.getUrl();
                    if (thumbnail_url != null && thumbnail_url.length() != 0) {
                        aq.id(R.id.relative_video).visible();

                        ImageView video_img = aq.id(R.id.img).getImageView();
                        ViewGroup.LayoutParams param = video_img.getLayoutParams();
                        param.height = Video_Image_Height;
                        video_img.setLayoutParams(param);

                        aq.id(R.id.img).image(thumbnail_url).clicked(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                String type = "video/mp4";
                                Uri uri = Uri.parse(video_url);
                                intent.setDataAndType(uri, type);
                                context.startActivity(intent);
                            }
                        });
                    }
                }
            }
        }

        final LinearLayout linear_img = (LinearLayout) aq.id(R.id.linear_img).getView();
        linear_img.removeAllViews();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 10, 0, 0);

        AVFile img1 = item.getAVFile(Caodian.img1);
        AVFile img2 = item.getAVFile(Caodian.img2);
        AVFile img3 = item.getAVFile(Caodian.img3);
        AVFile img4 = item.getAVFile(Caodian.img4);
        AVFile img5 = item.getAVFile(Caodian.img5);

        final List<String> images = new ArrayList<String>();
        if (img1 != null) {
            final String img1_url = img1.getUrl();
            if (img1_url != null && img1_url.length() != 0) {
                ImageView v1 = new ImageView(context);
                AQuery aq_v1 = new AQuery(v1);
                images.add(img1_url);
                aq_v1.image(img1_url).clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), ScaleImageViewActivity.class);
                        intent.putExtra("image", img1_url);
                        startActivity(intent);
                    }
                });
                v1.setScaleType(ImageView.ScaleType.FIT_START);
                v1.setLayoutParams(params);
                linear_img.addView(v1);
                linear_img.setVisibility(View.VISIBLE);
            }
        }
        if (img2 != null) {
            final String img2_url = img2.getUrl();
            if (img2_url != null && img2_url.length() != 0) {
                ImageView v2 = new ImageView(context);
                AQuery aq_v2 = new AQuery(v2);
                images.add(img2_url);
                aq_v2.image(img2_url).clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), ScaleImageViewActivity.class);
                        intent.putExtra("image", img2_url);
                        startActivity(intent);
                    }
                });
                v2.setScaleType(ImageView.ScaleType.FIT_START);
                v2.setLayoutParams(params);
                linear_img.addView(v2);
                linear_img.setVisibility(View.VISIBLE);
            }
        }
        if (img3 != null) {
            final String img3_url = img3.getUrl();
            if (img3_url != null && img3_url.length() != 0) {
                ImageView v3 = new ImageView(context);
                AQuery aq_v3 = new AQuery(v3);
                images.add(img3_url);
                aq_v3.image(img3_url).clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), ScaleImageViewActivity.class);
                        intent.putExtra("image", img3_url);
                        startActivity(intent);
                    }
                });
                v3.setScaleType(ImageView.ScaleType.FIT_START);
                v3.setLayoutParams(params);
                linear_img.addView(v3);
                linear_img.setVisibility(View.VISIBLE);
            }
        }
        if (img4 != null) {
            final String img4_url = img4.getUrl();
            if (img4_url != null && img4_url.length() != 0) {
                ImageView v4 = new ImageView(context);
                AQuery aq_v4 = new AQuery(v4);
                images.add(img4_url);
                aq_v4.image(img4_url).clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), ScaleImageViewActivity.class);
                        intent.putExtra("image", img4_url);
                        startActivity(intent);
                    }
                });
                v4.setScaleType(ImageView.ScaleType.FIT_START);
                v4.setLayoutParams(params);
                linear_img.addView(v4);
                linear_img.setVisibility(View.VISIBLE);
            }
        }
        if (img5 != null) {
            final String img5_url = img5.getUrl();
            if (img5_url != null && img5_url.length() != 0) {
                ImageView v5 = new ImageView(context);
                AQuery aq_v5 = new AQuery(v5);
                images.add(img5_url);
                aq_v5.image(img5_url).clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), ScaleImageViewActivity.class);
                        intent.putExtra("image", img5_url);
                        startActivity(intent);
                    }
                });
                v5.setScaleType(ImageView.ScaleType.FIT_START);
                v5.setLayoutParams(params);
                linear_img.addView(v5);
                linear_img.setVisibility(View.VISIBLE);
            }
        }


    }

    //跳转至评论页
    private void goCommentsPage() {
        Intent intent = new Intent(context, CommentsActivity.class);
        startActivity(intent);
    }

    //删除该槽点弹出提示框
    private void delete() {
        View view = inflater.inflate(R.layout.dialog_ok_cancel, null);
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(view);
        AQuery aq = new AQuery(view);
        aq.id(R.id.txt_msg).text(getString(R.string.delete_caodian));
        aq.id(R.id.btn_done).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog.cancel();
            }
        });
        aq.id(R.id.btn_ok).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog.cancel();
                deleteCaodian();
            }
        });
        dialog.show();
    }

    //删除该槽点内容
    private void deleteCaodian() {
        item.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    mCallback.switchContent(new MainFragment());
                } else {
                    Toast.makeText(context, context.getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //赞
    public void likeCaodian() {
        try {
            AVUser currentUser = AVUser.getCurrentUser();
            if (currentUser != null) {
                AVRelation<AVObject> relation = item.getRelation(Caodian.likes);
                relation.add(currentUser);
                item.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            aq.id(R.id.btn_add).background(R.drawable.corner_all_light_blue_strok).enabled(false);
                        } else {
                            aq.id(R.id.btn_add).background(R.drawable.corner_all_white2_strok).enabled(true);
                            Toast.makeText(context, context.getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //分享
    private void share() {
        try {
            AVFile caodian_video = item.getAVFile(Caodian.caodian_video);
            if (caodian_video != null) {
                AVFile video_thumbnail = item.getAVFile(Caodian.caodian_video_thumbnail);
                if (video_thumbnail != null) {
                    final String video_thumbnail_img = video_thumbnail.getUrl();
                    if (video_thumbnail_img != null && video_thumbnail_img.length() != 0) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Bitmap bitmap = BitmapFactory.decodeStream(getImageStream(video_thumbnail_img));
                                    image_url = CommonUtility.savePhoto(bitmap);
                                    bitmap = CommonUtility.compressBitmap(image_url, bitmap);
                                    image_url = CommonUtility.savePhoto(bitmap);

                                    handler.sendMessage(handler.obtainMessage());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
            } else {
                final AVFile img1 = item.getAVFile(Caodian.img1);
                if (img1 != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Bitmap bitmap = BitmapFactory.decodeStream(getImageStream(img1.getUrl()));
                                image_url = CommonUtility.savePhoto(bitmap);
                                bitmap = CommonUtility.compressBitmap(image_url, bitmap);
                                image_url = CommonUtility.savePhoto(bitmap);

                                handler.sendMessage(handler.obtainMessage());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InputStream getImageStream(String path) throws Exception {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return conn.getInputStream();
        }
        return null;
    }

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

