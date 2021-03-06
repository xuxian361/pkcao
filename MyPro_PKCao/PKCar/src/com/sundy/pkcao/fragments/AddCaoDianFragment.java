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
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.androidquery.AQuery;
import com.avos.avoscloud.*;
import com.sundy.pkcao.activitys.EditImageActivity;
import com.sundy.pkcao.R;
import com.sundy.pkcao.service.AddCaodianService;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.tools.ProgressWheel;
import com.sundy.pkcao.vo.Caodian;
import com.sundy.pkcao.vo.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sundy on 15/3/21.
 */
public class AddCaoDianFragment extends _AbstractFragment {

    private final String TAG = "AddCaoDianFragment";
    private Fragment fragment;
    private View v;
    private ArrayList<String> photoList = new ArrayList<String>();
    private LinearLayout linear_imgs;
    private int count = 0;
    private String videoPath = null;  //视频路径
    private String video_thumbnail_path; //视频缩略图路径
    private String photoPath = null;  //图片路径
    private ProgressWheel progressbar;
    private SharedPreferences preferences;
    private String caodian_id;
    private final int VIDEO_SIZE = 10 * 1024 * 1024;  //视频上传大小
    private final int VIDEO_DURATION = 10;//10秒
    private Vibrator vibrator;

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
        aq.id(R.id.txt_header_title).text(getString(R.string.add_caodian));
        linear_imgs = (LinearLayout) aq.id(R.id.linear_imgs).getView();
        progressbar = (ProgressWheel) aq.id(R.id.progressbar).getView();

