package com.sundy.pkcao.caodian;

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
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.sundy.pkcao.ChatActivity;
import com.sundy.pkcao.R;
import com.sundy.pkcao.ScaleImageViewActivity;
import com.sundy.pkcao._AbstractFragment;
import com.sundy.pkcao.adapters.CaoListAdapter;
import com.sundy.pkcao.adapters.CommentsAdapter;
import com.sundy.pkcao.login.LoginFragment;
import com.sundy.pkcao.main.MainFragment;
import com.sundy.pkcao.service.CacheService;
import com.sundy.pkcao.taker.CommonUtility;
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

    private EmotionEditText contentEdit;
    private XListView xListView;
    private ProgressWheel progressbar;
    private CommentsAdapter adapter;
    private List commentsList = new ArrayList();
    private int curPage = 1;
    private int pageNum = 10;
    private boolean ishasMore = true;
    private boolean isRefreshing = false;
    private String last_updated_time = "";

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
        progressbar = (ProgressWheel) aq.id(R.id.progressbar).getView();
        last_updated_time = getString(R.string.just_now);

        aq.id(R.id.btn_share).clicked(onClick);
        aq.id(R.id.btn_add).clicked(onClick);
        aq.id(R.id.btn_delete).clicked(onClick);
        aq.id(R.id.btn_chat).clicked(onClick);
        aq.id(R.id.sendBtn).clicked(onClick);

        contentEdit = (EmotionEditText) aq.id(R.id.textEdit).getView();
        contentEdit.setOnClickListener(onClick);
        contentEdit.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    aq.id(R.id.sendBtn).enabled(true);
                } else {
                    aq.id(R.id.sendBtn).enabled(false);
                }
                super.onTextChanged(s, start, before, count);
            }
        });
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

        if (commentsList != null)
            commentsList.clear();

        xListView = (XListView) aq.id(R.id.listview).getView();
        xListView.setPullRefreshEnable(true);
        xListView.setPullLoadEnable(true);
        xListView.setXListViewListener(ixListViewListener);
        adapter = new CommentsAdapter(context, inflater);
        xListView.setAdapter(adapter);
    }

    private void onLoad() {
        xListView.stopRefresh();
        xListView.stopLoadMore();
        xListView.setRefreshTime(last_updated_time);
    }

    private XListView.IXListViewListener ixListViewListener = new XListView.IXListViewListener() {
        @Override
        public void onRefresh() {
            ishasMore = true;
            if (isRefreshing)
                return;
            if (commentsList != null)
                commentsList.clear();
            xListView.setAdapter(adapter);
            curPage = 1;
            last_updated_time = CommonUtility.getLastUpdatedTime();

            getComments();
            onLoad();
        }

        @Override
        public void onLoadMore() {
            isRefreshing = false;
            if (ishasMore) {
                if (commentsList.size() / pageNum == curPage - 1)
                    return;
                curPage++;
                getComments();
            }
            onLoad();
        }
    };

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
                case R.id.btn_chat:
                    ScrollView scrollView_detial = (ScrollView) aq.id(R.id.scrollView_detial).getView();
                    RelativeLayout relative_comments = (RelativeLayout) aq.id(R.id.relative_comments).getView();
                    int visibility = relative_comments.getVisibility();
                    if (visibility == View.VISIBLE) {
                        scrollView_detial.setVisibility(View.VISIBLE);
                        relative_comments.setVisibility(View.GONE);
                        aq.id(R.id.btn_chat).text(getString(R.string.show_comments));
                    } else {
                        scrollView_detial.setVisibility(View.GONE);
                        relative_comments.setVisibility(View.VISIBLE);
                        aq.id(R.id.btn_chat).text(getString(R.string.show_content));
                        getComments();
                    }
                    break;
                case R.id.textEdit:
                    scrollToLast();
                    break;
                case R.id.sendBtn:
                    commitComments();
                    break;
            }
        }
    };

    private void getComments() {
        showProgress(progressbar);
        AVQuery<AVObject> query = AVQuery.getQuery(Comment.table_name);
        query.orderByDescending(Comment.createdAt);
        query.whereEqualTo(Comment.post, item);
        query.include(Comment.author);
        int skip = (curPage - 1) * pageNum;
        query.setSkip(skip);
        query.setLimit(pageNum);
        isRefreshing = true;
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> comments, AVException e) {
                stoProgress(progressbar);
                isRefreshing = false;
                onLoad();
                try {
                    if (e == null) {
                        if (comments != null && comments.size() != 0) {
                            for (int i = 0; i < comments.size(); i++) {
                                commentsList.add(comments.get(i));
                            }
                            if (commentsList.size() % pageNum != 0) {
                                ishasMore = false;
                            }

                            adapter.setData(commentsList);
                            adapter.notifyDataSetChanged();
                            scrollToLast();
                        } else {
                            ishasMore = false;
                            xListView.setFooterViewText(getString(R.string.no_result));
                        }
                    } else {
                        ishasMore = false;
                        Toast.makeText(context, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void commitComments() {
        try {
            String content = contentEdit.getText().toString().trim();
            if (content.length() == 0)
                return;
            showProgress(progressbar);
            AVUser currentUser = AVUser.getCurrentUser();
            if (currentUser != null) {
                //创建一个评论对象
                AVObject comment = new AVObject(Comment.table_name);
                comment.put(Comment.content, content);
                comment.put(Comment.post, item);
                comment.put(Comment.author, currentUser);
                comment.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        stoProgress(progressbar);
                        if (e == null) {
                            Toast.makeText(context, getString(R.string.send_success), Toast.LENGTH_SHORT).show();
                            contentEdit.setText("");
                            ishasMore = true;
                            if (isRefreshing)
                                return;
                            if (commentsList != null)
                                commentsList.clear();
                            xListView.setAdapter(adapter);
                            curPage = 1;
                            last_updated_time = CommonUtility.getLastUpdatedTime();

                            getComments();
                            onLoad();
                        } else {
                            Toast.makeText(context, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scrollToLast() {
        try {
            xListView.post(new Runnable() {
                @Override
                public void run() {
                    xListView.smoothScrollToPosition(xListView.getAdapter().getCount() - 1);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    private void deleteCaodian() {
        item.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    mCallback.switchContent(new MainFragment());
                } else {

                }
            }
        });
    }

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
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        if (progressbar != null)
            progressbar = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

