package com.sundy.pkcao.fragments;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.androidquery.AQuery;
import com.avos.avoscloud.*;
import com.sundy.pkcao.R;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.tools.ProgressWheel;
import com.sundy.pkcao.vo.User;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sundy on 15/4/16.
 */
public class EditUserFragment extends _AbstractFragment {

    private final String TAG = "EditUserFragment";
    private Fragment fragment;
    private View v;
    private Bitmap bitmap; //头像Bitmap
    private String photoPath;  //头像路径
    private SharedPreferences preferences;
    private String username;
    private String user_img;
    private ProgressWheel progressbar;
    private TextView txt_problem;


    public EditUserFragment() {
    }

    public EditUserFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        v = inflater.inflate(R.layout.edit_user, container, false);
        aq = new AQuery(v);

        init();
        return v;
    }

    private void init() {
        aq.id(R.id.txt_header_title).text(getString(R.string.user_edit));
        progressbar = (ProgressWheel) aq.id(R.id.progressbar).getView();
        txt_problem = aq.id(R.id.txt_problem).getTextView();

        aq.id(R.id.btn_edit).clicked(onClick);
        aq.id(R.id.relative_upload_photo).clicked(onClick);

        preferences = context.getSharedPreferences(CommonUtility.APP_NAME, Context.MODE_PRIVATE);
        username = preferences.getString(User.username, "");
        user_img = preferences.getString(User.user_img, "");

        aq.id(R.id.txt_name).text(username);
        aq.id(R.id.img_user).image(user_img);

    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_edit:
                    editUser();
                    break;
                case R.id.relative_upload_photo:
                    showPhotoChooseDialog();
                    break;
            }
        }
    };

    private void showPhotoChooseDialog() {
        CharSequence[] items = {getString(R.string.gallery), getString(R.string.camera)};
        new AlertDialog.Builder(context).setTitle(getString(R.string.upload_image)).setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/jpeg");
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        startActivityForResult(intent, CommonUtility.CONSULT_DOC_PICTURE_1);
                    } else {
                        startActivityForResult(intent, CommonUtility.CONSULT_DOC_PICTURE);
                    }
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CommonUtility.IMAGE_CAPTURE_OK);
                }
            }
        }).create().show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CommonUtility.CONSULT_DOC_PICTURE) {
            if (data == null) {
                return;
            }
            Uri uri = data.getData();
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.managedQuery(uri, proj,
                    null,
                    null,
                    null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            photoPath = cursor.getString(column_index);
            if (bitmap != null)// 如果不释放的话，不断取图片，将会内存不够
                bitmap.recycle();
            //压缩并旋转Bitmap
            bitmap = CommonUtility.compressImageFromFile(photoPath);
            bitmap = CommonUtility.compressBitmap(photoPath, bitmap);
            photoPath = CommonUtility.savePhoto(bitmap);
            aq.id(R.id.img_user).image(bitmap);
        } else if (requestCode == CommonUtility.CONSULT_DOC_PICTURE_1) {
            if (data == null) {
                return;
            }
            Uri uri = data.getData();
            photoPath = CommonUtility.getImagePath3(context, uri);
            if (bitmap != null)// 如果不释放的话，不断取图片，将会内存不够
                bitmap.recycle();
            //压缩并旋转Bitmap
            bitmap = CommonUtility.compressImageFromFile(photoPath);
            bitmap = CommonUtility.compressBitmap(photoPath, bitmap);
            photoPath = CommonUtility.savePhoto(bitmap);
            aq.id(R.id.img_user).image(bitmap);
        } else if (CommonUtility.IMAGE_CAPTURE_OK == requestCode) {
            Bundle bundle = data.getExtras();
            Bitmap bitmap = (Bitmap) bundle.get("data");
            String sdcardStaus = Environment.getExternalStorageState();
            if (!sdcardStaus.equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(context, getString(R.string.sdk_not_exist), Toast.LENGTH_SHORT).show();
            }
            File file = new File(Environment.getExternalStorageDirectory() + "/PKCao");
            if (!file.exists())
                file.mkdirs();
            String imageName = "user_img.jpg";
            try {
                File imageFile = new File(file, imageName);
                if (!imageFile.exists()) {
                    imageFile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(imageFile);
                photoPath = imageFile.getPath();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                if (bitmap != null) {
                    //压缩并旋转Bitmap
                    photoPath = CommonUtility.savePhoto(bitmap);
                    bitmap = CommonUtility.compressImageFromFile(photoPath);
                    bitmap = CommonUtility.compressBitmap(photoPath, bitmap);
                    photoPath = CommonUtility.savePhoto(bitmap);
                    aq.id(R.id.img_user).image(bitmap);
                } else {
                    Toast.makeText(context, getString(R.string.selete_photo_again), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //更新用户头像
        updateUserPhoto();
    }

    //更新用户头像
    private void updateUserPhoto() {
        try {
            showProgress(progressbar);
            AVUser currentUser = AVUser.getCurrentUser();
            if (currentUser != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                Date date = new Date();
                AVFile file = AVFile.withAbsoluteLocalPath(sdf.format(date) + "_img.jpg", photoPath);
                currentUser.put(User.user_img, file);
                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        stopProgress(progressbar);
                        if (e == null) {
                            aq.id(R.id.img_user).image(bitmap);
                        } else {
                            aq.id(R.id.img_user).image(user_img);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //编辑用户
    private void editUser() {
        final String password_old = aq.id(R.id.edt_password_old).getEditText().getText().toString().trim();
        final String password_new = aq.id(R.id.edt_password_new).getEditText().getText().toString().trim();
        String confirm_pwd = aq.id(R.id.edt_confirm_pwd).getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(password_old)) {
            txt_problem.setText(getString(R.string.old_password_can_not_null));
            return;
        }
        if (password_old.length() > 16) {
            txt_problem.setText(getString(R.string.fill_password_format_wrong));
            return;
        }
        if (password_old.length() < 6) {
            txt_problem.setText(getString(R.string.fill_password_format_wrong));
            return;
        }
        if (TextUtils.isEmpty(password_new)) {
            txt_problem.setText(getString(R.string.new_password_cannot_null));
            return;
        }
        if (password_new.length() > 16) {
            txt_problem.setText(getString(R.string.fill_password_format_wrong));
            return;
        }
        if (password_new.length() < 6) {
            txt_problem.setText(getString(R.string.fill_password_format_wrong));
            return;
        }

        if (!password_new.equals(confirm_pwd)) {
            txt_problem.setText(getString(R.string.confirm_pwd_not_equal_pwd));
            aq.id(R.id.edt_confirm_pwd).getEditText().setText("");
            return;
        }

        showProgress(progressbar);
        AVUser.logInInBackground(username, password_old, new LogInCallback<AVUser>() {
            @Override
            public void done(AVUser avUser, AVException e) {
                stopProgress(progressbar);
                try {
                    if (e == null) {//确保用户当前的有效登录状态
                        if (avUser != null) {
                            avUser.updatePasswordInBackground(password_old, password_new, new UpdatePasswordCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        login(username, password_new);
                                    } else {
                                        Toast.makeText(context, getString(R.string.update_fail), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(context, getString(R.string.session_expired), Toast.LENGTH_SHORT).show();
                            clearUserInfo();
                        }
                    } else {    //登陆过期
                        Toast.makeText(context, getString(R.string.session_expired), Toast.LENGTH_SHORT).show();
                        clearUserInfo();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    //清除用户信息
    private void clearUserInfo() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        AVUser.logOut();
        mCallback.switchContent(new LoginFragment());
    }

    //登陆
    private void login(String username, String password) {
        showProgress(progressbar);
        AVUser.logInInBackground(username, password, new LogInCallback<AVUser>() {
            @Override
            public void done(AVUser avUser, AVException e) {
                stopProgress(progressbar);
                if (avUser != null) {
                    saveUserInfo(avUser);
                    aq.id(R.id.edt_password_old).text("");
                    aq.id(R.id.edt_password_new).text("");
                    aq.id(R.id.edt_confirm_pwd).text("");
                    Toast.makeText(context, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                    mCallback.onBack();
                } else {
                    Toast.makeText(context, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
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
