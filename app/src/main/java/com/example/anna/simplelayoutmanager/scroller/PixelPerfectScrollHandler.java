package com.example.anna.simplelayoutmanager.scroller;

import android.util.Log;
import android.view.View;

import com.example.anna.simplelayoutmanager.Config;
import com.example.anna.simplelayoutmanager.ViewData;
import com.example.anna.simplelayoutmanager.circule.CircleHelperInterface;
import com.example.anna.simplelayoutmanager.layouter.Layouter;
import com.example.anna.simplelayoutmanager.point.Point;
import com.example.anna.simplelayoutmanager.point.UpdatablePoint;

/**
 * Created by andranik on 9/21/16.
 */

public class PixelPerfectScrollHandler extends ScrollHandler {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = PixelPerfectScrollHandler.class.getSimpleName();

    private final ScrollHandlerCallback mCallback;
    private final CircleHelperInterface mQuadrantHelper;
    private final Layouter mLayouter;

    /**
     * This is a helper object that will be updated many times while scrolling.
     * We use this to reduce memory consumption, which means less GC will kicks of less times :)
     */
    private final static UpdatablePoint SCROLL_HELPER_POINT = new UpdatablePoint(0, 0);

    PixelPerfectScrollHandler(ScrollHandlerCallback callback, CircleHelperInterface quadrantHelper, Layouter layouter) {
        super(callback, quadrantHelper, layouter);
        mCallback = callback;
        mQuadrantHelper = quadrantHelper;
        mLayouter = layouter;
    }

    /**
     * 1. Shifts first view by "dy"
     * 2. Shifts all other views relatively to first view.
     */
    @Override
    protected void scrollViews(View firstView, int delta) {
        /**1. */
        Point firstViewNewCenter = scrollSingleViewVerticallyBy(firstView, delta);

        ViewData previousViewData = new ViewData(
                firstView.getTop(),
                firstView.getBottom(),
                firstView.getLeft(),
                firstView.getRight(),
                firstViewNewCenter);

        /**2. */
        for (int indexOfView = 1; indexOfView < mCallback.getChildCount(); indexOfView++) {
            View view = mCallback.getChildAt(indexOfView);
            scrollSingleView(previousViewData, view);
        }
    }

    private void scrollSingleView(ViewData previousViewData, View view) {
        if (SHOW_LOGS) Log.v(TAG, "scrollSingleView, previousViewData " + previousViewData);

        int width = view.getWidth();
        int height = view.getHeight();

        int viewCenterX = view.getRight() - width / 2;
        int viewCenterY = view.getTop() + height / 2;

        SCROLL_HELPER_POINT.update(viewCenterX, viewCenterY);

        int centerPointIndex = mQuadrantHelper.getViewCenterPointIndex(SCROLL_HELPER_POINT);

        Point oldCenterPoint = mQuadrantHelper.getViewCenterPoint(centerPointIndex);

        Point newCenterPoint = mQuadrantHelper.findNextViewCenter(previousViewData, width / 2, height / 2);

        int dX = newCenterPoint.getX() - oldCenterPoint.getX();
        int dY = newCenterPoint.getY() - oldCenterPoint.getY();

        view.offsetTopAndBottom(dY);
        view.offsetLeftAndRight(dX);

        previousViewData.updateData(view, newCenterPoint);
    }

}