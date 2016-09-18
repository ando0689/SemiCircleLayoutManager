package com.example.anna.simplelayoutmanager;

/**
 * Created by andranik on 9/18/16.
 */

public class UpdatablePoint extends MyPoint {
    public UpdatablePoint(int x, int y) {
        super(x, y);
    }

    public void update(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
