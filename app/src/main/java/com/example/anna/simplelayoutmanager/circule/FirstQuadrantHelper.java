package com.example.anna.simplelayoutmanager.circule;

import android.util.Log;
import android.view.View;

import com.example.anna.simplelayoutmanager.Config;
import com.example.anna.simplelayoutmanager.ViewData;
import com.example.anna.simplelayoutmanager.point.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andranik on 9/21/16.
 */

public class FirstQuadrantHelper implements QuadrantHelper {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = FirstQuadrantHelper.class.getSimpleName();


    private List<Point> mPoints;

    private final Map<Integer, Point> mCircleIndexPoint;
    private final Map<Point, Integer> mCirclePointIndex;


    public FirstQuadrantHelper(List<Point> points, int pointsToAddCount) {

        mPoints = points;

        mCircleIndexPoint = new HashMap<>();
        mCirclePointIndex = new HashMap<>();

        if(SHOW_LOGS) Log.v(TAG, ">> constructor, start filling sector points");
        long start = System.currentTimeMillis();

        addMissingPoints(pointsToAddCount);
        initMaps();

        if(SHOW_LOGS) Log.v(TAG, "<< constructor, finished filling sector points in " + (System.currentTimeMillis() - start));
    }

    private void addMissingPoints(int pointsToAddCount){
        final ArrayList<Point> newPoints = new ArrayList<>();

        final Point firstPoint = mPoints.get(0);
        final Point lastPoint = mPoints.get(mPoints.size() - 1);

        // append points from start
        for (int i = pointsToAddCount; i > 0; i--){
            newPoints.add(new Point(firstPoint.getX() + i, firstPoint.getY()));
        }

        // add existing points
        newPoints.addAll(mPoints);

        // append points from end
        for (int i = 1; i < pointsToAddCount; i++){
            newPoints.add(new Point(lastPoint.getX() + i, lastPoint.getY()));
        }

        mPoints = newPoints;
    }

    private void initMaps(){

        for (int i = 0; i < mPoints.size(); i++){
            final Point p = mPoints.get(i);
            mCirclePointIndex.put(p, i);
            mCircleIndexPoint.put(i, p);
        }
    }

    /**
     * This method looks for a next point clockwise. 4th, 1st, 2nd quadrants in that order.
     * It is using {@link #mCirclePointIndex} and {@link #mCircleIndexPoint} to get point on the circle.
     *
     *     ^ We end here          -->  --> We start here
     *    /             -y |                    \
     *   /                 |           4th       \
     *               ______|______                V
     *   ^        _--      |      --_
     *   |     _/          |         \_      x    |
     *   |    |            |           |          |
     *        -------------|--------------->      V
     *   ^    |_           |          _|
     *   |      \_         |        _/            /
     *   |        --_______|______--             /
     *      2nd            |            1st     V
     *   ^                 |
     *    \   <--       +y V              <---
     *     \
     *
     *     We have previousViewData. And center of previous view in previousViewData;
     *
     *     The algorithm of finding next view is following:
     *
     *     1. We get "next view center" using "previous view center"
     *     2. Calculate next view top, bottom, right
     *
     *     3. We check if "next view top" is below "previous view bottom"
     *
     *                  -y |                    \
     *                     |           4th       \
     *                     |______                V
     *                     |      --_
     *                     |         \_      x    |
     *                     |           |          |
     *                     |           |          |
     *        -------------|--------------->      V
     *        |            |      _____|___
     *        |_           |     |    _|   | previous view
     *          \_         | ____|___/_____|
     *            --_______||______-- | next view
     *      2nd            ||_________|
     *                     |
     *        <--       +y V              <---
     *
     *
     *     4. We check if "next view bottom" is above "previous view top"
     *
     *                  -y |
     *                     |
     *                     |______
     *                     |      --_
     *                     |         \_      x
     *                     |           |
     *                     |           |
     *        -------------|--------------->
     *    ____|_________   |           |
     next view|    |_        |  |          _|
     *   |______\_______|__|_________/
     *            --_|_____|_______-- | previous view
     *               |_____|__________|
     *                     |
     *                  +y V
     *
     *     5. We check if next view is "to the left" of previous view
     *                  -y |
     *                     |
     *                     |______
     *                     |      --_
     *                     |         \_      x
     *                     |           |
     *                     |           |
     *        -------------|--------------->
     *    ____|_________   |           |
     next view|_        |  |          _|
     *   |______\_______|__|_________/
     *            --____|__|_______-- | previous view
     *                  |__|__________|
     *                     |
     *                  +y V
     *
     *     5. If any condition from 3, 4, 5 match then we found a center on the circle for the next view.
     */
    @Override
    public Point findNextViewCenter(ViewData previousViewData, int nextViewHalfViewWidth, int nextViewHalfViewHeight) {

        Point previousViewCenter = previousViewData.getCenterPoint();

        Point nextViewCenter;

        boolean foundNextViewCenter;
        do {

            /** 1. */
            nextViewCenter = getNextViewCenter(previousViewCenter);

            int nextViewTop = nextViewCenter.getY() - nextViewHalfViewHeight;
            int nextViewBottom = nextViewCenter.getY() + nextViewHalfViewHeight;
            int nextViewRight = nextViewCenter.getX() + nextViewHalfViewWidth;
            int nextViewLeft = nextViewCenter.getX() - nextViewHalfViewWidth;

            /** 3. */
            boolean nextViewTopIsBelowPreviousViewBottom = nextViewTop >= previousViewData.getViewBottom();
            /** 4. */
            boolean nextViewBottomIsAbovePreviousViewTop = nextViewBottom <= previousViewData.getViewTop();
            /** 5. */
            boolean nextViewIsToTheLeftOfThePreviousView = nextViewRight <= previousViewData.getViewLeft();

            boolean nextViewIsToTheRightOfThePreviousView = nextViewLeft >= previousViewData.getViewRight();

            foundNextViewCenter = nextViewTopIsBelowPreviousViewBottom || nextViewIsToTheLeftOfThePreviousView || nextViewBottomIsAbovePreviousViewTop || nextViewIsToTheRightOfThePreviousView;

            // "next view center" become previous
            previousViewCenter = nextViewCenter;
        } while (!foundNextViewCenter);

        return nextViewCenter;
    }

