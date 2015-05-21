package com.sundy.pkcao;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;
import com.avos.avoscloud.AVOSCloud;
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

        AVOSCloud.initialize(this,
                "f20bpd0qsoqvkjj79otbh09i5mk70gesw2kl5d7gv738jkon",
                "31hhp1ykz4czouw626n7asjmd5uvonsg0fx3p49wcbqq6no4");
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

    public void rtLog(String tag, String msg) {
        if (ResourceTaker.isDebug)
            Log.i(tag, msg);
    }

}
