package com.example.anna.simplelayoutmanager;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;


/**
 * Created by anna on 9/12/16.
 */

public class TextHolder extends RecyclerView.ViewHolder {

    private TextView textView;

    public TextHolder(View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(R.id.textView);
    }

    public void bindData(String text){
        textView.setText(text);
    }
}
