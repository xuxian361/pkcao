package com.sundy.pkcao.activitys;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.androidquery.AQuery;
import com.sundy.pkcao.R;
import com.sundy.pkcao.activitys._AbstractActivity;
import com.sundy.pkcao.tools.draganddropphotos.DraggableLayout;
import com.sundy.pkcao.tools.draganddropphotos.ImageDraggableView;

/**
 * Created by sundy on 15/4/23.
 */
public class ScaleImageViewActivity extends _AbstractActivity {

    private DraggableLayout dragableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scale_imageview);

        aq = new AQuery(this);
        aq.id(R.id.btnBack).clicked(onClickListener);

        String image = getIntent().getStringExtra("image");

        dragableLayout = (DraggableLayout) aq.id(R.id.dragableLayout).getView();
        ImageDraggableView imageTView = new ImageDraggableView(this, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageTView.setLayoutParams(params);
        AQuery image_aq = new AQuery(imageTView);
        image_aq.image(image);
        imageTView.setScaleType(ImageView.ScaleType.CENTER);
        dragableLayout.addView(imageTView);
        imageTView.setParentLayout(dragableLayout);
        dragableLayout.setBackground();
        dragableLayout.setActiveView(imageTView);
        dragableLayout.getActiveView();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnBack:
                    finish();
                    break;
            }
        }
    };
}
