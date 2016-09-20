package com.example.anna.simplelayoutmanager;


import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anna on 9/12/16.
 */

public class SimpleLayoutManager extends RecyclerView.LayoutManager {

    private int mDecoratedChildWidth;
    private int mDecoratedChildHeight;

    private SparseArray<View> viewCache = new SparseArray<>();

    private List<MyPoint> mPoints;
    private HashMap<MyPoint, Integer> pointToIndexMap;

    public SimpleLayoutManager(List<MyPoint> points) {
        this.mPoints = points;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        initViewSizes(recycler);
        addMissingPoints();
        initMaps();
        detachAndScrapAttachedViews(recycler);
        circleFill(recycler);
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }


    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        scrollViews(-dy);

        circleFill(recycler);
        return dy;
    }

    /* Jus initializes the width and height of the child views */
    private void initViewSizes(RecyclerView.Recycler recycler){
        if (getChildCount() == 0) { //First or empty layout
            //Scrap measure one child
            View scrap = recycler.getViewForPosition(0);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);

            mDecoratedChildWidth = getDecoratedMeasuredWidth(scrap);
            mDecoratedChildHeight = getDecoratedMeasuredHeight(scrap);

            detachAndScrapView(scrap, recycler);
        }
    }

    /* Adding points from start and end of the points array which will be out of screen because we need to move center of view out of screen */
    private void addMissingPoints(){
        final ArrayList<MyPoint> newPoints = new ArrayList<>();

        int missingPointsCount = (mDecoratedChildWidth / 2);

        final MyPoint firstPoint = mPoints.get(0);
        final MyPoint lastPoint = mPoints.get(mPoints.size() - 1);

        // append points from start
        for (int i = missingPointsCount; i > 0; i--){
            newPoints.add(new MyPoint(firstPoint.x + i, firstPoint.y));
        }

        // add existing points
        newPoints.addAll(mPoints);

        // append points from end
        for (int i = 1; i < missingPointsCount; i++){
            newPoints.add(new MyPoint(lastPoint.x + i, lastPoint.y));
        }

        mPoints = newPoints;
    }

    private void initMaps(){
        pointToIndexMap = new HashMap<>();

        for (int i = 0; i < mPoints.size(); i++){
            final MyPoint p = mPoints.get(i);
            pointToIndexMap.put(p, i);
        }
    }
