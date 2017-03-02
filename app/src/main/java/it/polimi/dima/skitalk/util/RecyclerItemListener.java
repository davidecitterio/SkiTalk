package it.polimi.dima.skitalk.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Max on 03/01/2017.
 */

public class RecyclerItemListener
        implements RecyclerView.OnItemTouchListener  {

    private RecyclerTouchListener listener;
    private GestureDetector gd;

    public interface RecyclerTouchListener {
        public void onClickItem(View v, int position) ;
        public void onClickSwitch(View v, int position) ;
    }

    public RecyclerItemListener(Context ctx, final RecyclerView rv,
                                final RecyclerTouchListener listener, final int width) {
        this.listener = listener;
        gd = new GestureDetector(ctx,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public void onLongPress(MotionEvent e) {

                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        if(e.getX() < width) {
                            View v = rv.findChildViewUnder(e.getX(), e.getY());
                            // Notify the even
                            listener.onClickItem(v, rv.getChildAdapterPosition(v));
                        } else {
                            View v = rv.findChildViewUnder(e.getX(), e.getY());
                            // Notify the even
                            listener.onClickSwitch(v, rv.getChildAdapterPosition(v));
                        }
                        return true;
                    }
                });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY());
        return ( child != null && gd.onTouchEvent(e));
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}