package com.sundy.pkcao.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.androidquery.AQuery;
import com.androidquery.util.Common;
import com.avos.avoscloud.*;
import com.sundy.pkcao.R;
import com.sundy.pkcao._AbstractFragment;
import com.sundy.pkcao.adapters.CaoListAdapter;
import com.sundy.pkcao.caodian.AddCaoDianFragment;
import com.sundy.pkcao.caodian.CaoDetailFragment;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.tools.xlistview.XListView;
import com.sundy.pkcao.vo.Caodian;
import com.sundy.pkcao.vo.Caodian_Img;
import com.sundy.pkcao.vo.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sundy on 15/3/21.
 */
public class MainFragment extends _AbstractFragment {

    private final String TAG = "MainFragment";
    private Fragment fragment;
    private View v;
    private XListView lv_main;
    private CaoListAdapter adapter;
    private LinearLayout linear_filter;
    private boolean isFilterLeftVisible = false;
    private boolean isFilterRightVisible = false;

    private List list = new ArrayList();
    private int curPage = 1;
    private int pageNum = 10;
    private boolean ishasMore = true;
    private boolean isRefreshing = false;
    private String last_updated_time = "";

    public MainFragment() {
    }

    public MainFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        v = inflater.inflate(R.layout.main, container, false);
        aq = new AQuery(v);