//
//    private void fill(RecyclerView.Recycler recycler) {
//
//        View anchorView = getAnchorView();
//        viewCache.clear();
//
//        //Помещаем вьюшки в кэш и...
//        for (int i = 0, cnt = getChildCount(); i < cnt; i++) { // Why not i < getChildCount() ?????
//            View view = getChildAt(i);
//            int pos = getPosition(view);
//            viewCache.put(pos, view);
//        }
//
//        //... и удалям из лэйаута
//        for (int i = 0; i < viewCache.size(); i++) {
//            detachView(viewCache.valueAt(i)); // How it can detach Views from cache, not real Views???
//        }
//
//        //Why fill Up and Down?????????????????????????????????????????????????
//        fillUp(anchorView, recycler);
//        fillDown(anchorView, recycler);
//
//        //отправляем в корзину всё, что не потребовалось в этом цикле лэйаута
//        //эти вьюшки или ушли за экран или не понадобились, потому что соответствующие элементы
//        //удалились из адаптера
//        for (int i=0; i < viewCache.size(); i++) {
//            recycler.recycleView(viewCache.valueAt(i));
//        }
//    }
//
//    private void fillUp(@Nullable View anchorView, RecyclerView.Recycler recycler) {
//        int anchorPos = 0;
//        int anchorTop = 0;
//        if (anchorView != null){
//            anchorPos = getPosition(anchorView);
//            anchorTop = getDecoratedTop(anchorView);
//        }
//
//        log("fillUp anchorPos = " + anchorPos);
//
//        boolean fillUp = true;
//        int pos = anchorPos - 1;
//        if (pos < 0){
//            pos = getItemCount() - 1;
//        }
//        int viewBottom = anchorTop; //нижняя граница следующей вьюшки будет начитаться от верхней границы предыдущей
//
//        while (fillUp && pos >= 0){
//            View view = viewCache.get(pos); //проверяем кэш
//            if (view == null){
//                log("fillUp view == null : pos = " + pos);
//                //если вьюшки нет в кэше - просим у recycler новую, измеряем и лэйаутим её
//                view = recycler.getViewForPosition(pos);
//                addView(view, 0);
//                measureChildWithMargins(view, 0, 0);
//
//                layoutDecorated(view, 0, viewBottom - mDecoratedChildHeight, mDecoratedChildWidth, viewBottom);
//            } else {
//                log("fillUp view != null : pos = " + pos);
//                //если вьюшка есть в кэше - просто аттачим её обратно
//                //нет необходимости проводить measure/layout цикл.
//                attachView(view);
//                viewCache.remove(pos);
//            }
//            viewBottom = getDecoratedTop(view); // If we assert this pos >= 0, Why we need this line????
//            fillUp = (viewBottom > 0);
//            pos--;
//            if (pos < 0){
//                pos = getItemCount() - 1;
//            }
//        }
//    }
//
//    private void fillDown(@Nullable View anchorView, RecyclerView.Recycler recycler) {
//        int anchorPos = 0;
//        int anchorTop = 0;
//        if (anchorView != null){
//            anchorPos = getPosition(anchorView);
//            anchorTop = getDecoratedTop(anchorView); // Why Top?????
//        }
//
//        log("fillDown anchorPos = " + anchorPos);
//
//        int pos = anchorPos;
//        boolean fillDown = true;
//        int height = getHeight();
//        int viewTop = anchorTop;
//        int itemCount = getItemCount();
//
//        while (fillDown && pos < itemCount){
//            View view = viewCache.get(pos);
//            if (view == null){
//                log("fillDown && view == null : pos = " + pos + ", itemCount = " + itemCount);
//                view = recycler.getViewForPosition(pos);
//                addView(view);
//                measureChildWithMargins(view, 0, 0);
//
//                layoutDecorated(view, 0, viewTop, mDecoratedChildWidth, viewTop + mDecoratedChildHeight);
//            } else {
//                log("fillDown && view != null : pos = " + pos + ", itemCount = " + itemCount);
//                attachView(view);
//                viewCache.remove(pos);
//            }
//            viewTop = getDecoratedBottom(view); // ??????????????????????????????????
//            fillDown = viewTop <= height;
//
//            if(pos == itemCount -1){
//                pos = 0;
//            } else {
//                pos++;
//            }
//        }
//    }

    private void circleFill(RecyclerView.Recycler recycler){
        View anchorView = getAnchorView();
        viewCache.clear();

        //Помещаем вьюшки в кэш и...
        for (int i = 0, cnt = getChildCount(); i < cnt; i++) { // Why not i < getChildCount() ?????
            View view = getChildAt(i);
            int pos = getPosition(view);
            viewCache.put(pos, view);
        }

        //... и удалям из лэйаута
        for (int i = 0; i < viewCache.size(); i++) {
            detachView(viewCache.valueAt(i)); // How it can detach Views from cache, not real Views???
        }

        circleFillUp(anchorView, recycler);
        circleFillDown(anchorView, recycler);

        //отправляем в корзину всё, что не потребовалось в этом цикле лэйаута
        //эти вьюшки или ушли за экран или не понадобились, потому что соответствующие элементы
        //удалились из адаптера
        for (int i=0; i < viewCache.size(); i++) {
            recycler.recycleView(viewCache.valueAt(i));
        }
    }

    ViewData upPreviousViewData;

    private void circleFillUp(@Nullable View anchorView, RecyclerView.Recycler recycler){
        int anchorPos = 0;
        int anchorRight = getWidth();
        if (anchorView != null){
            anchorPos = getPosition(anchorView);
            anchorRight = getDecoratedRight(anchorView);
        }

        boolean fillUp = true;
        int pos = anchorPos - 1;

        if (pos < 0){
            pos = getItemCount() - 1;
        }

        int viewRight = anchorRight;

        int upIndexOfViewPoint = 0;

//        if(upPreviousViewData == null) {
            upPreviousViewData = new ViewData(0, 0, 0, 0, mPoints.get(0));
//        }

        while (fillUp && pos >= 0){
            log("circleFillUp - anchorPos = " + anchorPos);
            View view = viewCache.get(pos);


            if (view == null){
                view = recycler.getViewForPosition(pos);
                addView(view, 0);
                measureChildWithMargins(view, 0, 0);

                upIndexOfViewPoint = doLayoutUp(view, upPreviousViewData);
                upPreviousViewData.updateData(view, mPoints.get(upIndexOfViewPoint));
            } else {
                attachView(view);
                viewCache.remove(pos);
            }

            viewRight = getDecoratedRight(view); // If we assert this pos >= 0, Why we need this line????
            fillUp = viewRight <= getWidth();
            pos--;
            if (pos < 0 && fillUp){
                pos = getItemCount() - 1;
            }
        }
    }


    ViewData downPreviousViewData;

    private void circleFillDown(@Nullable View anchorView, RecyclerView.Recycler recycler){
        int anchorPos = 0;
        int anchorRight = getWidth();

        if (anchorView != null){
            anchorPos = getPosition(anchorView);
            anchorRight = getDecoratedRight(anchorView);
        }

        int pos = anchorPos;
        boolean fillDown = true;
        int viewRight = anchorRight;
        int itemCount = getItemCount();

        int downIndexOfViewPoint = 0;

//        if(downPreviousViewData == null) {
            downPreviousViewData = new ViewData(0, 0, 0, 0, mPoints.get(0));
//        }

        while (fillDown && pos < itemCount){
            log("circleFillDown - anchorPos = " + anchorPos);
            View view = viewCache.get(pos);

            if (view == null){
                view = recycler.getViewForPosition(pos);
                addView(view);
                measureChildWithMargins(view, 0, 0);

                downIndexOfViewPoint = doLayoutDown(view, downPreviousViewData);
                downPreviousViewData.updateData(view, mPoints.get(downIndexOfViewPoint));
            } else {
                attachView(view);
                viewCache.remove(pos);
            }

            viewRight = getDecoratedRight(view);
            fillDown = viewRight <= getWidth();

            if(pos == itemCount -1){
                pos = 0;
            } else {
                pos++;
            }
        }
    }


    private int doLayoutUp(View child, ViewData previusViewData){
        int indexOfNexViewPoint = findIndexOfPreviousViewPoint(previusViewData);

        MyPoint point = mPoints.get(indexOfNexViewPoint);

        final int newLeft = point.x - (mDecoratedChildWidth / 2);
        final int newTop = point.y - (mDecoratedChildHeight / 2);
        final int newRight = newLeft + mDecoratedChildWidth;
        final int newBottom = newTop + mDecoratedChildHeight;

        log("test123 doLayoutUp point: " + point.toString());

        layoutDecorated(child, newLeft, newTop, newRight, newBottom);

        return indexOfNexViewPoint;
    }

    private int doLayoutDown(View child, ViewData previusViewData){
        int indexOfNexViewPoint = findIndexOfNextViewPoint(previusViewData);

        MyPoint point = mPoints.get(indexOfNexViewPoint);

        final int newLeft = point.x - (mDecoratedChildWidth / 2);
        final int newTop = point.y - (mDecoratedChildHeight / 2);
        final int newRight = newLeft + mDecoratedChildWidth;
        final int newBottom = newTop + mDecoratedChildHeight;

        log("test123 doLayoutDown point: " + point.toString());

        layoutDecorated(child, newLeft, newTop, newRight, newBottom);

        return indexOfNexViewPoint;
    }


    private int findIndexOfPreviousViewPoint(ViewData previousVideData){
//        MyPoint previousPoint = previousVideData.getCenterPoint();
//
//        for (int i = previousVideData.getPointIndex(); i < mPoints.size(); i++){
//            MyPoint p = mPoints.get(i);
//
//            boolean havePlaceX = i < mPoints.size() / 2 ? p.x > previousPoint.x - mDecoratedChildWidth : p.x < previousPoint.x + mDecoratedChildWidth;
//            boolean havePlaceY = p.y < previousPoint.y + mDecoratedChildHeight;
//
//            if(havePlaceX || havePlaceY){
//                log("found previous index " + i + ", havePlaceX: " + havePlaceX + ", havePlaceY: " + havePlaceY + ", pointX = " + p.x);
//                return i;
//            }
//        }
//
//
//        log("findIndexOfPreviousViewPoint = " + previousVideData.getPointIndex());
//
//        return 0;

        MyPoint point = findPreviousViewCenter(previousVideData, mDecoratedChildHeight / 2, mDecoratedChildWidth / 2);
        return pointToIndexMap.get(point);
    }

    private int findIndexOfNextViewPoint(ViewData previousVideData){
//        MyPoint previousPoint = previousVideData.getCenterPoint();
//
//        for (int i = previousVideData.getPointIndex(); i < mPoints.size(); i++){
//            MyPoint p = mPoints.get(i);
//
//            boolean havePlaceX = i < mPoints.size() / 2 ? p.x < previousPoint.x - mDecoratedChildWidth : p.x > previousPoint.x + mDecoratedChildWidth;
//            boolean havePlaceY = p.y > previousPoint.y + mDecoratedChildHeight;
//
//            if(havePlaceX || havePlaceY){
//                log("found next index " + i + ", havePlaceX: " + havePlaceX + ", havePlaceY: " + havePlaceY + ", pointX = " + p.x);
//                return i;
//            }
//        }
//
//        log("findIndexOfNextViewPoint = " + previousVideData.getPointIndex());
//
//        return 0;


        MyPoint point = findNextViewCenter(previousVideData, mDecoratedChildWidth / 2, mDecoratedChildHeight / 2);

        return pointToIndexMap.get(point);
    }


    public MyPoint findNextViewCenter(ViewData previousViewData, int nextViewHalfViewWidth, int nextViewHalfViewHeight) {

        MyPoint previousViewCenter = previousViewData.getCenterPoint();

        MyPoint nextViewCenter;

        boolean foundNextViewCenter;
        do {

            /** 1. */
            nextViewCenter = getNextViewCenter(previousViewCenter);

            /** 2. */
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

            if(foundNextViewCenter){
                log("foundNextViewCenter : nextViewTopIsBelowPreviousViewBottom = " + nextViewTopIsBelowPreviousViewBottom
                    + ", nextViewBottomIsAbovePreviousViewTop = " + nextViewBottomIsAbovePreviousViewTop
                    + ", nextViewIsToTheLeftOfThePreviousView = " + nextViewIsToTheLeftOfThePreviousView
                    + ", nextViewIsToTheRightOfThePreviousView = " + nextViewIsToTheRightOfThePreviousView);
            }

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
    private MyPoint getNextViewCenter(MyPoint previousViewCenter) {

        /** 1. */
        int previousViewCenterPointIndex = pointToIndexMap.get(previousViewCenter);

        log("getNextViewCenter previousViewCenterPointIndex = " + previousViewCenterPointIndex);

        /** 2. */
        int newIndex = previousViewCenterPointIndex + 1;
        int lastIndex = pointToIndexMap.size() - 1;

        /** 3. if index is bigger than last index mean we exceeded the the limit and should start from zero. New index should be at the circle points start*/
        int nextViewCenterPointIndex = newIndex > lastIndex ?
                newIndex - lastIndex :
                newIndex;

        /** 4. */
        MyPoint nextViewCenter = mPoints.get(nextViewCenterPointIndex);

//        if(SHOW_LOGS) Log.v(TAG, "getNextViewCenter, nextViewCenter " + nextViewCenter);
        return nextViewCenter;
    }


    public MyPoint findPreviousViewCenter(ViewData nextViewData, int previousViewHalfViewHeight, int previousViewHalfViewWidth) {

        MyPoint nextViewCenter = nextViewData.getCenterPoint();

        MyPoint previousViewCenter;

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

            if(foundPreviousViewCenter){
                log("foundPreviousViewCenter : previousViewTopIsBelowNextViewBottom = " + previousViewTopIsBelowNextViewBottom
                        + ", previousViewBottomIsAboveNextViewTop = " + previousViewBottomIsAboveNextViewTop
                        + ", previousViewIsToTheLeftOfTheNextView = " + previousViewIsToTheLeftOfTheNextView
                        + ", previousViewIsToTheRightOfTheNextView = " + previousViewIsToTheRightOfTheNextView);
            }

            // "previous view center" become next
            nextViewCenter = previousViewCenter;
        } while (!foundPreviousViewCenter);

        return nextViewCenter;
    }

    private MyPoint getPreviousViewCenter(MyPoint nextViewCenter) {
        /** 1. */
        int nextViewCenterPointIndex = pointToIndexMap.get(nextViewCenter);

        /** 2. */
        int newIndex = nextViewCenterPointIndex - 1;

        int lastIndex = pointToIndexMap.size() - 1;

        /** 3. */
        int previousViewCenterPointIndex = newIndex < 0 ?
                lastIndex + newIndex: // this will subtract newIndex from last index
                newIndex;

        /** 4. */
        return mPoints.get(previousViewCenterPointIndex);
    }


    /**
     * This is a helper object that will be updated many times while scrolling.
     * We use this to reduce memory consumption, which means less GC will kicks of less times :)
     */
    private final static UpdatablePoint SCROLL_HELPER_POINT = new UpdatablePoint(0, 0);

    protected void scrollViews(int delta) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            TextView tv = (TextView) view.findViewById(R.id.textView);

            int viewCenterX = view.getRight() - view.getWidth() / 2;
            int viewCenterY = view.getTop() + view.getHeight() / 2;
            log("test123 - " + tv.getText().toString() + ", x = " + viewCenterX + ", y = " + viewCenterY);

            scrollSingleViewVerticallyBy(view, delta);
        }
    }

    protected void scrollSingleViewVerticallyBy(View view, int indexOffset) {
        int viewCenterX = view.getRight() - view.getWidth() / 2;
        int viewCenterY = view.getTop() + view.getHeight() / 2;

        findPoint(viewCenterX, viewCenterY);

        log("test123 scrollSingleViewVerticallyBy point: " + SCROLL_HELPER_POINT.toString());

        if(!pointToIndexMap.containsKey(SCROLL_HELPER_POINT)){
            log("BAD SCROLL_HELPER_POINT - " + SCROLL_HELPER_POINT.toString());
            return;
        }

        int centerPointIndex = pointToIndexMap.get(SCROLL_HELPER_POINT);

        int newCenterPointIndex = getNewCenterPointIndex(centerPointIndex + indexOffset);

        MyPoint newCenterPoint = mPoints.get(newCenterPointIndex);

        int dx = newCenterPoint.x - viewCenterX;
        int dy = newCenterPoint.y - viewCenterY;

        view.offsetTopAndBottom(dy);
        view.offsetLeftAndRight(dx);
    }

    private void findPoint(int viewCenterX, int viewCenterY){
        for (int i = -1; i < 2; i++){
            for (int j = -1; j < 2; j++){
                SCROLL_HELPER_POINT.update(viewCenterX + i, viewCenterY + j);
                if(pointToIndexMap.containsKey(SCROLL_HELPER_POINT)){
                    return;
                }
            }
        }
    }

    public int getNewCenterPointIndex(int newCalculatedIndex) {

        int lastIndex = mPoints.size() - 1;
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

/*    Выбираем одну из имеющихся в лэйауте вьюшек как “якорную” и запоминаем её и её позицию.
    В нашем случае мы будем выбирать в качестве якорной вьюшки ту, которая полностью видна на экране.
    Если такой нет, то выбираем ту, видимая площадь которой максимальна. Такой способ определения якорной
    вьюшки поможет нам и в будущем, при реализации смены ориентации нашего лэйаут-менеджера*/

    //метод вернет вьюшку с максимальной видимой площадью
    private View getAnchorView() {
        int childCount = getChildCount();
        HashMap<Integer, View> viewsOnScreen = new HashMap<>();
        Rect mainRect = new Rect(0, 0, getWidth(), getHeight());
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            int top = getDecoratedTop(view);
            int bottom = getDecoratedBottom(view);
            int left = getDecoratedLeft(view);
            int right = getDecoratedRight(view);
            Rect viewRect = new Rect(left, top, right, bottom);
            boolean intersect = viewRect.intersect(mainRect);
            if (intersect){
                int square = viewRect.width() * viewRect.height();
                viewsOnScreen.put(square, view);
            }
        }
        if (viewsOnScreen.isEmpty()){
            return null;
        }
        Integer maxSquare = null;
        for (Integer square : viewsOnScreen.keySet()) {
            if (maxSquare == null){
                maxSquare = square;
            } else {
                maxSquare = Math.max(maxSquare, square);
            }
        }
        return viewsOnScreen.get(maxSquare);
    }



    private void log(String msg){
        Log.d("testt", msg);
    }

}

