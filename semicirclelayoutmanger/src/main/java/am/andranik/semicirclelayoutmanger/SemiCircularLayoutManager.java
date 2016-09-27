package am.andranik.semicirclelayoutmanger;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import java.util.List;

import am.andranik.semicirclelayoutmanger.circle.CircleHelper;
import am.andranik.semicirclelayoutmanger.circle.CircleHelperInterface;
import am.andranik.semicirclelayoutmanger.layouter.Layouter;
import am.andranik.semicirclelayoutmanger.layouter.LayouterCallback;
import am.andranik.semicirclelayoutmanger.point.Point;
import am.andranik.semicirclelayoutmanger.point.PointsGenerator;
import am.andranik.semicirclelayoutmanger.scroller.IScrollHandler;
import am.andranik.semicirclelayoutmanger.scroller.ScrollHandlerCallback;
import am.andranik.semicirclelayoutmanger.utils.Config;
import am.andranik.semicirclelayoutmanger.utils.ViewData;

/**
 * Created by andranik on 9/21/16.
 */

public class SemiCircularLayoutManager extends RecyclerView.LayoutManager implements LayouterCallback, ScrollHandlerCallback {

    public static final int POINTS_TO_ADD_COUNT = 500;
    public static final boolean ENDLESS_SCROLL = true;

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = SemiCircularLayoutManager.class.getSimpleName();

    private RecyclerView mRecyclerView;

    private Layouter mLayouter;

    private IScrollHandler mScroller;
    private CircleHelperInterface mQuadrantHelper;

    /**
     * This is a helper value. We should always return "true" from {@link #canScrollVertically()} but we need to change this value to false when measuring a child view size.
     * This is because the height "match_parent" is not calculated correctly if {@link #canScrollHorizontally()} returns "true"
     * and
     * the height "match_parent" is not calculated correctly if {@link #canScrollVertically()} returns "true"
     */

    private boolean mCanScrollVertically = true;

    private boolean firstLayouted = false;

    private int mFirstVisiblePosition = 0; //TODO: implement save/restore state
    private int mLastVisiblePosition = 0; //TODO: implement save/restore state