    /**
     * We start from previous view center point.
     * Here is the flow :
     *
     * 1. We get an index of previousViewCenter
     * 2. We increment the index.
     * 3. Correct received index. We might reach zero of last index
     * 4. We get next point using index
     *
     */
    private Point getNextViewCenter(Point previousViewCenter) {

        /** 1. */
        int previousViewCenterPointIndex = mCirclePointIndex.get(previousViewCenter);

        /** 2. */
        int newIndex = previousViewCenterPointIndex + 1;
        int lastIndex = mCircleIndexPoint.size() - 1;

        /** 3. if index is bigger than last index mean we exceeded the the limit and should start from zero. New index should be at the circle points start*/
        int nextViewCenterPointIndex = newIndex > lastIndex ?
                newIndex - lastIndex :
                newIndex;

        /** 4. */
        Point nextViewCenter = mCircleIndexPoint.get(nextViewCenterPointIndex);

//        if(SHOW_LOGS) Log.v(TAG, "getNextViewCenter, nextViewCenter " + nextViewCenter);
        return nextViewCenter;
    }

    private Point getPreviousViewCenter(Point nextViewCenter) {
        if (SHOW_LOGS) Log.v(TAG, ">> getPreviousViewCenter");

        /** 1. */
        int nextViewCenterPointIndex = mCirclePointIndex.get(nextViewCenter);
        if (SHOW_LOGS) Log.v(TAG, "getPreviousViewCenter, nextViewCenterPointIndex " + nextViewCenterPointIndex);

        /** 2. */
        int newIndex = nextViewCenterPointIndex - 1;
        if (SHOW_LOGS) Log.v(TAG, "getPreviousViewCenter, newIndex " + newIndex);

        int lastIndex = mCircleIndexPoint.size() - 1;
        if (SHOW_LOGS) Log.v(TAG, "getPreviousViewCenter, lastIndex " + lastIndex);

        /** 3. */
        int previousViewCenterPointIndex = newIndex < 0 ?
                lastIndex + newIndex: // this will subtract newIndex from last index
                newIndex;
        if (SHOW_LOGS) Log.v(TAG, "getPreviousViewCenter, previousViewCenterPointIndex " + previousViewCenterPointIndex);

        /** 4. */
        if (SHOW_LOGS) Log.v(TAG, "<< getPreviousViewCenter");
        return mCircleIndexPoint.get(previousViewCenterPointIndex);
    }


