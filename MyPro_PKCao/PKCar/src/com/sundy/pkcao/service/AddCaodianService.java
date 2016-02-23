package com.sundy.pkcao.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.avos.avoscloud.*;
import com.sundy.pkcao.R;
import com.sundy.pkcao.vo.Caodian;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by sundy on 16/2/20.
 */
public class AddCaodianService extends IntentService {

    private NotificationManager notificationManager;
    private Notification notification;
    private String title;
    private String content;
    private String user_id;
    private String caodian_id;
    private String videoPath;
    private ArrayList<String> photoList;
    private String video_thumbnail_path;

    private final int UPLOAD_FILE = 10;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPLOAD_FILE:
                    uploadCaodian();
                    break;
            }
        }
    };

    //上传槽点
    private void uploadCaodian() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        final Date date = new Date();
        //找到该用户后,创建一个槽点对象
        final AVObject caodian = new AVObject(Caodian.table_name);
        caodian.put(Caodian.title, title);
        caodian.put(Caodian.content, content);
        caodian.put(Caodian.creater, user_id);
        caodian.put(Caodian.caodian_id, caodian_id);


        if (videoPath != null && videoPath.length() != 0) {
            try {
                String suffix = videoPath.substring(videoPath.lastIndexOf("."), videoPath.length());
                final AVFile video = AVFile.withAbsoluteLocalPath(
                        Caodian.caodian_video + "_" + sdf.format(date) + suffix, videoPath);
                video.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        notificationManager.cancelAll();
                        if (video_thumbnail_path != null && video_thumbnail_path.length() != 0) {
                            try {
                                final AVFile video_thumbnail = AVFile.withAbsoluteLocalPath(Caodian.table_name + "_" + sdf.format(date) + ".jpg", video_thumbnail_path);
                                video_thumbnail.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(AVException e) {
                                        caodian.put(Caodian.caodian_video_thumbnail, video_thumbnail);

                                        //添加图片数组
                                        if (photoList != null && photoList.size() != 0) {
                                            for (int i = 0; i < photoList.size(); i++) {
                                                String path = photoList.get(i);
                                                if (path != null && path.length() != 0) {
                                                    try {
                                                        //上传图片
                                                        final AVFile file = AVFile.withAbsoluteLocalPath(Caodian.table_name + "_" + sdf.format(date) + ".jpg", path);
                                                        file.saveInBackground(new SaveCallback() {
                                                            @Override
                                                            public void done(AVException e) {
                                                                notificationManager.cancelAll();
                                                            }
                                                        }, new ProgressCallback() {
                                                            @Override
                                                            public void done(Integer integer) {
                                                                notification.contentView.setImageViewResource(R.id.img, R.drawable.logo);
                                                                // 更改文字
                                                                notification.contentView.setTextViewText(R.id.txtTitle, title);
                                                                // 更改进度条
                                                                notification.contentView.setProgressBar(R.id.noti_pd, 100, integer, false);
                                                                notificationManager.notify(0, notification);
                                                            }
                                                        });
                                                        caodian.put("img" + (i + 1), file);
                                                    } catch (IOException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }, new ProgressCallback() {
                                    @Override
                                    public void done(Integer integer) {
                                        notification.contentView.setImageViewResource(R.id.img, R.drawable.logo);
                                        // 更改文字
                                        notification.contentView.setTextViewText(R.id.txtTitle, title);
                                        // 更改进度条
                                        notification.contentView.setProgressBar(R.id.noti_pd, 100, integer, false);
                                        notificationManager.notify(0, notification);
                                    }
                                });
                            } catch (Exception e1) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new ProgressCallback() {
                    @Override
                    public void done(Integer integer) {
                        notification.contentView.setImageViewResource(R.id.img, R.drawable.logo);
                        // 更改文字
                        notification.contentView.setTextViewText(R.id.txtTitle, title);
                        // 更改进度条
                        notification.contentView.setProgressBar(R.id.noti_pd, 100, integer, false);
                        notificationManager.notify(0, notification);
                    }
                });
                caodian.put(Caodian.caodian_video, video);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else {
            //添加图片数组
            if (photoList != null && photoList.size() != 0) {
                for (int i = 0; i < photoList.size(); i++) {
                    String path = photoList.get(i);
                    if (path != null && path.length() != 0) {
                        try {
                            //上传图片
                            final AVFile file = AVFile.withAbsoluteLocalPath(Caodian.table_name + "_" + sdf.format(date) + ".jpg", path);
                            file.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    notificationManager.cancelAll();
                                }
                            }, new ProgressCallback() {
                                @Override
                                public void done(Integer integer) {
                                    notification.contentView.setImageViewResource(R.id.img, R.drawable.logo);
                                    // 更改文字
                                    notification.contentView.setTextViewText(R.id.txtTitle, title);
                                    // 更改进度条
                                    notification.contentView.setProgressBar(R.id.noti_pd, 100, integer, false);
                                    notificationManager.notify(0, notification);
                                }
                            });
                            caodian.put("img" + (i + 1), file);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
        caodian.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                notificationManager.cancelAll();
                stopSelf();
            }
        });
    }

    public AddCaodianService() {
        super("AddCaodianService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = new Notification(R.drawable.logo, getString(R.string.uploading_caodian), System.currentTimeMillis());

        RemoteViews view = new RemoteViews(getPackageName(), R.layout.notification_view_upload_caodian);
        notification.contentView = view;

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                R.string.app_name, new Intent(),
                PendingIntent.FLAG_UPDATE_CURRENT);

        notification.contentIntent = contentIntent;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        title = intent.getStringExtra(Caodian.title);
        content = intent.getStringExtra(Caodian.content);
        user_id = intent.getStringExtra(Caodian.creater);
        caodian_id = intent.getStringExtra(Caodian.caodian_id);
        videoPath = intent.getStringExtra("videoPath");
        photoList = intent.getStringArrayListExtra("photoList");
        video_thumbnail_path = intent.getStringExtra("video_thumbnail_path");

        notificationManager.notify(0, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mHandler.sendEmptyMessage(UPLOAD_FILE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancelAll();
    }
}
