package com.sundy.pkcao.caodian;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.androidquery.AQuery;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.sundy.pkcao.R;
import com.sundy.pkcao._AbstractFragment;
import com.sundy.pkcao.adapters.ImageHListAdapter;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.vo.Caodian;
import it.sephiroth.android.library.widget.HListView;

import java.util.HashMap;

/**
 * Created by sundy on 15/3/22.
 */
public class CaoDetailFragment extends _AbstractFragment {

    private final String TAG = "CaoDetailFragment";
    private AVObject item;
    private Fragment fragment;
    private View v;
    private ImageHListAdapter adapter;

    public CaoDetailFragment() {
    }

    public CaoDetailFragment(Fragment fragment, AVObject item) {
        this.fragment = fragment;
        this.item = item;
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
        showCaodian();
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

        LinearLayout linear_img = (LinearLayout) aq.id(R.id.linear_img).getView();
        linear_img.removeAllViews();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 10, 0, 0);

        AVFile img1 = item.getAVFile(Caodian.img1);
        AVFile img2 = item.getAVFile(Caodian.img2);
        AVFile img3 = item.getAVFile(Caodian.img3);
        AVFile img4 = item.getAVFile(Caodian.img4);
        AVFile img5 = item.getAVFile(Caodian.img5);

        if (img1 != null) {
            String img1_url = img1.getUrl();
            if (img1_url != null && img1_url.length() != 0) {
                ImageView v1 = new ImageView(context);
                AQuery aq_v1 = new AQuery(v1);
                aq_v1.image(img1_url);
                v1.setScaleType(ImageView.ScaleType.FIT_XY);
                v1.setLayoutParams(params);
                linear_img.addView(v1);
                linear_img.setVisibility(View.VISIBLE);
            }
        }
        if (img2 != null) {
            String img2_url = img2.getUrl();
            if (img2_url != null && img2_url.length() != 0) {
                ImageView v2 = new ImageView(context);
                AQuery aq_v2 = new AQuery(v2);
                aq_v2.image(img2_url);
                v2.setScaleType(ImageView.ScaleType.FIT_XY);
                v2.setLayoutParams(params);
                linear_img.addView(v2);
                linear_img.setVisibility(View.VISIBLE);
            }
        }
        if (img3 != null) {
            String img3_url = img3.getUrl();
            if (img3_url != null && img3_url.length() != 0) {
                ImageView v3 = new ImageView(context);
                AQuery aq_v3 = new AQuery(v3);
                aq_v3.image(img3_url);
                v3.setScaleType(ImageView.ScaleType.FIT_XY);
                v3.setLayoutParams(params);
                linear_img.addView(v3);
                linear_img.setVisibility(View.VISIBLE);
            }
        }
        if (img4 != null) {
            String img4_url = img4.getUrl();
            if (img4_url != null && img4_url.length() != 0) {
                ImageView v4 = new ImageView(context);
                AQuery aq_v4 = new AQuery(v4);
                aq_v4.image(img4_url);
                v4.setScaleType(ImageView.ScaleType.FIT_XY);
                v4.setLayoutParams(params);
                linear_img.addView(v4);
                linear_img.setVisibility(View.VISIBLE);
            }
        }
        if (img5 != null) {
            String img5_url = img5.getUrl();
            if (img5_url != null && img5_url.length() != 0) {
                ImageView v5 = new ImageView(context);
                AQuery aq_v5 = new AQuery(v5);
                aq_v5.image(img5_url);
                v5.setScaleType(ImageView.ScaleType.FIT_XY);
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
            }
        }
    };

    private void share() {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("title", item.getString(Caodian.title));
        data.put("content", item.getString(Caodian.content));
        AVFile caodian_video = item.getAVFile(Caodian.caodian_video);
        if (caodian_video != null) {
            AVFile video_thumbnail = item.getAVFile(Caodian.caodian_video_thumbnail);
            if (video_thumbnail != null) {
                String video_thumbnail_img = video_thumbnail.getUrl();
                if (video_thumbnail_img != null && video_thumbnail_img.length() != 0) {
                    data.put("img_url", video_thumbnail_img);
                } else {
                    AVFile img1 = item.getAVFile(Caodian.img1);
                    if (img1 != null) {
                        data.put("img_url", img1.getUrl());
                    }
                }
            }
        }
//                    data.put("url", "url");
        CommonUtility.showShare(context, data);
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

