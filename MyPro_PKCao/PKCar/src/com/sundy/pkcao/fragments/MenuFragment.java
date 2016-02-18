package com.sundy.pkcao.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.androidquery.AQuery;
import com.avos.avoscloud.*;
import com.sundy.pkcao.R;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.taker.ResourceTaker;
import com.sundy.pkcao.vo.User;

/**
 * Created by sundy on 15/3/21.
 */
public class MenuFragment extends Fragment {

    private final String TAG = "MenuFragment";
    private AQuery aq;
    private LayoutInflater layoutinflater;
    private View v;
    private OnListListener mCallback;
    public OnListListener MenuCallBack;
    protected FragmentActivity context;
    public int curRadioId = R.id.btn_main;
    private _AbstractFragment main, i_like, ta_like, record;
    private SharedPreferences preferences;

    public MenuFragment() {
    }

    public MenuFragment(int radioId) {
        if (radioId > 0)
            curRadioId = radioId;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnListListener) (context = (FragmentActivity) activity);
            MenuCallBack = mCallback;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.layoutinflater = inflater;
        v = layoutinflater.inflate(R.layout.menu, container, false);
        aq = new AQuery(v);

        init();

        return v;
    }

    private void init() {
        try {
            if (mCallback == null)
                return;
            mCallback.switchContent(getFragmentItem(curRadioId));

            //replace fragment
            aq.id(R.id.btn_main).clicked(onClickListener);
            aq.id(R.id.btn_login).clicked(onClickListener);
            aq.id(R.id.btn_logout).clicked(onClickListener);
            aq.id(R.id.btn_I_like).clicked(onClickListener);
            aq.id(R.id.btn_TA_like).clicked(onClickListener);
            aq.id(R.id.btn_record).clicked(onClickListener);

            preferences = context.getSharedPreferences(CommonUtility.APP_NAME, Context.MODE_PRIVATE);

            refreshUserInfo();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //更新用户状态
    private void refreshUserInfo() {
        if (CommonUtility.isLogin(context)) {
            getUserInfo();
        } else {
            hideUserInfo();
        }
    }

    //获取用户信息
    private void getUserInfo() {
        AVUser currentUser = AVUser.getCurrentUser();
        if (currentUser != null) {
            showUserInfo(currentUser);
            saveUserInfo(currentUser);
        } else {
            hideUserInfo();
        }
    }

    //隐藏用户
    private void hideUserInfo() {
        aq.id(R.id.btn_logout).gone();
        aq.id(R.id.btn_login).visible();
        aq.id(R.id.txt_name).invisible();
        aq.id(R.id.img_profile).image(R.drawable.logo).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.addContent(new LoginFragment());
            }
        });
    }

    //显示用户
    private void showUserInfo(AVObject user) {
        try {
            aq.id(R.id.btn_logout).visible();
            aq.id(R.id.btn_login).gone();
            String username = user.getString(User.username);
            if (username != null && username.length() != 0)
                aq.id(R.id.txt_name).visible().text(username);
            else
                aq.id(R.id.txt_name).invisible();
            AVFile img_file = user.getAVFile(User.user_img);
            String user_img = img_file.getUrl();
            if (user_img != null && user_img.length() != 0)
                aq.id(R.id.img_profile).image(user_img).clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCallback.addContent(new EditUserFragment());
                    }
                });
            else {
                aq.id(R.id.img_profile).image(R.drawable.logo).clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCallback.addContent(new LoginFragment());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int id = v.getId();
            Fragment fragment = getFragmentItem(id);

            if (fragment == null)
                return;
            if (mCallback != null)
                mCallback.switchContent(fragment);
        }
    };

    private Fragment getFragmentItem(int index) {
        Fragment fragment = null;
        switch (index) {
            case R.id.btn_main:
                if (main == null)
                    main = new MainFragment(MenuFragment.this);
                fragment = main;
                curRadioId = index;

                break;
            case R.id.btn_login:
                mCallback.addContent(new LoginFragment());
                break;
            case R.id.btn_logout:
                logout();
                break;
            case R.id.btn_I_like:
                if (CommonUtility.isLogin(context)) {
                    if (i_like == null)
                        i_like = new ILikeFragment(MenuFragment.this);
                    fragment = i_like;
                    curRadioId = index;
                } else {
                    mCallback.addContent(new LoginFragment());
                }
                break;
            case R.id.btn_TA_like:
                if (CommonUtility.isLogin(context)) {
                    if (ta_like == null)
                        ta_like = new TaLikeFragment(MenuFragment.this);
                    fragment = ta_like;
                    curRadioId = index;
                } else {
                    mCallback.addContent(new LoginFragment());
                }
                break;
            case R.id.btn_record:
                if (CommonUtility.isLogin(context)) {
                    if (record == null)
                        record = new RecordFragment(MenuFragment.this);
                    fragment = record;
                    curRadioId = index;
                } else {
                    mCallback.addContent(new LoginFragment());
                }
                break;
        }
        return fragment;
    }

    //登出
    private void logout() {
        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_ok_cancel, null);
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(view);
        AQuery aq = new AQuery(view);
        aq.id(R.id.btn_ok).text(getString(R.string.confrim));
        aq.id(R.id.btn_done).text(getString(R.string.cancel));
        aq.id(R.id.txt_msg).text(getString(R.string.confirm_logout));
        aq.id(R.id.btn_done).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog.cancel();
            }
        });
        aq.id(R.id.btn_ok).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog.cancel();
                clearUserInfo();
                hideUserInfo();
                main = new MainFragment(MenuFragment.this);
                curRadioId = R.id.btn_main;
                mCallback.switchContent(main);
            }
        });
        dialog.show();
    }

    //保存用户信息
    private void saveUserInfo(AVObject user) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(User.objectId, user.getObjectId());
        editor.putString(User.createdAt, CommonUtility.formatDate2String(user.getCreatedAt()));
        editor.putString(User.updatedAt, CommonUtility.formatDate2String(user.getUpdatedAt()));
        editor.putString(User.username, user.getString(User.username));
        AVFile file = user.getAVFile(User.user_img);
        if (file != null) {
            String user_img = file.getUrl();
            if (user_img != null)
                editor.putString(User.user_img, user_img);
            else
                editor.putString(User.user_img, "");
        } else {
            editor.putString(User.user_img, "");
        }
        String uuid = user.getUuid();
        if (uuid != null) {
            editor.putString(User.uuid, uuid);
        } else {
            editor.putString(User.uuid, "");
        }
        editor.commit();
    }

    //清除用户信息
    private void clearUserInfo() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        AVUser.logOut();
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

    public void onClick(View view) {
        if (mCallback == null)
            return;
        int id = view.getId();
        switch (id) {
            case R.id.btn_main:
                if (curRadioId == id) {
                    mCallback.showSlidingMenu();
                }
                break;
            case R.id.btnMenu:
                rtLog(TAG, "------------>btnMenu");
                refreshUserInfo();
                break;
        }
    }

    // Container Activity must implement this interface
    public interface OnListListener {
        public void onLoading();

        public void finishLoading();

        public void switchContent(Fragment fragment);

        public void addContent(Fragment fragment);

        public void showSlidingMenu();
    }

    private void rtLog(String tag, String msg) {
        if (ResourceTaker.isDebug) {
            Log.i(tag, msg);
        }
    }
}
