package it.polimi.dima.skitalk.util;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Max on 02/01/2017.
 */

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;
    private int spacing;
    private boolean lastRowDivider;

    public DividerItemDecoration(Drawable divider, int spacing, boolean lastRowDivider) {
        this.mDivider = divider;
        this.spacing = spacing;
        this.lastRowDivider = lastRowDivider;
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount;
        if(lastRowDivider)
            childCount = parent.getChildCount();
        else
            childCount = parent.getChildCount() - 1;
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin + spacing/2;
            final int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(canvas);
        }
    }
}