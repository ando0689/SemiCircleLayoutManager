package com.example.anna.simplelayoutmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

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
        for(int i = 1; i <= 15; i++){
            mTextList.add("Simple Text " + i);
        }
        mAdapter = new TextAdapter(this, mTextList);
//       mLayoutManager =  new LinearLayoutManager(this);
//        mLayoutManager = new StaggeredGridLayoutManager(2, 1);
        mLayoutManager = new SimpleLayoutManager();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }
}
