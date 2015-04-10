package com.sundy.pkcao.taker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import com.sundy.pkcao.vo.User;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sundy on 15/3/21.
 */
public class CommonUtility {

    private static String TAG = "CommonUtility";
    public static String APP_NAME = "PKCao";
    public static int SCREEN_WIDTH = 720;
    public static int SCREEN_HEIGHT = 1080;
    public static float SCREEN_DENSITY = DisplayMetrics.DENSITY_HIGH;


    public static Date parseString2Date(String dateStr) {
        Date date = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = dateFormat.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String formatDate2String(Date date) {
        String str = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            str = format.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static boolean isLogin(Activity context) {
        boolean isLogin = false;
        SharedPreferences preferences = context.getSharedPreferences(CommonUtility.APP_NAME, Context.MODE_PRIVATE);
        String objectId = preferences.getString(User.objectId, "");
        if (objectId != null && objectId.length() != 0)
            isLogin = true;
        return isLogin;
    }

}
