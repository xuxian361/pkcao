package com.sundy.pkcao.activitys;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.avos.avoscloud.*;
import com.sundy.pkcao.R;
import com.sundy.pkcao.adapters.CommentsAdapter;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.tools.EmotionEditText;
import com.sundy.pkcao.tools.ProgressWheel;
import com.sundy.pkcao.tools.SimpleTextWatcher;
import com.sundy.pkcao.tools.xlistview.XListView;
import com.sundy.pkcao.vo.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sundy on 16/2/27.
 */
public class CommentsActivity extends _AbstractActivity {

    private final String TAG = "CommentsActivity";
    private XListView xListView;
    private ProgressWheel progressbar;
    private CommentsAdapter adapter;
    private List commentsList = new ArrayList();
    private int curPage = 1;
    private int pageNum = 10;
    private boolean ishasMore = true;
    private boolean isRefreshing = false;
    private String last_updated_time = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        init();
    }

    private void init() {
        aq.id(R.id.txt_header_title).text(getString(R.string.detail));
        progressbar = (ProgressWheel) aq.id(R.id.progressbar).getView();
        last_updated_time = getString(R.string.just_now);
        aq.id(R.id.sendBtn).clicked(onClick);

//        contentEdit = (EmotionEditText) aq.id(R.id.textEdit).getView();
//        contentEdit.setOnClickListener(onClick);
//        contentEdit.addTextChangedListener(new SimpleTextWatcher() {
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() > 0) {
//                    aq.id(R.id.sendBtn).enabled(true);
//                } else {
//                    aq.id(R.id.sendBtn).enabled(false);
//                }
//                super.onTextChanged(s, start, before, count);
//            }
//        });

        if (commentsList != null)
            commentsList.clear();

        xListView = (XListView) aq.id(R.id.listview).getView();
        xListView.setPullRefreshEnable(true);
        xListView.setPullLoadEnable(true);
        xListView.setXListViewListener(ixListViewListener);
        adapter = new CommentsAdapter(this, getLayoutInflater());
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

    //获取Comments
    private void getComments() {
//        showProgress(progressbar);
//        AVQuery<AVObject> query = AVQuery.getQuery(Comment.table_name);
//        query.orderByDescending(Comment.createdAt);
//        query.whereEqualTo(Comment.post, item);
//        query.include(Comment.author);
//        int skip = (curPage - 1) * pageNum;
//        query.setSkip(skip);
//        query.setLimit(pageNum);
//        isRefreshing = true;
//
//        query.findInBackground(new FindCallback<AVObject>() {
//            @Override
//            public void done(List<AVObject> comments, AVException e) {
//                stopProgress(progressbar);
//                isRefreshing = false;
//                onLoad();
//                try {
//                    if (e == null) {
//                        if (comments != null && comments.size() != 0) {
//                            for (int i = 0; i < comments.size(); i++) {
//                                commentsList.add(comments.get(i));
//                            }
//                            if (commentsList.size() % pageNum != 0) {
//                                ishasMore = false;
//                            }
//
//                            adapter.setData(commentsList);
//                            adapter.notifyDataSetChanged();
//                            scrollToLast();
//                        } else {
//                            ishasMore = false;
//                            xListView.setFooterViewText(getString(R.string.no_result));
//                        }
//                    } else {
//                        ishasMore = false;
//                        Toast.makeText(context, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
//                    }
//                } catch (Exception e1) {
//                    e1.printStackTrace();
//                }
//            }
//        });
    }

    //提交Comments
    private void commitComments() {
        try {
//            String content = contentEdit.getText().toString().trim();
//            if (content.length() == 0)
//                return;
//            showProgress(progressbar);
//            AVUser currentUser = AVUser.getCurrentUser();
//            if (currentUser != null) {
//                //创建一个评论对象
//                AVObject comment = new AVObject(Comment.table_name);
//                comment.put(Comment.content, content);
//                comment.put(Comment.post, item);
//                comment.put(Comment.author, currentUser);
//                comment.saveInBackground(new SaveCallback() {
//                    @Override
//                    public void done(AVException e) {
//                        stopProgress(progressbar);
//                        if (e == null) {
//                            Toast.makeText(context, getString(R.string.send_success), Toast.LENGTH_SHORT).show();
//                            contentEdit.setText("");
//                            ishasMore = true;
//                            if (isRefreshing)
//                                return;
//                            if (commentsList != null)
//                                commentsList.clear();
//                            xListView.setAdapter(adapter);
//                            curPage = 1;
//                            last_updated_time = CommonUtility.getLastUpdatedTime();
//
//                            getComments();
//                            onLoad();
//                        } else {
//                            Toast.makeText(context, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.textEdit:
                    scrollToLast();
                    break;
            }
        }
    };

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

    private void showProgress(ProgressWheel progressWheel) {
        if (progressWheel != null) {
            progressWheel.setVisibility(View.VISIBLE);
            if (progressWheel.isSpinning) {
                progressWheel.stopSpinning();
            }
            progressWheel.spin();
        }
    }

    private void stopProgress(ProgressWheel progressWheel) {
        if (progressWheel != null) {
            progressWheel.setVisibility(View.GONE);
            progressWheel.stopSpinning();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressbar != null)
            progressbar = null;
    }
}
