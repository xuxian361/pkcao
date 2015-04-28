package com.sundy.pkcao.taker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import com.avos.avoscloud.AVUser;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.sundy.pkcao.R;
import com.sundy.pkcao.baidupush.Utils;
import com.sundy.pkcao.vo.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by sundy on 15/3/21.
 */
public class CommonUtility {

    private static String TAG = "CommonUtility";
    public static String APP_NAME = "PKCao";
    public static int SCREEN_WIDTH = 720;
    public static int SCREEN_HEIGHT = 1080;
    public static float SCREEN_DENSITY = DisplayMetrics.DENSITY_HIGH;
    public static final int IMAGE_CAPTURE_OK = 1;
    public final static int CONSULT_DOC_PICTURE = 11;
    public final static int CONSULT_DOC_PICTURE_1 = 111;
    public final static int VIDEO_LOCAL = 2;
    public final static int VIDEO_TAKE_VIDEO = 22;
    public static final int IMAGE_EDIT = 3;

    public static Date parseString2Date(String dateStr) {
        Date date = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = dateFormat.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String formatDate2String(Date date) {
        String str = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            str = format.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static boolean isLogin(Activity context) {
        /* 1.0版本
        boolean isLogin = false;
        SharedPreferences preferences = context.getSharedPreferences(CommonUtility.APP_NAME, Context.MODE_PRIVATE);
        String objectId = preferences.getString(User.objectId, "");
        if (objectId != null && objectId.length() != 0)
            isLogin = true;
        return isLogin;
        */
        boolean isLogin = false;
        AVUser currentUser = AVUser.getCurrentUser();
        if (currentUser != null)
            isLogin = true;
        else
            isLogin = false;
        return isLogin;
    }

    public static Bitmap compressImageFromFile(String srcPath) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            newOpts.inJustDecodeBounds = true;//只读边,不读内容
            bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
            newOpts.inJustDecodeBounds = false;
            int w = newOpts.outWidth;
            int h = newOpts.outHeight;
            float hh = 800f;//
            float ww = 480f;//
            int be = 1;
            if (w > h && w > ww) {
                be = (int) (newOpts.outWidth / ww);
            } else if (w < h && h > hh) {
                be = (int) (newOpts.outHeight / hh);
            }
            if (be <= 0)
                be = 1;
            newOpts.inSampleSize = be;//设置采样率
            newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;//该模式是默认的,可不设
            newOpts.inPurgeable = true;// 同时设置才会有效
            newOpts.inInputShareable = true;//。当系统内存不够时候图片自动被回收

            bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static int getRotate(String path) {
        int angle = 0;
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                angle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                angle = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                angle = 270;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return angle;
    }

    //返回拍照缩略图
    public static Bitmap getBitmapFromCamera(Intent data, Activity mContext) {
        Bundle bundle = data.getExtras();
        Bitmap bitmap = (Bitmap) bundle.get("data");
        String sdcardStaus = Environment.getExternalStorageState();
        if (!sdcardStaus.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(mContext, mContext.getString(R.string.sdk_not_exist), Toast.LENGTH_SHORT).show();
            return null;
        }
        File file = new File(Environment.getExternalStorageDirectory() + "/PKCao");
        if (!file.exists())
            file.mkdirs();
        String imageName = android.text.format.DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
        try {
            File imageFile = new File(file, imageName);
            if (!imageFile.exists()) {
                imageFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getImagePath3(Activity context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height,
                                           int kind) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    public static String getLastUpdatedTime() {
        String str = null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            //获取当前时间
            Date curDate = new Date(System.currentTimeMillis());
            str = formatter.format(curDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 保存缩略图
     */
    public static String saveThumbnail(Bitmap thumbnail) {
        String path = null;
        try {
            if (thumbnail != null) {
                File file = new File(Environment.getExternalStorageDirectory() + "/PKCao");
                if (!file.exists())
                    file.mkdirs();
                try {
                    File imageFile = new File(file, "thumbnail");
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                    imageFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(imageFile);
                    path = imageFile.getPath();
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 70, fos);
                    fos.flush();
                    fos.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    /**
     * 保存槽点图片
     */
    public static String savePhoto(Bitmap bitmap) {
        String path = null;
        try {
            if (bitmap != null) {
                File file = new File(Environment.getExternalStorageDirectory() + "/PKCao/Caodian_Imgs");
                if (!file.exists())
                    file.mkdirs();
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                    Date date = new Date();
                    File imageFile = new File(file, "caodian_img_" + sdf.format(date) + ".jpg");
                    if (!imageFile.exists()) {
                        imageFile.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(imageFile);
                    path = imageFile.getPath();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    //压缩Bitmap
    public static Bitmap compressBitmap(String path, Bitmap bitmap) {
        try {
            if (bitmap != null) {
                int angle = CommonUtility.getRotate(path);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static boolean isLargeImage(String path, Bitmap bitmap) {
        boolean isLarge = false;
        try {
            if (bitmap != null) {
                int angle = CommonUtility.getRotate(path);
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
                if (baos.toByteArray().length / 1024 > 100) {
                    isLarge = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isLarge;
    }

    //分享
    public static void showShare(Context context, HashMap<String, String> data) {
        try {
            ShareSDK.initSDK(context);
            OnekeyShare oks = new OnekeyShare();
            //关闭sso授权
            oks.disableSSOWhenAuthorize();
            // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
            oks.setTitle("《pk槽点》" + "http://apk.91.com/Soft/Android/com.sundy.pkcao-1-1.0.html");
            // text是分享文本，所有平台都需要这个字段
            oks.setText("《pk槽点》Android APP : " + data.get("title") + "http://apk.91.com/Soft/Android/com.sundy.pkcao-1-1.0.html");
            // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
            oks.setImagePath(data.get("img_url"));//确保SDcard下面存在此张图片
            // url仅在微信（包括好友和朋友圈）中使用
            oks.setUrl("http://apk.91.com/Soft/Android/com.sundy.pkcao-1-1.0.html");
            // comment是我对这条分享的评论，仅在人人网和QQ空间使用
            oks.setComment("《pk槽点》Android APP : " + data.get("content"));
            // site是分享此内容的网站名称，仅在QQ空间使用
            oks.setSite("http://apk.91.com/Soft/Android/com.sundy.pkcao-1-1.0.html");
            // 启动分享GUI
            oks.show(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 以apikey的方式绑定(baidu push)
    public static void startBaiduPush(Activity context) {
        PushManager.startWork(context,
                PushConstants.LOGIN_TYPE_API_KEY,
                Utils.getMetaValue(context, "api_key"));
    }

    //停止Baidu 推送
    public static void stopBaiduPush(Activity context) {
        PushManager.stopWork(context);
    }

}
