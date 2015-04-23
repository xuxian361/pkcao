package com.sundy.pkcao.tools.draganddropphotos;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import java.util.HashSet;

public class DraggableLayout extends RelativeLayout implements OnClickListener {


    private static final int OUT_IMAGE_SIZE = 1200;

    private HashSet<OnInterceptToutchEventListener> listeners;

    private ImageDraggableView activeView;
    private boolean dragging;
    int height = 2000;
    int width = 2000;
    Context c;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public DraggableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        height = size.y;
        width = size.x;
        setOnClickListener(this);
        listeners = new HashSet<OnInterceptToutchEventListener>();
    }

    @Override
    public void onClick(View arg0) {
        setBackground();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        for (OnInterceptToutchEventListener listener : listeners) {
            listener.onTouchEvent(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    public interface OnInterceptToutchEventListener {
        public void onTouchEvent(MotionEvent e);
    }

    public boolean addOnInterceptToutchEventListener(
            OnInterceptToutchEventListener onInterceptToutchEventListener) {
        return listeners.add(onInterceptToutchEventListener);
    }

    public boolean removeOnInterceptToutchEventListener(
            OnInterceptToutchEventListener onInterceptToutchEventListener) {
        return listeners.remove(onInterceptToutchEventListener);
    }

    public ImageDraggableView getActiveView() {
        setBackground();
        return activeView;
    }

    public void setActiveView(ImageDraggableView activeView) {
        this.activeView = activeView;
    }

    public void setBackground() {
        for (int i = 0; i < getChildCount(); i++) {
            ImageDraggableView iView = (ImageDraggableView) getChildAt(i);
        }
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

}
