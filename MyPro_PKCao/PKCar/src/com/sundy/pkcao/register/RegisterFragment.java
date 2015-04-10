package com.sundy.pkcao.register;

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
import com.sundy.pkcao.vo.User;

import java.util.List;

/**
 * Created by sundy on 15/3/22.
 */
public class RegisterFragment extends _AbstractFragment {

    private final String TAG = "RegisterFragment";
    private Fragment fragment;
    private View v;


    public RegisterFragment() {
    }

    public RegisterFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        v = inflater.inflate(R.layout.register, container, false);
        aq = new AQuery(v);

        init();
        return v;
    }

    private void init() {
        aq.id(R.id.txt_title).text(R.string.register);
        aq.id(R.id.btn_register).clicked(onClick);
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_register:
                    registerUser();
                    break;
            }
        }
    };

    private void registerUser() {
        final String username = aq.id(R.id.ext_name).getEditText().getText().toString().trim();
        final String password = aq.id(R.id.edt_password).getEditText().getText().toString().trim();
        String confirm_pwd = aq.id(R.id.edt_confirm_pwd).getEditText().getText().toString().trim();

        if (!password.equals(confirm_pwd)) {
            Toast.makeText(context, getString(R.string.confirm_pwd_not_equal_pwd), Toast.LENGTH_SHORT).show();
            aq.id(R.id.edt_confirm_pwd).getEditText().setText("");
            return;
        }

        //先查询Server是否存在这个User
        AVQuery<AVObject> query = new AVQuery<AVObject>(User.table_name);
        query.whereEqualTo(User.username, username);
        query.whereEqualTo(User.password, password);

        mCallback.onLoading();
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    if (list != null && list.size() != 0) {
                        mCallback.finishLoading();
                        Toast.makeText(context, getString(R.string.user_exit), Toast.LENGTH_SHORT).show();
                    } else {
                        AVObject user = new AVObject(User.table_name);
                        user.put(User.username, username);
                        user.put(User.password, password);

                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                mCallback.finishLoading();
                                if (e == null) {
                                    mCallback.switchContent(new MainFragment());
                                } else {
                                    Toast.makeText(context, getString(R.string.register_faid), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    mCallback.finishLoading();
                    Toast.makeText(context, getString(R.string.user_exit), Toast.LENGTH_SHORT).show();
                }
            }
        });
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


