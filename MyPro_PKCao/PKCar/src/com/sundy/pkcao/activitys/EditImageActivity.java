package com.sundy.pkcao.activitys;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import com.androidquery.AQuery;
import com.sundy.pkcao.R;
import com.sundy.pkcao.activitys._AbstractActivity;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.tools.MosaicView;
import com.sundy.pkcao.tools.OperationFileHelper;

import java.io.File;

/**
 * Created by sundy on 15/4/25.
 */
public class EditImageActivity extends _AbstractActivity {

    private final String TAG = "EditImageActivity";
    private MosaicView mvImage;
    private String imgPath;
    private int currentColor = R.color.red;

    public EditImageActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        rtLog(TAG, "------------>onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_image);

        aq = new AQuery(this);
        init();
    }

    private void init() {
        aq.id(R.id.btnBack).clicked(onClickListener);
        aq.id(R.id.btnSave).clicked(onClickListener);

        imgPath = getIntent().getStringExtra("image");
        Bitmap bitmap = CommonUtility.compressImageFromFile(imgPath);
        if (CommonUtility.isLargeImage(imgPath, bitmap)) {
            bitmap = CommonUtility.compressBitmap(imgPath, bitmap);
            imgPath = CommonUtility.savePhoto(bitmap);
        }

        //总
        aq.id(R.id.btn_color).clicked(onClickListener);
        aq.id(R.id.btn_model).clicked(onClickListener);
        aq.id(R.id.btn_effect).clicked(onClickListener);
        aq.id(R.id.btn_clear).clicked(onClickListener);
        aq.id(R.id.btn_reset).clicked(onClickListener);
        //分--颜色
        aq.id(R.id.btn_red).clicked(onClickListener);
        aq.id(R.id.btn_orange).clicked(onClickListener);
        aq.id(R.id.btn_yellow).clicked(onClickListener);
        aq.id(R.id.btn_green).clicked(onClickListener);
        aq.id(R.id.btn_blue).clicked(onClickListener);
        aq.id(R.id.btn_indigo).clicked(onClickListener);
        aq.id(R.id.btn_purple).clicked(onClickListener);
        //分--模式
        aq.id(R.id.btn_follow_finger).clicked(onClickListener);
        aq.id(R.id.btn_square).clicked(onClickListener);
        //分--效果
        aq.id(R.id.btn_mosaic).clicked(onClickListener);
        aq.id(R.id.btn_glass).clicked(onClickListener);
        aq.id(R.id.btn_purecolor).clicked(onClickListener);

        mvImage = (MosaicView) aq.id(R.id.iv_content).getView();
        mvImage.setSrcPath(imgPath);

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnBack:
                    try {
                        File file = new File(imgPath);
                        OperationFileHelper.RecursionDeleteFile(file);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    finish();
                    break;
                case R.id.btnSave:
                    //保存图片
                    String photoPath = mvImage.save();
                    if (photoPath != null && photoPath.length() != 0) {
                        Intent bundle = new Intent();
                        bundle.putExtra("imagePath", photoPath);
                        setResult(RESULT_OK, bundle);
                        finish();
                    } else {
                        Intent bundle = new Intent();
                        bundle.putExtra("imagePath", imgPath);
                        setResult(RESULT_OK, bundle);
                        finish();
                    }
                    break;
                case R.id.btn_reset:
                    aq.id(R.id.btn_reset).background(R.drawable.corner_all_orange);
                    aq.id(R.id.btn_clear).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.btn_effect).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.btn_model).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.btn_color).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.linear_color).gone();
                    aq.id(R.id.linear_effect).gone();
                    aq.id(R.id.linear_model).gone();
                    mvImage.clear();
                    mvImage.setErase(false);
                    break;
                case R.id.btn_clear:
                    aq.id(R.id.btn_reset).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.btn_clear).background(R.drawable.corner_all_orange);
                    aq.id(R.id.btn_effect).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.btn_model).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.btn_color).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.linear_color).gone();
                    aq.id(R.id.linear_effect).gone();
                    aq.id(R.id.linear_model).gone();
                    mvImage.setErase(true);
                    break;
                case R.id.btn_effect:
                    aq.id(R.id.btn_reset).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.btn_clear).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.btn_effect).background(R.drawable.corner_all_orange);
                    aq.id(R.id.btn_model).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.btn_color).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.linear_color).gone();
                    aq.id(R.id.linear_effect).visible();
                    aq.id(R.id.linear_model).gone();
                    break;
                case R.id.btn_model:
                    aq.id(R.id.btn_reset).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.btn_clear).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.btn_effect).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.btn_model).background(R.drawable.corner_all_orange);
                    aq.id(R.id.btn_color).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.linear_color).gone();
                    aq.id(R.id.linear_effect).gone();
                    aq.id(R.id.linear_model).visible();
                    break;
                case R.id.btn_color:
                    mvImage.setEffect(MosaicView.Effect.COLOR);
                    aq.id(R.id.btn_reset).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.btn_clear).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.btn_effect).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.btn_model).background(R.drawable.corner_all_light_white);
                    aq.id(R.id.btn_color).background(R.drawable.corner_all_orange);
                    aq.id(R.id.linear_color).visible();
                    aq.id(R.id.linear_effect).gone();
                    aq.id(R.id.linear_model).gone();
                    break;
                case R.id.btn_red:
                    aq.id(R.id.linear_color).gone();
                    aq.id(R.id.linear_effect).gone();
                    aq.id(R.id.linear_model).gone();
                    currentColor = R.color.red;
                    mvImage.setMosaicColor(getResources().getColor(currentColor));
                    break;
                case R.id.btn_orange:
                    aq.id(R.id.linear_color).gone();
                    aq.id(R.id.linear_effect).gone();
                    aq.id(R.id.linear_model).gone();
                    currentColor = R.color.orange;
                    mvImage.setMosaicColor(getResources().getColor(currentColor));
                    break;
                case R.id.btn_yellow:
                    aq.id(R.id.linear_color).gone();
                    aq.id(R.id.linear_effect).gone();
                    aq.id(R.id.linear_model).gone();
                    currentColor = R.color.yellow;
                    mvImage.setMosaicColor(getResources().getColor(currentColor));
                    break;
                case R.id.btn_green:
                    aq.id(R.id.linear_color).gone();
                    aq.id(R.id.linear_effect).gone();
                    aq.id(R.id.linear_model).gone();
                    currentColor = R.color.green;
                    mvImage.setMosaicColor(getResources().getColor(currentColor));
                    break;
                case R.id.btn_blue:
                    aq.id(R.id.linear_color).gone();
                    aq.id(R.id.linear_effect).gone();
                    aq.id(R.id.linear_model).gone();
                    currentColor = R.color.blue;
                    mvImage.setMosaicColor(getResources().getColor(currentColor));
                    break;
                case R.id.btn_indigo:
                    aq.id(R.id.linear_color).gone();
                    aq.id(R.id.linear_effect).gone();
                    aq.id(R.id.linear_model).gone();
                    currentColor = R.color.indigo;
                    mvImage.setMosaicColor(getResources().getColor(currentColor));
                    break;
                case R.id.btn_purple:
                    aq.id(R.id.linear_color).gone();
                    aq.id(R.id.linear_effect).gone();
                    aq.id(R.id.linear_model).gone();
                    currentColor = R.color.purple;
                    mvImage.setMosaicColor(getResources().getColor(currentColor));
                    break;
                case R.id.btn_mosaic://磨砂
                    aq.id(R.id.linear_color).gone();
                    aq.id(R.id.linear_effect).gone();
                    aq.id(R.id.linear_model).gone();
                    mvImage.setEffect(MosaicView.Effect.GRID);
                    break;
                case R.id.btn_glass://毛玻璃
                    aq.id(R.id.linear_color).gone();
                    aq.id(R.id.linear_effect).gone();
                    aq.id(R.id.linear_model).gone();
                    mvImage.setEffect(MosaicView.Effect.BLUR);
                    break;
                case R.id.btn_purecolor://纯颜色
                    aq.id(R.id.linear_color).visible();
                    aq.id(R.id.linear_effect).gone();
                    aq.id(R.id.linear_model).gone();
                    mvImage.setMosaicColor(getResources().getColor(currentColor));
                    mvImage.setEffect(MosaicView.Effect.COLOR);
                    break;
                case R.id.btn_follow_finger://手指模式
                    aq.id(R.id.linear_color).gone();
                    aq.id(R.id.linear_effect).gone();
                    aq.id(R.id.linear_model).gone();
                    mvImage.setMode(MosaicView.Mode.PATH);
                    break;
                case R.id.btn_square://方块模式
                    aq.id(R.id.linear_color).gone();
                    aq.id(R.id.linear_effect).gone();
                    aq.id(R.id.linear_model).gone();
                    mvImage.setMode(MosaicView.Mode.GRID);
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}

