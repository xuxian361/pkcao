package com.sundy.pkcao.fragments;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.androidquery.AQuery;
import com.avos.avoscloud.*;
import com.sundy.pkcao.R;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.tools.ProgressWheel;
import com.sundy.pkcao.vo.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sundy on 15/3/22.
 */
public class RegisterFragment extends _AbstractFragment {

    private final String TAG = "RegisterFragment";
    private Fragment fragment;
    private View v;
    private Bitmap bitmap; //头像Bitmap
    private String photoPath;  //头像路径
    private String username;
    private ProgressWheel progressbar;
    private CheckBox checkBox;
    private TextView txt_login_problem;


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
        aq.id(R.id.txt_header_title).text(getString(R.string.register));
        aq.id(R.id.btn_register).clicked(onClick);
        aq.id(R.id.relative_upload_photo).clicked(onClick);
        txt_login_problem = aq.id(R.id.txt_login_problem).getTextView();

        progressbar = (ProgressWheel) aq.id(R.id.progressbar).getView();

        checkBox = aq.id(R.id.checkbox).getCheckBox();
        aq.id(R.id.txt_term).text((Html.fromHtml("我同意 <font color='#878774'><u>PK槽点条款与制约</u></font>"))).clicked(onClick);
        checkBox.setChecked(false);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    aq.id(R.id.btn_register).enabled(true);
                    aq.id(R.id.btn_register).background(R.drawable.corner_all_light_blue);
                } else {
                    aq.id(R.id.btn_register).enabled(false);
                    aq.id(R.id.btn_register).background(R.drawable.corner_all_gray);
                }
            }
        });
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_register:
                    registerUser();
                    break;
                case R.id.relative_upload_photo:
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
                    break;
                case R.id.txt_term:
                    showTermDialog();
                    break;
            }
        }
    };

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
    }

    //注册
    private void registerUser() {
        username = aq.id(R.id.ext_name).getEditText().getText().toString().trim();
        final String password = aq.id(R.id.edt_password).getEditText().getText().toString().trim();
        String confirm_pwd = aq.id(R.id.edt_confirm_pwd).getEditText().getText().toString().trim();


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

        if (!password.equals(confirm_pwd)) {
            txt_login_problem.setText(getString(R.string.confirm_pwd_not_equal_pwd));
            aq.id(R.id.edt_confirm_pwd).getEditText().setText("");
            return;
        }

        if (TextUtils.isEmpty(photoPath)) {
            txt_login_problem.setText(getString(R.string.selete_photo));
            return;
        }

        try {
            txt_login_problem.setText("");
            showProgress(progressbar);

            AVUser user = new AVUser();
            user.setUsername(username);
            user.setPassword(password);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            Date date = new Date();
            AVFile file = AVFile.withAbsoluteLocalPath(sdf.format(date) + "_img.jpg", photoPath);
            user.put(User.user_img, file);

            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(AVException e) {
                    stopProgress(progressbar);
                    if (e == null) {
                        //Register success -> Login
                        login(username, password);
                    } else {
                        if (e.getCode() == 202) // Username has already been taken
                        {
                            txt_login_problem.setText(getString(R.string.user_exist));
                        } else {
                            txt_login_problem.setText(getString(R.string.server_error));
                        }
                    }
                }
            });
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    //登陆
    private void login(String username, String password) {
        showProgress(progressbar);
        AVUser.logInInBackground(username, password, new LogInCallback<AVUser>() {
            @Override
            public void done(AVUser avUser, AVException e) {
                stopProgress(progressbar);
                if (e == null) {//Login success
                    if (avUser != null) {
                        saveUserInfo(avUser);
                        mCallback.switchContent(new MainFragment());
                    } else {
                        txt_login_problem.setText(getString(R.string.server_error));
                    }
                } else {
                    txt_login_problem.setText(getString(R.string.server_error));
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

    //弹出协议对话框
    private void showTermDialog() {
        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_ok, null);
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(view);
        AQuery aq = new AQuery(view);
        aq.id(R.id.btn_ok).text(getString(R.string.confrim));
        aq.id(R.id.btn_ok).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog.cancel();
            }
        });
        dialog.show();
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


