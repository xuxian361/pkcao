package com.sundy.pkcao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import com.androidquery.AQuery;
import com.avos.avoscloud.AVAnalytics;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.tools.OperationFileHelper;

import java.io.File;

/**
 * Created by sundy on 15/4/26.
 */
public class ChatActivity extends _AbstractActivity {

    private final String TAG = "ChatActivity";

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

    }

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


}
