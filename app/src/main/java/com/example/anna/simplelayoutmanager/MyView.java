package com.example.anna.simplelayoutmanager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

/**
 * Created by anna on 9/13/16.
 */

public class MyView extends View {


    private Paint mPaint;

    private List<MyPoint> points;

    public MyView(Context context) {
        this(context, null);
    }

    public MyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d("testt", "onSizeChanged - w = " + w + ", h = " + h);

        int x = w;
        int y = h/2;
        int radius = w/2 + 100;

        points = PointsGenerator.generatePoints(x, y, radius);

        super.onSizeChanged(w, h, oldw, oldh);
    }

    int position = 0;

    public MyPoint getNextPoint(){
        return points.get(position++);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d("testt", "onDraw " + position + ", max " + points.size());

        for(int i = 0; i < points.size(); i++) {

            MyPoint p = points.get(i);
            Log.d("testt", "x = " + p.x + ", y = " + p.y);
            canvas.drawPoint(p.x, p.y, mPaint);
        }
    }

    public List<MyPoint> getPoints() {
        return points;
    }
}
