package com.sundy.pkcao.login;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.androidquery.AQuery;
import com.avos.avoscloud.*;
import com.sundy.pkcao.R;
import com.sundy.pkcao._AbstractFragment;
import com.sundy.pkcao.main.MainFragment;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.vo.User;

import java.io.*;
import java.util.List;

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
        aq.id(R.id.txt_title).text(R.string.register);
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
                    new DownloadImage().execute(user_img);
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
            bitmap = CommonUtility.compressImageFromFile(photoPath);
            if (bitmap != null) {
                int angle = CommonUtility.getRotate(photoPath);
                if (angle != 0) {
                    Matrix m = new Matrix();
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    m.setRotate(angle);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int options = 100;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                while (baos.toByteArray().length / 1024 > 100) {
                    baos.reset();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
                    options -= 10;
                }
            }
            aq.id(R.id.img_user).image(bitmap);
        } else if (requestCode == CommonUtility.CONSULT_DOC_PICTURE_1) {
            if (data == null) {
                return;
            }
            Uri uri = data.getData();
            photoPath = CommonUtility.getImagePath3(context, uri);
            if (bitmap != null)// 如果不释放的话，不断取图片，将会内存不够
                bitmap.recycle();
            bitmap = CommonUtility.compressImageFromFile(photoPath);
            if (bitmap != null) {
                int angle = CommonUtility.getRotate(photoPath);
                if (angle != 0) {
                    Matrix m = new Matrix();
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    m.setRotate(angle);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int options = 100;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                while (baos.toByteArray().length / 1024 > 100) {
                    baos.reset();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
                    options -= 10;
                }
            }
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
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int options = 100;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    while (baos.toByteArray().length / 1024 > 100) {
                        baos.reset();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
                        options -= 10;
                    }
                    aq.id(R.id.img_user).image(bitmap);
                } else {
                    Toast.makeText(context, getString(R.string.selete_photo_again), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void editUser() {
        final String password = aq.id(R.id.edt_password).getEditText().getText().toString().trim();
        String confirm_pwd = aq.id(R.id.edt_confirm_pwd).getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, getString(R.string.fill_password), Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() > 16) {
            Toast.makeText(context, getString(R.string.fill_password), Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(context, getString(R.string.fill_password), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm_pwd)) {
            Toast.makeText(context, getString(R.string.confirm_pwd_not_equal_pwd), Toast.LENGTH_SHORT).show();
            aq.id(R.id.edt_confirm_pwd).getEditText().setText("");
            return;
        }

        mCallback.onLoading();
        String user_id = preferences.getString(User.objectId, "");
        //先查询Server是否存在这个User
        AVQuery<AVObject> query = new AVQuery<AVObject>(User.table_name);
        query.whereEqualTo(User.objectId, user_id);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    if (list != null && list.size() != 0) {
                        AVObject user = list.get(0);
                        if (user != null) {
                            user.put(User.password, password);
                            try {
                                AVFile file = AVFile.withAbsoluteLocalPath(username + "_img.jsp", photoPath);
                                user.put(User.user_img, file);

                                user.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(AVException e) {
                                        mCallback.finishLoading();
                                        if (e == null) {
                                            findUserInfo(username, password);
                                        } else {
                                            Toast.makeText(context, getString(R.string.register_faid), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void findUserInfo(String username, String password) {
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
                            Toast.makeText(context, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, getString(R.string.register_faid), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, getString(R.string.register_faid), Toast.LENGTH_SHORT).show();
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
        AVFile file = user.getAVFile(User.user_img);
        if (username != null) {
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

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... URL) {
            String imageURL = URL[0];
            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
                photoPath = CommonUtility.savePhoto(bitmap);
                //压缩并旋转Bitmap
                bitmap = CommonUtility.compressImageFromFile(photoPath);
                bitmap = CommonUtility.compressBitmap(photoPath, bitmap);
                photoPath = CommonUtility.savePhoto(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Set the bitmap into ImageView
            if (result != null) {
                editUser();
            }
        }
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
