package com.example.anna.simplelayoutmanager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anna on 9/12/16.
 */

public class TextAdapter extends RecyclerView.Adapter<TextHolder> {

    private Context mContext;

    private List<String> mTextList = new ArrayList<>();

    public TextAdapter(Context context, List<String> textList) {
        mContext = context;
        mTextList = textList;
    }

    @Override
    public TextHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        return new TextHolder(view);
    }

    @Override
    public void onBindViewHolder(TextHolder holder, int position) {
        holder.bindData(mTextList.get(position));
    }

    @Override
    public int getItemCount() {
        return mTextList.size();
    }
}
