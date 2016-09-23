package com.example.anna.simplelayoutmanager;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.anna.simplelayoutmanager.scroller.IScrollHandler;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

   private RecyclerView mRecyclerView;

    private TextAdapter mAdapter;

    private List<String> mTextList = new ArrayList<>();

    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.text_list);
        final MyView myView = (MyView) findViewById(R.id.circle_view);

        for(int i = 1; i <= 15; i++){
            mTextList.add("" + i);
        }
        mAdapter = new TextAdapter(this, mTextList);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mLayoutManager = new NewLayoutManager(
                        myView.getPoints(),
                        mRecyclerView,
                        IScrollHandler.Strategy.NATURAL);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapter);
                myView.setVisibility(View.GONE);
            }
        }, 1000);

    }
}