    public SemiCircularLayoutManager(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    @Override
    public void getHitRect(Rect rect) {
        mRecyclerView.getHitRect(rect);
    }

    @Override
    public Pair<Integer, Integer> getHalfWidthHeightPair(View view) {

        Pair<Integer, Integer> widthHeight;
        measureChildWithMargins(view, 0, 0);

        int measuredWidth = getDecoratedMeasuredWidth(view);
        int measuredHeight = getDecoratedMeasuredHeight(view);

        if (SHOW_LOGS)
            Log.i(TAG, "getHalfWidthHeightPair, measuredWidth " + measuredWidth + ", measuredHeight " + measuredHeight);

        int halfViewHeight = measuredHeight / 2;
        if (SHOW_LOGS) Log.v(TAG, "getHalfWidthHeightPair, halfViewHeight " + halfViewHeight);

        int halfViewWidth = measuredWidth / 2;
        if (SHOW_LOGS) Log.v(TAG, "getHalfWidthHeightPair, halfViewWidth " + halfViewWidth);

        widthHeight = new Pair<>(
                halfViewWidth,
                halfViewHeight
        );
        return widthHeight;
    }

    @Override
    public boolean isEndlessScrollEnabled() {
        return ENDLESS_SCROLL;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean canScrollVertically() {
        return mCanScrollVertically;
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {

        if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy dy " + dy);
        int childCount = getChildCount();
        if(SHOW_LOGS) Log.v(TAG, "scrollVerticallyBy childCount " + childCount);

        if (childCount == 0) {
            // we cannot scroll if we don't have views
            return 0;
        }

        return mScroller.scrollVerticallyBy(dy, recycler);
    }

    private void initHelpers(){
        int x = getWidth();
        int y = getHeight()/2;
        int radius = x/2 + 100;

        List<Point> mPoints = PointsGenerator.generatePoints(x, y, radius);

        mQuadrantHelper = new CircleHelper(mPoints, POINTS_TO_ADD_COUNT);

        mLayouter = new Layouter(this, mQuadrantHelper);
        mScroller = IScrollHandler.Factory.createScrollHandler(
                IScrollHandler.Strategy.NATURAL,
                this,
                mQuadrantHelper,
                mLayouter);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if(SHOW_LOGS) Log.v(TAG, ">> onLayoutChildren, state " + state);
        initHelpers();

        //We have nothing to show for an empty data set but clear any existing views
        int itemCount = getItemCount();
        if (itemCount == 0) {
            removeAllViews();
            return;
        }

        removeAllViews();

        // TODO: These values should not be set to "0". They should be restored from state
        mLastVisiblePosition = 0;
        mFirstVisiblePosition = 0;

        if(SHOW_LOGS) {
            Log.v(TAG, "onLayoutChildren, state " + state);
            Log.v(TAG, "onLayoutChildren, mLastVisiblePosition " + mLastVisiblePosition);
        }

        ViewData viewData = new ViewData(0, 0, 0, 0,
                mQuadrantHelper.getViewCenterPoint(0)
        );

        if(!firstLayouted){
            firstLayouted = true;
            viewData = new ViewData(0, 0, 0, 0,
                    mQuadrantHelper.getViewCenterPoint(POINTS_TO_ADD_COUNT + 1)
            );
        }

        // It will be our stop flag
        boolean isLastLayoutedView;

        do{
            View view = recycler.getViewForPosition(mLastVisiblePosition);
            addView(view);
            viewData = mLayouter.layoutNextView(view, viewData);

            // We update coordinates instead of creating new object to keep the heap clean
            if (SHOW_LOGS) Log.v(TAG, "onLayoutChildren, viewData " + viewData);

            isLastLayoutedView = mLayouter.isLastLaidOutView(view);
            mLastVisiblePosition++;

        } while (!isLastLayoutedView && mLastVisiblePosition < itemCount);

        if (SHOW_LOGS) Log.v(TAG, "onLayoutChildren, mLastVisiblePosition " + mLastVisiblePosition);
    }

    /**
     * This is a wrapper method for {@link android.support.v7.widget.RecyclerView#measureChildWithMargins(View, int, int, int, int)}
     *
     * If capsules width is "match_parent" then we we need to return "false" from {@link #canScrollHorizontally()}
     * If capsules height is "match_parent" then we we need to return "false" from {@link #canScrollVertically()}
     *
     * This method simply changes return values of {@link #canScrollHorizontally()} and {@link #canScrollVertically()} while measuring
     * size of a child view
     */
    public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
        // change a value to "false "temporary while measuring
        mCanScrollVertically = false;

        super.measureChildWithMargins(child, widthUsed, heightUsed);

        // return a value to "true" because we do actually can scroll in both ways
        mCanScrollVertically = true;
    }

    @Override
    public int getFirstVisiblePosition() {
        if(isEndlessScrollEnabled() && mFirstVisiblePosition <= 0){
            mFirstVisiblePosition = getItemCount();
        }

        return mFirstVisiblePosition;
    }

    @Override
    public int getLastVisiblePosition() {
        if(isEndlessScrollEnabled() && mLastVisiblePosition >= getItemCount()){
            mLastVisiblePosition = 0;
        }
        return mLastVisiblePosition;
    }

    @Override
    public void incrementFirstVisiblePosition() {
        if(isEndlessScrollEnabled() && mFirstVisiblePosition == getItemCount()){
            mFirstVisiblePosition = 0;
        }
        mFirstVisiblePosition++;
    }

    @Override
    public void incrementLastVisiblePosition() {
        if(isEndlessScrollEnabled() && mLastVisiblePosition == getItemCount()){
            mLastVisiblePosition = 0;
        }
        mLastVisiblePosition++;
    }

    @Override
    public void decrementLastVisiblePosition() {
        if(isEndlessScrollEnabled() && mLastVisiblePosition == 0){
            mLastVisiblePosition = 1;
        }
        mLastVisiblePosition--;
    }

    @Override
    public void decrementFirstVisiblePosition() {
        if(isEndlessScrollEnabled() && mFirstVisiblePosition == 0){
            mFirstVisiblePosition = 1;
        }
        mFirstVisiblePosition--;
    }
}
