package com.sundy.pkcao;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.sundy.pkcao.baidupush.Utils;
import com.sundy.pkcao.taker.ResourceTaker;

/**
 * Created by sundy on 15/3/21.
 */
public class _AbstractActivity extends Activity {

    public AQuery aq;
    private final String TAG = "_AbstractActivity";

    public _AbstractActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        rtLog(TAG, "------------>onCreate");
        super.onCreate(savedInstanceState);
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
        rtLog(TAG, "------------>onDestroy");
        super.onDestroy();
        if (isTaskRoot()) {
            AQUtility.cleanCacheAsync(this);
        }
    }

    // 以apikey的方式绑定(baidu push)
    public void startBaiduPush(Activity context) {
        PushManager.startWork(context,
                PushConstants.LOGIN_TYPE_API_KEY,
                Utils.getMetaValue(context, "api_key"));
    }

    //停止Baidu 推送
    public void stopBaiduPush(Activity context) {
        PushManager.stopWork(context);
    }

    public void rtLog(String tag, String msg) {
        if (ResourceTaker.isDebug)
            Log.i(tag, msg);
    }

}
