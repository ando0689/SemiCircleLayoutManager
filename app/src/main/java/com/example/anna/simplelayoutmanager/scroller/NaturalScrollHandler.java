package com.example.anna.simplelayoutmanager.scroller;

import android.view.View;

import com.example.anna.simplelayoutmanager.circule.CircleHelperInterface;
import com.example.anna.simplelayoutmanager.layouter.Layouter;

/**
 * Created by andranik on 9/21/16.
 */

public class NaturalScrollHandler extends ScrollHandler {

    private final ScrollHandlerCallback mCallback;

    public NaturalScrollHandler(ScrollHandlerCallback callback, CircleHelperInterface quadrantHelper, Layouter layouter) {
        super(callback, quadrantHelper, layouter);
        mCallback = callback;
    }

    @Override
    protected void scrollViews(View firstView, int delta) {
        for (int indexOfView = 0; indexOfView < mCallback.getChildCount(); indexOfView++) {
            View view = mCallback.getChildAt(indexOfView);
            scrollSingleViewVerticallyBy(view, delta);
        }
    }
}