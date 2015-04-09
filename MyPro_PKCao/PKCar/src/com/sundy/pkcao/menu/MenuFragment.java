package com.sundy.pkcao.menu;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.androidquery.AQuery;
import com.sundy.pkcao.R;
import com.sundy.pkcao._AbstractFragment;
import com.sundy.pkcao.ilike.ILikeFragment;
import com.sundy.pkcao.login.LoginFragment;
import com.sundy.pkcao.main.MainFragment;
import com.sundy.pkcao.record.RecordFragment;
import com.sundy.pkcao.taker.ResourceTaker;
import com.sundy.pkcao.talike.TaLikeFragment;

/**
 * Created by sundy on 15/3/21.
 */
public class MenuFragment extends Fragment {

    private final String TAG = "MenuFragment";
    private AQuery aq;
    private LayoutInflater layoutinflater;
    private View v;
    private OnListListener mCallback;
    public OnListListener MenuCallBack;
    protected FragmentActivity context;
    public int curRadioId = R.id.btn_main;
    private _AbstractFragment main, i_like, ta_like, record;

    public MenuFragment() {
    }

    public MenuFragment(int radioId) {
        if (radioId > 0)
            curRadioId = radioId;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnListListener) (context = (FragmentActivity) activity);
            MenuCallBack = mCallback;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.layoutinflater = inflater;
        v = layoutinflater.inflate(R.layout.menu, container, false);
        aq = new AQuery(v);

        init();

        return v;
    }

    private void init() {
        try {
            if (mCallback == null)
                return;
            mCallback.switchContent(getFragmentItem(curRadioId));

            //replace fragment
            aq.id(R.id.btn_main).clicked(onClickListener);
            aq.id(R.id.btn_login).clicked(onClickListener);
            aq.id(R.id.btn_I_like).clicked(onClickListener);
            aq.id(R.id.btn_TA_like).clicked(onClickListener);
            aq.id(R.id.btn_record).clicked(onClickListener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int id = v.getId();
            Fragment fragment = getFragmentItem(id);

            if (fragment == null)
                return;
            if (mCallback != null)
                mCallback.switchContent(fragment);
        }
    };

    private Fragment getFragmentItem(int index) {
        Fragment fragment = null;
        switch (index) {
            case R.id.btn_main:
                if (main == null)
                    main = new MainFragment(MenuFragment.this);
                fragment = main;
                curRadioId = index;

                break;
            case R.id.btn_login:
                mCallback.addContent(new LoginFragment());
                break;
            case R.id.btn_I_like:
                if (i_like == null)
                    i_like = new ILikeFragment(MenuFragment.this);
                fragment = i_like;
                curRadioId = index;

                break;
            case R.id.btn_TA_like:
                if (ta_like == null)
                    ta_like = new TaLikeFragment(MenuFragment.this);
                fragment = ta_like;
                curRadioId = index;

                break;
            case R.id.btn_record:
                if (record == null)
                    record = new RecordFragment(MenuFragment.this);
                fragment = record;
                curRadioId = index;

                break;
        }
        return fragment;
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

    public void onClick(View v) {

    }

    // Container Activity must implement this interface
    public interface OnListListener {
        public void onLoading();

        public void finishLoading();

        public void switchContent(Fragment fragment);

        public void addContent(Fragment fragment);

        public void showSlidingMenu();
    }

    private void rtLog(String tag, String msg) {
        if (ResourceTaker.isDebug) {
            Log.i(tag, msg);
        }
    }
}