    @Override
    public Point findPreviousViewCenter(ViewData nextViewData, int previousViewHalfViewHeight, int previousViewHalfViewWidth) {

        Point nextViewCenter = nextViewData.getCenterPoint();

        Point previousViewCenter;

        boolean foundPreviousViewCenter;
        do {
            /** 1.*/
            previousViewCenter = getPreviousViewCenter(nextViewCenter);

            /** 2. */
            int previousViewTop = previousViewCenter.getY() - previousViewHalfViewHeight;
            int previousViewBottom = previousViewCenter.getY() + previousViewHalfViewHeight;
            int previousViewRight = previousViewCenter.getX() + previousViewHalfViewWidth;
            int previousViewLeft = previousViewCenter.getX() - previousViewHalfViewWidth;

            boolean previousViewTopIsBelowNextViewBottom = previousViewTop > nextViewData.getViewBottom();
            /** 4. */
            boolean previousViewBottomIsAboveNextViewTop = previousViewBottom < nextViewData.getViewTop();
            /** 5. */
            boolean previousViewIsToTheLeftOfTheNextView = previousViewRight < nextViewData.getViewLeft();

            boolean previousViewIsToTheRightOfTheNextView = previousViewLeft > nextViewData.getViewRight();

            foundPreviousViewCenter = previousViewTopIsBelowNextViewBottom || previousViewBottomIsAboveNextViewTop || previousViewIsToTheLeftOfTheNextView || previousViewIsToTheRightOfTheNextView;

            // "previous view center" become next
            nextViewCenter = previousViewCenter;
        } while (!foundPreviousViewCenter);

        return nextViewCenter;
    }

        @Override
    public int getViewCenterPointIndex(Point point) {
        return mCirclePointIndex.get(point);
    }

    @Override
    public Point getViewCenterPoint(int newCenterPointIndex) {
        return mCircleIndexPoint.get(
                newCenterPointIndex
        );
    }

    @Override
    public int getNewCenterPointIndex(int newCalculatedIndex) {

        int lastIndex = mCircleIndexPoint.size() - 1;
        int correctedIndex;
        if(newCalculatedIndex < 0){
            correctedIndex = lastIndex + newCalculatedIndex;
        } else {
            correctedIndex = newCalculatedIndex > lastIndex ?
                    newCalculatedIndex - lastIndex :
                    newCalculatedIndex;
        }

        return correctedIndex;
    }

    /**
     * This method checks if this is last visible layouted view.
     * The return might be used to know if we should stop laying out
     * TODO: use this method in Scroll Handler
     */
    @Override
    public boolean isLastLayoutedView(int recyclerWidth, View view) {
        boolean isLastLayoutedView;
        if(SHOW_LOGS) Log.v(TAG, "isLastLaidOutView, recyclerWidth " + recyclerWidth);
        int spaceToRightEdge = view.getRight();
        if(SHOW_LOGS) Log.v(TAG, "isLastLaidOutView, spaceToRightEdge " + spaceToRightEdge);
        isLastLayoutedView = spaceToRightEdge >= recyclerWidth;
        if(SHOW_LOGS) Log.v(TAG, "isLastLaidOutView, " + isLastLayoutedView);
        return isLastLayoutedView;
    }

    @Override
    public int checkBoundsReached(int recyclerWidth, int dy, View firstView, View lastView, boolean isFirstItemReached, boolean isLastItemReached) {
        int delta;
        if (SHOW_LOGS) {
            Log.v(TAG, "checkBoundsReached, isFirstItemReached " + isFirstItemReached);
            Log.v(TAG, "checkBoundsReached, isLastItemReached " + isLastItemReached);
        }
        if (dy > 0) { // Contents are scrolling up
            //Check against bottom bound
            if (isLastItemReached) {
                //If we've reached the last row, enforce limits
                int rightOffset = getOffset(recyclerWidth, lastView);
                delta = Math.max(-dy, rightOffset);
            } else {

                delta = -dy;
            }
        } else { // Contents are scrolling down
            if (SHOW_LOGS) Log.v(TAG, "checkBoundsReached, dy " + dy);

            if (isFirstItemReached) {
                int rightOffset = getOffset(recyclerWidth, firstView);
                delta = -Math.max(dy, rightOffset);
            } else {
                delta = -dy;
            }
        }
        if (SHOW_LOGS) Log.v(TAG, "checkBoundsReached, delta " + delta);
        return delta;
    }

    @Override
    public int getOffset(int recyclerWidth, View lastView) {

        int offset = recyclerWidth - lastView.getRight();

        if (SHOW_LOGS) {
            Log.v(TAG, "getOffset, offset" + offset);
        }
        return offset;
    }
}