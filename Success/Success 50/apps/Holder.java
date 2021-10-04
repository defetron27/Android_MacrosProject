package com.deffe.macros.apps;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class Holder extends RecyclerView.ViewHolder{

    public final TextView positionView;

    public Holder(View itemView) {
        super(itemView);
        positionView = (TextView) itemView.findViewById(R.id.position);
    }
}