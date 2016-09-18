package com.example.anna.simplelayoutmanager;

import android.graphics.Rect;
import android.view.View;

/**
 * Created by andranik on 9/18/16.
 */

public class ViewData {

    private static final String TAG = ViewData.class.getSimpleName();

    private final Rect mViewRect = new Rect();

    private int mViewTop;
    private int mViewBottom;
    private int mViewLeft;
    private int mViewRight;

    private int mPointIndex;

    private boolean mIsViewVisible; // TODO: remove it
    private MyPoint mViewCenter;

    public ViewData(int viewTop, int viewBottom, int viewLeft, int viewRight, int pointIndex, MyPoint viewCenter) {
        mViewTop = viewTop;
        mViewBottom = viewBottom;
        mViewLeft = viewLeft;
        mViewRight = viewRight;
        mPointIndex = pointIndex;
        mViewCenter = viewCenter;
    }



    public void updateData(View view, int pointIndex, MyPoint viewCenter) {
        mIsViewVisible = view.getLocalVisibleRect(mViewRect);

        mViewTop = view.getTop();
        mViewBottom = view.getBottom();
        mViewLeft = view.getLeft();
        mViewRight = view.getRight();
        mPointIndex = pointIndex;
        mViewCenter = viewCenter;
    }

    @Override
    public String toString() {
        return "ViewData{" +
                "mViewRect=" + mViewRect +
                ", mViewTop=" + mViewTop +
                ", mViewBottom=" + mViewBottom +
                ", mViewLeft=" + mViewLeft +
                ", mViewRight=" + mViewRight +
                ", mIsViewVisible=" + mIsViewVisible +
                '}';
    }

    public int getViewBottom() {
        return mViewBottom;
    }

    public int getViewLeft() {
        return mViewLeft;
    }

    public int getViewTop() {
        return mViewTop;
    }

    public int getPointIndex() {
        return mPointIndex;
    }

    public boolean isViewVisible() {
        return mIsViewVisible;
    }

    public MyPoint getCenterPoint() {
        return mViewCenter;
    }
}
