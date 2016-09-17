package com.example.anna.simplelayoutmanager;


import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

/**
 * Created by anna on 9/12/16.
 */

public class SimpleLayoutManager extends RecyclerView.LayoutManager {

    private static final float VIEW_HEIGHT_PERCENT = 0.70f;

    private SparseArray<View> viewCache = new SparseArray<>();

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        fill(recycler);
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        offsetChildrenVertical(-dy);

        log("\n****************************************************************************\n");

        fill(recycler);
        return dy;
    }

    private void fill(RecyclerView.Recycler recycler) {

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

        //Why fill Up and Down?????????????????????????????????????????????????
        fillUp(anchorView, recycler);
        fillDown(anchorView, recycler);

        //отправляем в корзину всё, что не потребовалось в этом цикле лэйаута
        //эти вьюшки или ушли за экран или не понадобились, потому что соответствующие элементы
        //удалились из адаптера
        for (int i=0; i < viewCache.size(); i++) {
            recycler.recycleView(viewCache.valueAt(i));
        }

    }

    private void fillUp(@Nullable View anchorView, RecyclerView.Recycler recycler) {
        int anchorPos = 0;
        int anchorTop = 0;
        if (anchorView != null){
            anchorPos = getPosition(anchorView);
            anchorTop = getDecoratedTop(anchorView);
        }

        log("fillUp anchorPos = " + anchorPos);

        boolean fillUp = true;
        int pos = anchorPos - 1;
        if (pos < 0){
            pos = getItemCount() - 1;
        }
        int viewBottom = anchorTop; //нижняя граница следующей вьюшки будет начитаться от верхней границы предыдущей

        while (fillUp && pos >= 0){
            View view = viewCache.get(pos); //проверяем кэш
            if (view == null){
                log("fillUp view == null : pos = " + pos);
                //если вьюшки нет в кэше - просим у recycler новую, измеряем и лэйаутим её
                view = recycler.getViewForPosition(pos);
                addView(view, 0);
                measureChildWithMargins(view, 0, 0);
                int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);
                int decoratedMeasuredHeight = getDecoratedMeasuredHeight(view);
                layoutDecorated(view, 0, viewBottom - decoratedMeasuredHeight, decoratedMeasuredWidth, viewBottom);
            } else {
                log("fillUp view != null : pos = " + pos);
                //если вьюшка есть в кэше - просто аттачим её обратно
                //нет необходимости проводить measure/layout цикл.
                attachView(view);
                viewCache.remove(pos);
            }
            viewBottom = getDecoratedTop(view); // If we assert this pos >= 0, Why we need this line????
            fillUp = (viewBottom > 0);
            pos--;
            if (pos < 0){
                pos = getItemCount() - 1;
            }
        }
    }

    private void fillDown(@Nullable View anchorView, RecyclerView.Recycler recycler) {
        int anchorPos = 0;
        int anchorTop = 0;
        if (anchorView != null){
            anchorPos = getPosition(anchorView);
            anchorTop = getDecoratedTop(anchorView); // Why Top?????
        }

        log("fillDown anchorPos = " + anchorPos);

        int pos = anchorPos;
        boolean fillDown = true;
        int height = getHeight();
        int viewTop = anchorTop;
        int itemCount = getItemCount();

        while (fillDown && pos < itemCount){
            View view = viewCache.get(pos);
            if (view == null){
                log("fillDown && view == null : pos = " + pos + ", itemCount = " + itemCount);
                view = recycler.getViewForPosition(pos);
                addView(view);
//                measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec);
                measureChildWithMargins(view, 0, 0);
                int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);
                int decoratedMeasuredHeight = getDecoratedMeasuredHeight(view);
                layoutDecorated(view, 0, viewTop, decoratedMeasuredWidth, viewTop + decoratedMeasuredHeight);
            } else {
                log("fillDown && view != null : pos = " + pos + ", itemCount = " + itemCount);
                attachView(view);
                viewCache.remove(pos);
            }
            viewTop = getDecoratedBottom(view); // ??????????????????????????????????
            fillDown = viewTop <= height;

            if(pos == itemCount -1){
                pos = 0;
            } else {
                pos++;
            }
        }
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

