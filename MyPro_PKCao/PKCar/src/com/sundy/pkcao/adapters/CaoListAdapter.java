package com.sundy.pkcao.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.androidquery.AQuery;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.sundy.pkcao.R;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.vo.Caodian;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sundy on 15/3/21.
 */
public class CaoListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List list = new ArrayList();

    public CaoListAdapter() {
    }

    public CaoListAdapter(Context context, LayoutInflater inflater) {
        this.context = context;
        this.inflater = inflater;
    }

    public void setData(List list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        if (list != null)
            return list.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            view = inflater.inflate(R.layout.cao_item, viewGroup, false);
            holder = new ViewHolder();
            AQuery aq = new AQuery(view);
            holder.txt_title = aq.id(R.id.txt_title).getTextView();
            holder.txt_count = aq.id(R.id.txt_count).getTextView();
            holder.img = aq.id(R.id.img).getImageView();
            holder.img_play = aq.id(R.id.img_play).getImageView();
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        AVObject item = (AVObject) list.get(i);
        if (item != null) {
            holder.txt_title.setText(item.getString(Caodian.title));

            AQuery aq_img = new AQuery(holder.img);
            AQuery aq_img_play = new AQuery(holder.img_play);

            AVFile video = item.getAVFile(Caodian.caodian_video);
            if (video != null) {
                final String video_path = video.getUrl();
                AVFile video_thumbnail = item.getAVFile(Caodian.caodian_video_thumbnail);
                if (video_thumbnail != null) {
                    String video_thumbnail_img = video_thumbnail.getUrl();
                    if (video_thumbnail_img != null && video_thumbnail_img.length() != 0) {
                        aq_img.visible().image(video_thumbnail_img).clicked(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                String type = "video/mp4";
                                Uri uri = Uri.parse(video_path);
                                intent.setDataAndType(uri, type);
                                context.startActivity(intent);
                            }
                        });
                        aq_img_play.visible();
                    } else {
                        aq_img.gone();
                        aq_img_play.gone();
                    }
                }
            } else {
                aq_img.gone();
                aq_img_play.gone();
            }
        }
        return view;
    }

    class ViewHolder {
        TextView txt_title, txt_count;
        ImageView img, img_play;
    }
}
