package com.sundy.pkcao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import com.androidquery.AQuery;
import com.avos.avoscloud.AVAnalytics;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.tools.OperationFileHelper;
import com.sundy.pkcao.tools.xlistview.XListView;

import java.io.File;

/**
 * Created by sundy on 15/4/26.
 */
public class ChatActivity extends _AbstractActivity implements XListView.IXListViewListener {

    private final String TAG = "ChatActivity";
    private XListView listview;

    public ChatActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        rtLog(TAG, "------------>onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_view);

        aq = new AQuery(this);
        init();
    }

    private void init() {
        aq.id(R.id.btnBack).clicked(onClick);

        listview = (XListView) aq.id(R.id.listview).getView();
        listview.setPullRefreshEnable(true);
        listview.setPullLoadEnable(false);
        listview.setXListViewListener(this);

    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnBack:
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {

    }
}
