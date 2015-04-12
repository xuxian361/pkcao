package com.sundy.pkcao.caodian;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.androidquery.AQuery;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.sundy.pkcao.R;
import com.sundy.pkcao._AbstractFragment;
import com.sundy.pkcao.adapters.CaoListAdapter;
import com.sundy.pkcao.main.MainFragment;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.vo.Caodian;
import com.sundy.pkcao.vo.Caodian_Img;
import com.sundy.pkcao.vo.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

/**
 * Created by sundy on 15/3/21.
 */
public class AddCaoDianFragment extends _AbstractFragment {

    private final String TAG = "AddCaoDianFragment";
    private Fragment fragment;
    private View v;
    private List<String> photoList = new ArrayList<String>();
    private LinearLayout linear_imgs;
    private String caodian_id;
    private int count = 0;
    private String videoPath = null;  //视频路径


    public AddCaoDianFragment() {
    }

    public AddCaoDianFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        v = inflater.inflate(R.layout.add_cao, container, false);
        aq = new AQuery(v);

        init();
        return v;
    }

    private void init() {
        linear_imgs = (LinearLayout) aq.id(R.id.linear_imgs).getView();

        aq.id(R.id.txt_confirm).clicked(onClick);
        aq.id(R.id.btn_photo).clicked(onClick);
        aq.id(R.id.btn_video).clicked(onClick);
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.txt_confirm:
                    addCaodian();
                    break;
                case R.id.btn_photo:
                    selectPhoto();
                    break;
                case R.id.btn_video:
                    selectVideo();
                    break;
            }
        }
    };

    private void selectVideo() {
        CharSequence[] items = {getString(R.string.local), getString(R.string.take_video)};
        new AlertDialog.Builder(context).setTitle(getString(R.string.upload_caodian_video)).setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("video/*");
                    Intent wrapperIntent = Intent.createChooser(intent, null);
                    startActivityForResult(wrapperIntent, CommonUtility.VIDEO_LOCAL);
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                    intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 3 * 1024 * 1024);
                    startActivityForResult(intent, CommonUtility.VIDEO_TAKE_VIDEO);
                }
            }
        }).create().show();
    }

    private void selectPhoto() {
        CharSequence[] items = {getString(R.string.gallery), getString(R.string.camera)};
        new AlertDialog.Builder(context).setTitle(getString(R.string.upload_caodian_img)).setItems(items, new DialogInterface.OnClickListener() {
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

    private void addCaodian() {
        String title = aq.id(R.id.edt_title).getEditText().getText().toString().trim();
        String content = aq.id(R.id.edt_content).getEditText().getText().toString().trim();
        SharedPreferences preferences = context.getSharedPreferences(CommonUtility.APP_NAME, Context.MODE_PRIVATE);
        String creater_id = preferences.getString(User.objectId, "");

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(context, getString(R.string.input_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(context, getString(R.string.input_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(creater_id)) {
            Toast.makeText(context, getString(R.string.account_exception), Toast.LENGTH_SHORT).show();
            clearUserInfo();
            mCallback.switchContent(new MainFragment());
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date date = new Date();
        caodian_id = sdf.format(date);

        AVObject avObject = new AVObject(Caodian.table_name);
        avObject.put(Caodian.title, title);
        avObject.put(Caodian.content, content);
        avObject.put(Caodian.creater_id, creater_id);
        avObject.put(Caodian.caodian_id, caodian_id);

        if (videoPath != null && videoPath.length() != 0) {
            try {
                String suffix = videoPath.substring(videoPath.lastIndexOf("."), videoPath.length());
                AVFile video = AVFile.withAbsoluteLocalPath(Caodian.caodian_video + suffix, videoPath);
                avObject.put(Caodian.caodian_video, video);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mCallback.onLoading();
        avObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    if (photoList != null && photoList.size() != 0) {
                        saveCaoDianImgs();
                    } else {
                        mCallback.finishLoading();
                        Toast.makeText(context, getString(R.string.add_success), Toast.LENGTH_SHORT).show();
                        mCallback.switchContent(new MainFragment());
                    }
                } else {
                    mCallback.finishLoading();
                    Toast.makeText(context, getString(R.string.add_failed), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveCaoDianImgs() {
        try {
            for (int i = 0; i < photoList.size(); i++) {
                String path = photoList.get(i);
                if (path != null && path.length() != 0) {
                    AVObject object = new AVObject(Caodian_Img.table_name);
                    object.put(Caodian_Img.caodian_id, caodian_id);

                    AVFile file = AVFile.withAbsoluteLocalPath(Caodian_Img.caodian_id + "_" + i + ".jsp", path);
                    object.put(Caodian_Img.caodian_img, file);
                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if (count == photoList.size() - 1) {
                                mCallback.finishLoading();
                                Toast.makeText(context, getString(R.string.add_success), Toast.LENGTH_SHORT).show();
                                mCallback.switchContent(new MainFragment());
                            } else {
                                count = count + 1;
                            }
                        }
                    });
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void clearUserInfo() {
        SharedPreferences preferences = context.getSharedPreferences(CommonUtility.APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap = null; //图片Bitmap
        String photoPath = null;  //图片路径
        videoPath = "";
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
            showImgs(photoPath, bitmap);
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
            showImgs(photoPath, bitmap);
        } else if (CommonUtility.IMAGE_CAPTURE_OK == requestCode) {
            Bundle bundle = data.getExtras();
            bitmap = (Bitmap) bundle.get("data");
            String sdcardStaus = Environment.getExternalStorageState();
            if (!sdcardStaus.equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(context, getString(R.string.sdk_not_exist), Toast.LENGTH_SHORT).show();
            }
            File file = new File(Environment.getExternalStorageDirectory() + "/PKCao");
            if (!file.exists())
                file.mkdirs();
            String imageName = "caodian_img.jpg";
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
                } else {
                    Toast.makeText(context, getString(R.string.selete_photo_again), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            showImgs(photoPath, bitmap);
        } else if (CommonUtility.VIDEO_LOCAL == requestCode) {
            if (data == null) {
                return;
            }
            Uri uri = data.getData();
            videoPath = CommonUtility.getImagePath3(context, uri);
            Bitmap thumbnail =
                    CommonUtility.getVideoThumbnail(videoPath, 200, 120, MediaStore.Images.Thumbnails.MICRO_KIND);
            if (thumbnail != null) {
                aq.id(R.id.relative_video).visible();
                aq.id(R.id.img_video).image(thumbnail);
                aq.id(R.id.btn_video).gone();
                aq.id(R.id.img_delete_video).clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        videoPath = "";
                        aq.id(R.id.relative_video).gone();
                        aq.id(R.id.btn_video).visible();
                    }
                });
            } else {
                aq.id(R.id.relative_video).gone();
                aq.id(R.id.btn_video).visible();
            }
        } else if (CommonUtility.VIDEO_TAKE_VIDEO == requestCode) {
            rtLog(TAG, "----------->data= " + data.toString());
            if (data == null) {
                return;
            }
            Uri uri = data.getData();
            videoPath = CommonUtility.getImagePath3(context, uri);
            rtLog(TAG, "----------->videoPath= " + videoPath.toString());
            Bitmap thumbnail =
                    CommonUtility.getVideoThumbnail(videoPath, 200, 120, MediaStore.Images.Thumbnails.MICRO_KIND);
            if (thumbnail != null) {
                aq.id(R.id.relative_video).visible();
                aq.id(R.id.img_video).image(thumbnail);
                aq.id(R.id.btn_video).gone();
                aq.id(R.id.img_delete_video).clicked(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        videoPath = "";
                        aq.id(R.id.relative_video).gone();
                        aq.id(R.id.btn_video).visible();
                    }
                });
            } else {
                aq.id(R.id.relative_video).gone();
                aq.id(R.id.btn_video).visible();
            }
        }
    }

    private void showImgs(String photoPath, Bitmap bitmap) {
        if (photoPath != null && photoPath.length() != 0) {
            if (photoList.contains(photoPath)) {
                return;
            } else {
                if (bitmap != null) {
                    photoList.add(photoPath);
                    final View view = inflater.inflate(R.layout.caodian_img, null);
                    AQuery aq1 = new AQuery(view);
                    aq1.id(R.id.img_caodian).image(bitmap);
                    aq1.id(R.id.img_delete).tag(photoPath).clicked(new View.OnClickListener() {
                        @Override
                        public void onClick(View button) {
                            String tag = (String) button.getTag();
                            if (tag != null) {
                                if (photoList.contains(tag)) {
                                    photoList.remove(tag);

                                    linear_imgs.removeView(view);
                                }
                            }
                        }
                    });
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    params.setMargins(0, 5, 0, 5);
                    view.setLayoutParams(params);
                    linear_imgs.addView(view);
                }
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
