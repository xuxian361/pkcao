package com.sundy.pkcao.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.androidquery.AQuery;
import com.avos.avoscloud.*;
import com.sundy.pkcao.R;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.tools.ProgressWheel;
import com.sundy.pkcao.vo.User;

/**
 * Created by sundy on 15/3/22.
 */
public class LoginFragment extends _AbstractFragment {

    private final String TAG = "LoginFragment";
    private Fragment fragment;
    private View v;
    private String username;
    private ProgressWheel progressbar;

    public LoginFragment() {
    }

    public LoginFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        v = inflater.inflate(R.layout.login, container, false);
        aq = new AQuery(v);

        init();
        return v;
    }

    private void init() {
        aq.id(R.id.txt_header_title).text(getString(R.string.user_login));
        aq.id(R.id.btn_register).clicked(onClick);
        aq.id(R.id.btn_login).clicked(onClick);

        progressbar = (ProgressWheel) aq.id(R.id.progressbar).getView();
    }


    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_register:
                    mCallback.addContent(new RegisterFragment());
                    break;
                case R.id.btn_login:
                    login();
                    break;
            }
        }
    };

    //登陆
    private void login() {
        username = aq.id(R.id.ext_name).getEditText().getText().toString().trim();
        String password = aq.id(R.id.edt_password).getEditText().getText().toString().trim();

        final TextView txt_login_problem = aq.id(R.id.txt_login_problem).getTextView();

        if (TextUtils.isEmpty(username)) {
            txt_login_problem.setText(getString(R.string.username_cannot_null));
            return;
        }
        if (username.length() > 16) {
            txt_login_problem.setText(getString(R.string.fill_username_format_wrong));
            return;
        }

        if (TextUtils.isEmpty(password)) {
            txt_login_problem.setText(getString(R.string.password_can_not_null));
            return;
        }
        if (password.length() > 16) {
            txt_login_problem.setText(getString(R.string.fill_password_format_wrong));
            return;
        }
        if (password.length() < 6) {
            txt_login_problem.setText(getString(R.string.fill_password_format_wrong));
            return;
        }

        txt_login_problem.setText("");
        showProgress(progressbar);
        AVUser.logInInBackground(username, password, new LogInCallback<AVUser>() {
            @Override
            public void done(AVUser avUser, AVException e) {
                stopProgress(progressbar);
                if (avUser != null) {
                    saveUserInfo(avUser);
                    mCallback.switchContent(new MainFragment());
                } else {
                    txt_login_problem.setText(getString(R.string.login_failed));
                }
            }
        });
    }

    //保存用户信息
    private void saveUserInfo(AVObject user) {
        SharedPreferences preferences = context.getSharedPreferences(CommonUtility.APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(User.objectId, user.getObjectId());
        editor.putString(User.createdAt, CommonUtility.formatDate2String(user.getCreatedAt()));
        editor.putString(User.updatedAt, CommonUtility.formatDate2String(user.getUpdatedAt()));
        editor.putString(User.username, username);
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
        if (progressbar != null)
            progressbar = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

