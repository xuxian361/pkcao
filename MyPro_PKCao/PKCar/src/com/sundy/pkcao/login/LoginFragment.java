package com.sundy.pkcao.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.androidquery.AQuery;
import com.avos.avoscloud.AVObject;
import com.sundy.pkcao.R;
import com.sundy.pkcao._AbstractFragment;
import com.sundy.pkcao.register.RegisterFragment;

/**
 * Created by sundy on 15/3/22.
 */
public class LoginFragment extends _AbstractFragment {

    private final String TAG = "LoginFragment";
    private Fragment fragment;
    private View v;


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
        String username = aq.id(R.id.ext_name).getEditText().getText().toString().trim();
        String password = aq.id(R.id.edt_password).getEditText().getText().toString().trim();

        AVObject user = new AVObject("PkUser");
        user.put("username", username);
        user.put("password", password);
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

