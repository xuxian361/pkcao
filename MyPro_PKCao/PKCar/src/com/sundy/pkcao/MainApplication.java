package com.sundy.pkcao;

import android.app.Application;
import android.util.Log;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.BitmapAjaxCallback;
import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;
import com.baidu.frontia.FrontiaApplication;

/**
 * Created by sundy on 15/3/21.
 */
public class MainApplication extends FrontiaApplication {

    private final String TAG = "MainApplication";

    @Override
    public void onCreate() {
        Log.e(TAG, "------------>onCreate");
        //set the max number of concurrent network connections, default is 4
        AjaxCallback.setNetworkLimit(8);

        //set the max number of icons (image width <= 50) to be cached in memory, default is 20
        BitmapAjaxCallback.setIconCacheLimit(20);

        //set the max number of images (image width > 50) to be cached in memory, default is 20
        BitmapAjaxCallback.setCacheLimit(40);

        //set the max size of an image to be cached in memory, default is 1600 pixels (ie. 400x400)
        BitmapAjaxCallback.setPixelLimit(400 * 400);

        //set the max size of the memory cache, default is 1M pixels (4MB)
        BitmapAjaxCallback.setMaxPixelLimit(2000000);

        AVOSCloud.initialize(this,
                "f20bpd0qsoqvkjj79otbh09i5mk70gesw2kl5d7gv738jkon",
                "31hhp1ykz4czouw626n7asjmd5uvonsg0fx3p49wcbqq6no4");

        super.onCreate();
        FrontiaApplication.initFrontiaApplication(this);
    }

    @Override
    public void onLowMemory() {
        Log.e(TAG, "------------>onLowMemory");
        BitmapAjaxCallback.clearCache();
    }

}
