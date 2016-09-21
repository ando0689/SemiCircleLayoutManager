package com.example.anna.simplelayoutmanager.scroller;

import android.support.v7.widget.RecyclerView;

import com.example.anna.simplelayoutmanager.circule.QuadrantHelper;
import com.example.anna.simplelayoutmanager.layouter.Layouter;

/**
 * Created by andranik on 9/21/16.
 */

public interface IScrollHandler {
    int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler);

    public enum Strategy{
        PIXEL_PERFECT,
        NATURAL
    }

    public static class Factory{

        private Factory(){}

        public static IScrollHandler createScrollHandler(Strategy strategy, ScrollHandlerCallback callback, QuadrantHelper quadrantHelper, Layouter layouter){
            IScrollHandler scrollHandler = null;
            switch (strategy){
                case PIXEL_PERFECT:
                    scrollHandler = new PixelPerfectScrollHandler(callback, quadrantHelper, layouter);
                    break;
                case NATURAL:
                    scrollHandler = new NaturalScrollHandler(callback, quadrantHelper, layouter);
                    break;
            }
            return  scrollHandler;
        }
    }
}
