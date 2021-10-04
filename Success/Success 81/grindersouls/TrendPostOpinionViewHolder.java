package com.deffe.macros.grindersouls;

import android.view.View;
import android.widget.TextView;

public class TrendPostOpinionViewHolder extends ChildViewHolder
{
    private TextView opinionTextView;

    TrendPostOpinionViewHolder(View itemView) {
        super(itemView);
        opinionTextView = itemView.findViewById(R.id.comment_text_view);
    }

    public void bind(TrendPostOpinions trendPostOpinions)
    {
        opinionTextView.setText(trendPostOpinions.getOpinion());
    }
}
