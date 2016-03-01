package com.sundy.pkcao.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
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
import com.sundy.pkcao.tools.DensityUtil;
import com.sundy.pkcao.vo.Caodian;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sundy on 15/3/21.
 */
public class CaoListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List list = new ArrayList();
    private String image_url;
    private AVObject current_item;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                if (current_item == null)
                    return;
                HashMap<String, String> data = new HashMap<String, String>();
                data.put("title", current_item.getString(Caodian.title));
                data.put("content", current_item.getString(Caodian.content));
                if (image_url != null && image_url.length() != 0)
                    data.put("img_url", image_url);
                CommonUtility.showShare(context, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private int Image_Height = CommonUtility.SCREEN_WIDTH * 16 / 9;

    public CaoListAdapter() {
    }

    public CaoListAdapter(Context context, LayoutInflater inflater) {
        this.context = context;
        this.inflater = inflater;

        Image_Height = DensityUtil.px2dip(context, Image_Height);
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
            holder.txt_content = aq.id(R.id.txt_content).getTextView();
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        try {
            final AVObject caodian = (AVObject) list.get(i);
            if (caodian != null) {
                holder.txt_title.setText(caodian.getString(Caodian.title));
                holder.txt_content.setText(caodian.getString(Caodian.content));

                AVRelation relation = caodian.getRelation(Caodian.likes);
                AVQuery<AVObject> query = relation.getQuery();
                final ViewHolder finalHolder = holder;
                query.findInBackground(new FindCallback<AVObject>() {
                    @Override
                    public void done(List<AVObject> nums, AVException e) {
                        try {
                            finalHolder.txt_count.setText("( " + nums.size() + "+ )");
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });

                ViewGroup.LayoutParams param = holder.img.getLayoutParams();
                param.height = Image_Height;
                holder.img.setLayoutParams(param);

                AQuery aq_img = new AQuery(holder.img);
                AQuery aq_img_play = new AQuery(holder.img_play);
                final AVFile video = caodian.getAVFile(Caodian.caodian_video);
                if (video != null) {
                    aq_img_play.visible();
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
                    } else {
                        aq_img.gone();
                    }
                }

                holder.btn_add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (CommonUtility.isLogin((Activity) context)) {
                        } else {
                            Toast.makeText(context, context.getString(R.string.please_login), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                holder.btn_share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        current_item = caodian;
                        AVFile caodian_video = current_item.getAVFile(Caodian.caodian_video);
                        if (caodian_video != null) {
                            AVFile video_thumbnail = current_item.getAVFile(Caodian.caodian_video_thumbnail);
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
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            handler.sendMessage(handler.obtainMessage());
                                        }
                                    }).start();
                                }
                            }
                        } else {
                            final AVFile img1 = current_item.getAVFile(Caodian.img1);
                            if (img1 != null) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Bitmap bitmap = BitmapFactory.decodeStream(getImageStream(img1.getUrl()));
                                            image_url = CommonUtility.savePhoto(bitmap);
                                            bitmap = CommonUtility.compressBitmap(image_url, bitmap);
                                            image_url = CommonUtility.savePhoto(bitmap);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        handler.sendMessage(handler.obtainMessage());
                                    }
                                }).start();
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
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

    class ViewHolder {
        TextView txt_title, txt_count, txt_content;
        ImageView img, img_play;
        TextView btn_add, btn_share;
    }
}