        preferences = context.getSharedPreferences(CommonUtility.APP_NAME, Context.MODE_PRIVATE);

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
                    photoPath = "";
                    selectPhoto();
                    break;
                case R.id.btn_video:
                    videoPath = "";
                    video_thumbnail_path = "";
                    selectVideo();
                    break;
            }
        }
    };

    //选择视频
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
                    intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, VIDEO_DURATION);
                    intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, VIDEO_SIZE);
                    startActivityForResult(intent, CommonUtility.VIDEO_TAKE_VIDEO);
                }
            }
        }).create().show();
    }

    //选择图片
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

    //添加槽点
    private void addCaodian() {
        final String title = aq.id(R.id.edt_title).getEditText().getText().toString().trim();
        final String content = aq.id(R.id.edt_content).getEditText().getText().toString().trim();
        AVUser currentUser = AVUser.getCurrentUser();
        if (currentUser != null) {
            String user_id = currentUser.getObjectId();
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(context, getString(R.string.title_cannot_empty), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(context, getString(R.string.content_cannot_empty), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(user_id)) {
                Toast.makeText(context, getString(R.string.account_exception), Toast.LENGTH_SHORT).show();
                clearUserInfo();
                return;
            }

            final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            final Date date = new Date();
            caodian_id = Caodian.caodian_id + "_" + sdf.format(date);

            //开启一个后台服务来上传文件
            Intent intent = new Intent(context, AddCaodianService.class);
            intent.putExtra(Caodian.title, title);
            intent.putExtra(Caodian.content, content);
            intent.putExtra(Caodian.creater, user_id);
            intent.putExtra(Caodian.caodian_id, caodian_id);
            intent.putExtra("videoPath", videoPath);
            intent.putExtra("video_thumbnail_path", video_thumbnail_path);
            intent.putStringArrayListExtra("photoList", photoList);
            context.startService(intent);

            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {100, 400, 100, 400};   // 停止 开启 停止 开启
            vibrator.vibrate(pattern, 2);

            mCallback.onBack();

        } else {
            Toast.makeText(context, getString(R.string.session_expired), Toast.LENGTH_SHORT).show();
            clearUserInfo();
        }
    }

    //清理用户信息及退出,重新登录
    private void clearUserInfo() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        AVUser.logOut();
        mCallback.switchContent(new LoginFragment());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap = null; //图片Bitmap
        if (requestCode == CommonUtility.CONSULT_DOC_PICTURE) {
            try {
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
                String path = cursor.getString(column_index);
                //编辑图片
                Intent intent = new Intent(context, EditImageActivity.class);
                intent.putExtra("image", path);
                context.startActivityForResult(intent, CommonUtility.IMAGE_EDIT);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == CommonUtility.CONSULT_DOC_PICTURE_1) {
            try {
                if (data == null) {
                    return;
                }
                Uri uri = data.getData();
                String path = CommonUtility.getImagePath3(context, uri);
                //编辑图片
                Intent intent = new Intent(context, EditImageActivity.class);
                intent.putExtra("image", path);
                context.startActivityForResult(intent, CommonUtility.IMAGE_EDIT);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (CommonUtility.IMAGE_CAPTURE_OK == requestCode) {
            try {
                if (data == null)
                    return;
                Bundle bundle = data.getExtras();
                bitmap = (Bitmap) bundle.get("data");
                if (bitmap == null)
                    return;
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

                    //编辑图片
                    Intent intent = new Intent(context, EditImageActivity.class);
                    intent.putExtra("image", photoPath);
                    context.startActivityForResult(intent, CommonUtility.IMAGE_EDIT);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (CommonUtility.VIDEO_LOCAL == requestCode) {
            try {
                if (data == null) {
                    return;
                }
                Uri uri = data.getData();
                videoPath = CommonUtility.getImagePath3(context, uri);
                long size = CommonUtility.getFileSize(videoPath);
                if (size > VIDEO_SIZE) {
                    Toast.makeText(context, getString(R.string.video_too_large), Toast.LENGTH_SHORT).show();
                    videoPath = "";
                    video_thumbnail_path = "";
                    return;
                } else {
                    Bitmap thumbnail =
                            CommonUtility.getVideoThumbnail(videoPath, 480, 320, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
                    if (thumbnail != null) {
                        video_thumbnail_path = CommonUtility.saveThumbnail(thumbnail);
                        aq.id(R.id.relative_video).visible().clicked(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                String type = "video/mp4";
                                Uri uri = Uri.parse(videoPath);
                                intent.setDataAndType(uri, type);
                                startActivity(intent);
                            }
                        });
                        aq.id(R.id.img_video).image(thumbnail);
                        aq.id(R.id.btn_video).gone();
                        aq.id(R.id.img_delete_video).clicked(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                videoPath = "";
                                video_thumbnail_path = "";
                                aq.id(R.id.relative_video).gone();
                                aq.id(R.id.btn_video).visible();
                            }
                        });
                    } else {
                        aq.id(R.id.relative_video).gone();
                        aq.id(R.id.btn_video).visible();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (CommonUtility.VIDEO_TAKE_VIDEO == requestCode) {
            try {
                if (data == null) {
                    return;
                }
                Uri uri = data.getData();
                videoPath = CommonUtility.getImagePath3(context, uri);
                long size = CommonUtility.getFileSize(videoPath);
                if (size > VIDEO_SIZE) {
                    Toast.makeText(context, getString(R.string.video_too_large), Toast.LENGTH_SHORT).show();
                    videoPath = "";
                    video_thumbnail_path = "";
                    return;
                } else {
                    Bitmap thumbnail =
                            CommonUtility.getVideoThumbnail(videoPath, 480, 320, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
                    if (thumbnail != null) {
                        video_thumbnail_path = CommonUtility.saveThumbnail(thumbnail);
                        aq.id(R.id.relative_video).visible().clicked(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                String type = "video/mp4";
                                Uri uri = Uri.parse(videoPath);
                                intent.setDataAndType(uri, type);
                                startActivity(intent);
                            }
                        });
                        aq.id(R.id.img_video).image(thumbnail);
                        aq.id(R.id.btn_video).gone();
                        aq.id(R.id.img_delete_video).clicked(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                videoPath = "";
                                video_thumbnail_path = "";
                                aq.id(R.id.relative_video).gone();
                                aq.id(R.id.btn_video).visible();
                            }
                        });
                    } else {
                        aq.id(R.id.relative_video).gone();
                        aq.id(R.id.btn_video).visible();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (CommonUtility.IMAGE_EDIT == requestCode) {
            try {
                if (data == null)
                    return;
                Bundle bundle = data.getExtras();
                if (bundle == null)
                    return;
                photoPath = bundle.getString("imagePath");
                if (bitmap != null)// 如果不释放的话，不断取图片，将会内存不够
                    bitmap.recycle();
                //压缩并旋转Bitmap
                bitmap = CommonUtility.compressImageFromFile(photoPath);
                bitmap = CommonUtility.compressBitmap(photoPath, bitmap);
                showImgs(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showImgs(Bitmap bitmap) {
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
        if (vibrator != null)
            vibrator.cancel();
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
