package com.deffe.macros.grindersouls;

import android.view.View;
import android.widget.TextView;

public class CommentsViewHolder extends ChildViewHolder
{
    private TextView mMoviesTextView;

    CommentsViewHolder(View itemView) {
        super(itemView);
        mMoviesTextView = itemView.findViewById(R.id.comment);
    }

    public void bind(Comments comments) {
        mMoviesTextView.setText(comments.getUserComment());
    }
}
