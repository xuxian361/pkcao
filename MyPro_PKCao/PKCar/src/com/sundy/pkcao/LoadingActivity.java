package com.sundy.pkcao;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import com.sundy.pkcao.taker.CommonUtility;

/**
 * Created by sundy on 15/4/23.
 */
public class LoadingActivity extends _AbstractActivity {

    private final String TAG = "LoadingActivity";
    private final int GO_MAIN = 998;
    private long SPLASH_DELAY_MILLIS = 1500;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GO_MAIN:
                    goMain();
                    break;
            }
        }
    };

    public LoadingActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);

        //启动百度推送Service
        startBaiduPush(this);

        rtLog(TAG, "------------>onCreate");
        init();
    }

    private void init() {

        //屏幕的信息
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        CommonUtility.SCREEN_WIDTH = metrics.widthPixels;
        CommonUtility.SCREEN_HEIGHT = metrics.heightPixels;
        CommonUtility.SCREEN_DENSITY = metrics.density;

        handler.sendEmptyMessageDelayed(GO_MAIN, SPLASH_DELAY_MILLIS);
    }

    private void goMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
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
