package com.sundy.pkcao;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.avos.avoscloud.AVOSCloud;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;
import com.sundy.pkcao.main.MainFragment;
import com.sundy.pkcao.menu.MenuFragment;
import com.sundy.pkcao.taker.CommonUtility;
import com.sundy.pkcao.taker.ResourceTaker;
import com.sundy.pkcao.tools.OperationFileHelper;

import java.io.File;

public class MainActivity extends SlidingFragmentActivity implements MainFragment.OnListListener, MenuFragment.OnListListener {

    private final String TAG = "MainActivity";
    private MenuFragment menuFragment;
    private Fragment mContent;
    private ProgressDialog loadingDailog;
    private int exit_count = 1;

    public MainActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            AVOSCloud.initialize(this,
                    "f20bpd0qsoqvkjj79otbh09i5mk70gesw2kl5d7gv738jkon",
                    "31hhp1ykz4czouw626n7asjmd5uvonsg0fx3p49wcbqq6no4");


            initeFragment();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initeFragment() {
        setContentView(R.layout.content_frame);
        setBehindContentView(R.layout.menu_frame);
        menuFragment = new MenuFragment(0);
        addMenuFragment();

        WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int width = (int) (display.getWidth() * 0.38);
        // customize the SlidingMenu
        SlidingMenu sm = getSlidingMenu();
        sm.setBehindOffset(width);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        sm.setFadeDegree(0.5f);
        sm.setShadowWidth(50);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

    }

    private void addMenuFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.menu_frame, menuFragment)
                .commit();
    }

    @Override
    protected void onStart() {
        rtLog(TAG, "------------------------onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        rtLog(TAG, "------------------------onResume");
        super.onResume();

    }

    public void onClick(View v) {
        if (mContent instanceof _AbstractFragment) {
            ((_AbstractFragment) mContent).onClick(v);
        }
        if (menuFragment != null) {
            menuFragment.onClick(v);
        }
    }

    @Override
    public void onLoading() {
        if (loadingDailog != null)
            loadingDailog.dismiss();
        loadingDailog = ProgressDialog.show(this, null, null);
        loadingDailog.setCancelable(true);
    }

    @Override
    public void finishLoading() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (loadingDailog != null)
                    loadingDailog.dismiss();
            }
        }, 500);
    }

    @Override
    public void switchContent(Fragment fragment) {
        try {
            if (fragment == null && mContent == fragment) {
                return;
            } else {
                mContent = fragment;
                if (fragment.isAdded()) {
                    getSupportFragmentManager().beginTransaction().show(fragment).commit();
                    if (getSlidingMenu().isMenuShowing()) {
                        getSlidingMenu().showContent();
                    } else {
                        showSlidingMenu();
                    }
                } else {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content_frame, fragment)
                            .commit();
                    getSlidingMenu().showContent();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addContent(Fragment fragment) {
        if (fragment == null && mContent == fragment) {
            return;
        } else {
            mContent = fragment;
            if (fragment.isAdded()) {
                getSupportFragmentManager().beginTransaction().show(fragment).commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.content_frame, fragment)
                        .addToBackStack(null)
                        .commit();
                getSlidingMenu().showContent();
            }
        }
    }

    @Override
    public void showSlidingMenu() {
        getSlidingMenu().toggle();
    }

    @Override
    public void onBack() {
        onBackPressed();
    }

    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            getSupportFragmentManager().popBackStack();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mContent = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                }
            }, 350);
        } else {
            if (exit_count == 2) {
                android.os.Process.killProcess(android.os.Process.myPid());
                exit_count = 1;
                try {
                    File file = new File(Environment.getExternalStorageDirectory() + "/PKCao");
                    OperationFileHelper.RecursionDeleteFile(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            exit_count++;
            Toast.makeText(this, getString(R.string.exit_confirm), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void reloadActivity() {
        try {
            if (loadingDailog != null) {
                loadingDailog.dismiss();
                loadingDailog.cancel();
                loadingDailog = null;
            }

            getSupportFragmentManager().popBackStack();
            rtLog(TAG, "------------>reloadActivity");
            menuFragment = new MenuFragment(menuFragment.curRadioId);
            addMenuFragment();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void switchContent(int rid) {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        rtLog(TAG, "------------------------onActivityResult");
        if (requestCode == CommonUtility.IMAGE_CAPTURE_OK) {
            mContent.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == CommonUtility.CONSULT_DOC_PICTURE) {
            mContent.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == CommonUtility.CONSULT_DOC_PICTURE_1) {
            mContent.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == CommonUtility.VIDEO_LOCAL) {
            mContent.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == CommonUtility.VIDEO_TAKE_VIDEO) {
            mContent.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == CommonUtility.IMAGE_EDIT) {
            mContent.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        rtLog(TAG, "------------------------onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        rtLog(TAG, "------------------------onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        rtLog(TAG, "------------------->onDestroy");
        super.onDestroy();
        try {
            if (mContent != null)
                mContent = null;
            if (menuFragment != null)
                menuFragment = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rtLog(String tag, String msg) {
        if (ResourceTaker.isDebug) {
            Log.e(tag, msg);
        }
    }
}