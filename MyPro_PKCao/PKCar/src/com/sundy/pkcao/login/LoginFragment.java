package com.sundy.pkcao.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.androidquery.AQuery;
import com.avos.avoscloud.*;
import com.sundy.pkcao.R;
import com.sundy.pkcao._AbstractFragment;
import com.sundy.pkcao.main.MainFragment;
import com.sundy.pkcao.register.RegisterFragment;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.vo.User;

import java.util.List;

/**
 * Created by sundy on 15/3/22.
 */
public class LoginFragment extends _AbstractFragment {

    private final String TAG = "LoginFragment";
    private Fragment fragment;
    private View v;
    private String username;


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
        aq.id(R.id.btn_register).clicked(onClick);
        aq.id(R.id.btn_login).clicked(onClick);
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

    private void login() {
        username = aq.id(R.id.ext_name).getEditText().getText().toString().trim();
        String password = aq.id(R.id.edt_password).getEditText().getText().toString().trim();

        AVQuery<AVObject> query = new AVQuery<AVObject>(User.table_name);
        query.whereEqualTo(User.username, username);
        query.whereEqualTo(User.password, password);
        mCallback.onLoading();
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                mCallback.finishLoading();
                if (e == null) {
                    if (list != null && list.size() != 0) {
                        if (list.get(0) != null) {
                            saveUserInfo(list.get(0));
                            mCallback.switchContent(new MainFragment());
                        }
                    } else {
                        Toast.makeText(context, getString(R.string.user_not_exit), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, getString(R.string.user_not_exit), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveUserInfo(AVObject user) {
        SharedPreferences preferences = context.getSharedPreferences(CommonUtility.APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(User.objectId, user.getObjectId());
        editor.putString(User.createdAt, CommonUtility.formatDate2String(user.getCreatedAt()));
        editor.putString(User.updatedAt, CommonUtility.formatDate2String(user.getUpdatedAt()));
        editor.putString(User.username, username);
        String user_img = user.getString(User.user_img);
        if (user_img != null) {
            editor.putString(User.user_img, user_img);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

