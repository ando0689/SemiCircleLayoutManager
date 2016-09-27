package com.example.anna.simplelayoutmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import am.andranik.semicirclelayoutmanger.SemiCircularLayoutManager;

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

        for(int i = 1; i <= 15; i++){
            mTextList.add("" + i);
        }
        mAdapter = new TextAdapter(this, mTextList);

        mLayoutManager = new SemiCircularLayoutManager(mRecyclerView);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

    }
}