        init();
        return v;
    }

    private void init() {
        last_updated_time = getString(R.string.just_now);
        lv_main = (XListView) aq.id(R.id.lv_main).getListView();
        adapter = new CaoListAdapter(context, inflater);
        lv_main.setAdapter(adapter);
        lv_main.setOnItemClickListener(onItemClickListener);
        lv_main.setPullLoadEnable(true);
        lv_main.setPullRefreshEnable(true);
        lv_main.setXListViewListener(ixListViewListener);

        linear_filter = (LinearLayout) aq.id(R.id.linear_filter).getView();

        aq.id(R.id.btn_filter_left).clicked(onClick);
        aq.id(R.id.btn_filter_right).clicked(onClick);
        aq.id(R.id.btnAdd).clicked(onClick);

        if (list != null)
            list.clear();

        getCaodians();
    }

    private void onLoad() {
        lv_main.stopRefresh();
        lv_main.stopLoadMore();
        lv_main.setRefreshTime(last_updated_time);
    }

    private XListView.IXListViewListener ixListViewListener = new XListView.IXListViewListener() {
        @Override
        public void onRefresh() {
            ishasMore = true;
            if (isRefreshing)
                return;
            if (list != null)
                list.clear();
            lv_main.setAdapter(adapter);
            curPage = 1;
            last_updated_time = CommonUtility.getLastUpdatedTime();
            getCaodians();
            onLoad();
        }

        @Override
        public void onLoadMore() {
            isRefreshing = false;
            if (ishasMore) {
                if (list.size() / pageNum == curPage - 1)
                    return;
                curPage++;
                getCaodians();
            }
            onLoad();
        }
    };

    private void getCaodians() {
        mCallback.onLoading();
//        AVQuery<AVObject> query = new AVQuery<AVObject>(Caodian.table_name);
//        query.setLimit(pageNum);
//        query.orderByDescending(Caodian.createdAt);
//
//        if (curPage > 1) {
//            query.setSkip((curPage - 1) * pageNum);
//        }
//        isRefreshing = true;
//        query.findInBackground(new FindCallback<AVObject>() {
//            @Override
//            public void done(List<AVObject> objects, AVException e) {
//        mCallback.finishLoading();
//        isRefreshing = false;
//        onLoad();
//        if (objects != null && objects.size() != 0) {
//            for (int i = 0; i < objects.size(); i++) {
//                AVObject item = objects.get(i);
//                if (item != null) {
//                    list.add(item);         ////----来到这里了，加油啊，你妹
//                }
//            }
//            if (list.size() % pageNum != 0) {
//                ishasMore = false;
//            }
//            adapter.setData(list);
//            adapter.notifyDataSetChanged();
//        } else {
//            ishasMore = false;
//            lv_main.setFooterViewText(getString(R.string.no_result));
//        }
//            }
//        });
        //-------
        final AVQuery<AVObject> caoidan_img = AVQuery.getQuery(Caodian_Img.table_name);
        caoidan_img.orderByDescending(Caodian_Img.createdAt);
        caoidan_img.setLimit(pageNum);
        caoidan_img.include(Caodian_Img.caodian);
        if (curPage > 1) {
            caoidan_img.setSkip((curPage - 1) * pageNum);
        }
        caoidan_img.findInBackground(new FindCallback<AVObject>() {
            public void done(List<AVObject> avObjectList, AVException e) {
                mCallback.finishLoading();
                isRefreshing = false;
                onLoad();
                try {
                    if (e == null) {
                        if (avObjectList != null && avObjectList.size() != 0) {
                            for (AVObject item : avObjectList) {
                                if (item != null) {
                                    AVObject caodian = item.getAVObject(Caodian_Img.caodian);
                                    if (caodian != null) {
                                        rtLog(TAG, "----------->caodiao = " + caodian);

                                        //
                                        04-14 18:10:11.066  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552ce1fee4b01e374fa9e68c, updatedAt=2015-04-14T09:46:38.308Z, createdAt=2015-04-14T09:46:38.308Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@4194e498, caodian_id=com.avos.avoscloud.AVKeyValues@4194f6f0, title=com.avos.avoscloud.AVKeyValues@41950048, creater_id=com.avos.avoscloud.AVKeyValues@419509a0, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@4195f548, caodian_video=com.avos.avoscloud.AVKeyValues@41957778}]
                                        04-14 18:10:11.067  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552ce1fee4b01e374fa9e68c, updatedAt=2015-04-14T09:46:38.308Z, createdAt=2015-04-14T09:46:38.308Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@41969308, caodian_id=com.avos.avoscloud.AVKeyValues@4196a560, title=com.avos.avoscloud.AVKeyValues@4196aeb8, creater_id=com.avos.avoscloud.AVKeyValues@4196b810, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@419798f0, caodian_video=com.avos.avoscloud.AVKeyValues@41971b20}]
                                        04-14 18:10:11.068  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552ce1fee4b01e374fa9e68c, updatedAt=2015-04-14T09:46:38.308Z, createdAt=2015-04-14T09:46:38.308Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@419836b0, caodian_id=com.avos.avoscloud.AVKeyValues@41984908, title=com.avos.avoscloud.AVKeyValues@41985260, creater_id=com.avos.avoscloud.AVKeyValues@41985bb8, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@41993c98, caodian_video=com.avos.avoscloud.AVKeyValues@4198bec8}]
                                        04-14 18:10:11.068  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552ce1fee4b01e374fa9e68c, updatedAt=2015-04-14T09:46:38.308Z, createdAt=2015-04-14T09:46:38.308Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@4199da58, caodian_id=com.avos.avoscloud.AVKeyValues@4199ecb0, title=com.avos.avoscloud.AVKeyValues@4199f608, creater_id=com.avos.avoscloud.AVKeyValues@4199ff60, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@419ae040, caodian_video=com.avos.avoscloud.AVKeyValues@419a6270}]
                                        04-14 18:10:11.069  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552cd5f1e4b01e374fa96970, updatedAt=2015-04-14T08:55:13.530Z, createdAt=2015-04-14T08:55:13.530Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@419b7e00, caodian_id=com.avos.avoscloud.AVKeyValues@419b9058, title=com.avos.avoscloud.AVKeyValues@419b99b0, creater_id=com.avos.avoscloud.AVKeyValues@419ba308, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@419c83e8, caodian_video=com.avos.avoscloud.AVKeyValues@419c0618}]
                                        04-14 18:10:11.069  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552cd5f1e4b01e374fa96970, updatedAt=2015-04-14T08:55:13.530Z, createdAt=2015-04-14T08:55:13.530Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@419d21a8, caodian_id=com.avos.avoscloud.AVKeyValues@419d3400, title=com.avos.avoscloud.AVKeyValues@419d3d58, creater_id=com.avos.avoscloud.AVKeyValues@419d46b0, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@419e2790, caodian_video=com.avos.avoscloud.AVKeyValues@419da9c0}]
                                        04-14 18:10:11.070  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552cd40fe4b01e374fa95268, updatedAt=2015-04-14T08:47:11.929Z, createdAt=2015-04-14T08:47:11.929Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@419ec550, caodian_id=com.avos.avoscloud.AVKeyValues@419ed7a8, title=com.avos.avoscloud.AVKeyValues@419ee100, creater_id=com.avos.avoscloud.AVKeyValues@419eea58, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@419fcb38, caodian_video=com.avos.avoscloud.AVKeyValues@419f4d68}]
                                        04-14 18:10:11.070  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552cd393e4b01e374fa94eb3, updatedAt=2015-04-14T08:45:07.629Z, createdAt=2015-04-14T08:45:07.629Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@41a068f8, caodian_id=com.avos.avoscloud.AVKeyValues@41a07b50, title=com.avos.avoscloud.AVKeyValues@41a084a8, creater_id=com.avos.avoscloud.AVKeyValues@41a08e00, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@41a10b98}]
                                        04-14 18:10:11.071  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552cd375e4b01e374fa94dca, updatedAt=2015-04-14T08:44:37.445Z, createdAt=2015-04-14T08:44:37.445Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@41a1a990, caodian_id=com.avos.avoscloud.AVKeyValues@41a1bbe8, title=com.avos.avoscloud.AVKeyValues@41a1c540, creater_id=com.avos.avoscloud.AVKeyValues@41a1ce98, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@41a2af78, caodian_video=com.avos.avoscloud.AVKeyValues@41a231a8}]
                                        04-14 18:10:11.071  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552cd258e4b01e374fa9459d, updatedAt=2015-04-14T08:39:52.620Z, createdAt=2015-04-14T08:39:52.620Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@41a34d38, caodian_id=com.avos.avoscloud.AVKeyValues@41948e28, title=com.avos.avoscloud.AVKeyValues@41946918, creater_id=com.avos.avoscloud.AVKeyValues@419444d0, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@418d7630}]
                                        04-14 18:10:18.452  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552ce1fee4b01e374fa9e68c, updatedAt=2015-04-14T09:46:38.308Z, createdAt=2015-04-14T09:46:38.308Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@419869b8, caodian_id=com.avos.avoscloud.AVKeyValues@41987bf8, title=com.avos.avoscloud.AVKeyValues@41988550, creater_id=com.avos.avoscloud.AVKeyValues@41988ea8, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@419a0a68, caodian_video=com.avos.avoscloud.AVKeyValues@41991740}]
                                        04-14 18:10:18.453  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552ce1fee4b01e374fa9e68c, updatedAt=2015-04-14T09:46:38.308Z, createdAt=2015-04-14T09:46:38.308Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@419acdb0, caodian_id=com.avos.avoscloud.AVKeyValues@419b0688, title=com.avos.avoscloud.AVKeyValues@419b0fe0, creater_id=com.avos.avoscloud.AVKeyValues@419b1938, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@419c6e78, caodian_video=com.avos.avoscloud.AVKeyValues@419bcb20}]
                                        04-14 18:10:18.453  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552ce1fee4b01e374fa9e68c, updatedAt=2015-04-14T09:46:38.308Z, createdAt=2015-04-14T09:46:38.308Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@419d8180, caodian_id=com.avos.avoscloud.AVKeyValues@419d93d8, title=com.avos.avoscloud.AVKeyValues@419d9d30, creater_id=com.avos.avoscloud.AVKeyValues@419da688, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@419f2240, caodian_video=com.avos.avoscloud.AVKeyValues@419e55a0}]
                                        04-14 18:10:18.454  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552ce1fee4b01e374fa9e68c, updatedAt=2015-04-14T09:46:38.308Z, createdAt=2015-04-14T09:46:38.308Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@41a00c00, caodian_id=com.avos.avoscloud.AVKeyValues@41a01e58, title=com.avos.avoscloud.AVKeyValues@41a027b0, creater_id=com.avos.avoscloud.AVKeyValues@41a03108, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@41a1f0c0, caodian_video=com.avos.avoscloud.AVKeyValues@41a0fd70}]
                                        04-14 18:10:18.454  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552cd5f1e4b01e374fa96970, updatedAt=2015-04-14T08:55:13.530Z, createdAt=2015-04-14T08:55:13.530Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@41a2da98, caodian_id=com.avos.avoscloud.AVKeyValues@41a2ecf0, title=com.avos.avoscloud.AVKeyValues@41a2f648, creater_id=com.avos.avoscloud.AVKeyValues@41a2ffa0, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@41a49190, caodian_video=com.avos.avoscloud.AVKeyValues@41a413c0}]
                                        04-14 18:10:18.455  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552cd5f1e4b01e374fa96970, updatedAt=2015-04-14T08:55:13.530Z, createdAt=2015-04-14T08:55:13.530Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@41a52f50, caodian_id=com.avos.avoscloud.AVKeyValues@41a541a8, title=com.avos.avoscloud.AVKeyValues@41a54b00, creater_id=com.avos.avoscloud.AVKeyValues@41a55458, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@41a63538, caodian_video=com.avos.avoscloud.AVKeyValues@41a5b768}]
                                        04-14 18:10:18.455  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552cd40fe4b01e374fa95268, updatedAt=2015-04-14T08:47:11.929Z, createdAt=2015-04-14T08:47:11.929Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@41a6d2f8, caodian_id=com.avos.avoscloud.AVKeyValues@41a6e550, title=com.avos.avoscloud.AVKeyValues@41a6eea8, creater_id=com.avos.avoscloud.AVKeyValues@41a6f800, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@41a7d8e0, caodian_video=com.avos.avoscloud.AVKeyValues@41a75b10}]
                                        04-14 18:10:18.456  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552cd393e4b01e374fa94eb3, updatedAt=2015-04-14T08:45:07.629Z, createdAt=2015-04-14T08:45:07.629Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@41a876a0, caodian_id=com.avos.avoscloud.AVKeyValues@41a888f8, title=com.avos.avoscloud.AVKeyValues@41a89250, creater_id=com.avos.avoscloud.AVKeyValues@41a89ba8, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@41a91940}]
                                        04-14 18:10:18.456  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552cd375e4b01e374fa94dca, updatedAt=2015-04-14T08:44:37.445Z, createdAt=2015-04-14T08:44:37.445Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@41978db0, caodian_id=com.avos.avoscloud.AVKeyValues@41974c40, title=com.avos.avoscloud.AVKeyValues@4196fe10, creater_id=com.avos.avoscloud.AVKeyValues@4196d980, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@418c5b70, caodian_video=com.avos.avoscloud.AVKeyValues@418db678}]
                                        04-14 18:10:18.457  24009-24009/com.sundy.pkcao I/MainFragment﹕ ----------->caodiao = AVObject [className=PkCaoDian, objectId=552cd258e4b01e374fa9459d, updatedAt=2015-04-14T08:39:52.620Z, createdAt=2015-04-14T08:39:52.620Z, uuid=null, fetchWhenSave=false, keyValues={content=com.avos.avoscloud.AVKeyValues@41901ed8, caodian_id=com.avos.avoscloud.AVKeyValues@41767900, title=com.avos.avoscloud.AVKeyValues@419748c8, creater_id=com.avos.avoscloud.AVKeyValues@417ddab0, caodian_video_thumbnail=com.avos.avoscloud.AVKeyValues@417bfab8}]



                                        list.add(item);
                                    }
                                }
                            }
                            if (list.size() % pageNum != 0) {
                                ishasMore = false;
                            }
                            adapter.setData(list);
                            adapter.notifyDataSetChanged();
                        } else {
                            ishasMore = false;
                            lv_main.setFooterViewText(getString(R.string.no_result));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_filter_left:
                    if (linear_filter != null)
                        linear_filter.removeAllViews();
                    if (isFilterLeftVisible == false) {
                        View filter_left = inflater.inflate(R.layout.view_filter_left, null);
                        AQuery aq1 = new AQuery(filter_left);
                        aq1.id(R.id.btn_filter_mine).clicked(onClick);
                        aq1.id(R.id.btn_filter_other).clicked(onClick);
                        aq1.id(R.id.btn_filter_all).clicked(onClick);
                        linear_filter.addView(filter_left);
                        isFilterLeftVisible = true;
                    } else {
                        isFilterLeftVisible = false;
                    }
                    isFilterRightVisible = false;
                    break;
                case R.id.btn_filter_right:
                    if (linear_filter != null)
                        linear_filter.removeAllViews();
                    if (isFilterRightVisible == false) {
                        View filter_right = inflater.inflate(R.layout.view_filter_right, null);
                        AQuery aq1 = new AQuery(filter_right);
                        aq1.id(R.id.btn_filter_latest).clicked(onClick);
                        aq1.id(R.id.btn_filter_hot).clicked(onClick);
                        linear_filter.addView(filter_right);
                        isFilterRightVisible = true;
                    } else {
                        isFilterRightVisible = false;
                    }
                    isFilterLeftVisible = false;
                    break;
                case R.id.btn_filter_all:
                    if (linear_filter != null)
                        linear_filter.removeAllViews();
                    isFilterLeftVisible = false;
                    aq.id(R.id.btn_filter_left).text("全部");

                    break;
                case R.id.btn_filter_mine:
                    if (linear_filter != null)
                        linear_filter.removeAllViews();
                    isFilterLeftVisible = false;
                    aq.id(R.id.btn_filter_left).text("我的");

                    break;
                case R.id.btn_filter_other:
                    if (linear_filter != null)
                        linear_filter.removeAllViews();
                    isFilterLeftVisible = false;
                    aq.id(R.id.btn_filter_left).text("其他人");

                    break;
                case R.id.btn_filter_hot:
                    if (linear_filter != null)
                        linear_filter.removeAllViews();
                    isFilterRightVisible = false;
                    aq.id(R.id.btn_filter_right).text("最热");
                    break;
                case R.id.btn_filter_latest:
                    if (linear_filter != null)
                        linear_filter.removeAllViews();
                    isFilterRightVisible = false;
                    aq.id(R.id.btn_filter_right).text("最新");
                    break;
                case R.id.btnAdd:
                    if (CommonUtility.isLogin(context)) {
                        mCallback.addContent(new AddCaoDianFragment(MainFragment.this));
                    } else {
                        Toast.makeText(context, getString(R.string.please_login), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (list != null && list.size() != 0) {
                AVObject item = (AVObject) list.get(i - 1);
                mCallback.addContent(new CaoDetailFragment(MainFragment.this, item));
            }
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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

