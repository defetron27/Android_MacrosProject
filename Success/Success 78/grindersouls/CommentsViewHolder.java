package com.deffe.macros.grindersouls;

import android.view.View;
import android.widget.TextView;

public class CommentsViewHolder extends ChildViewHolder
{
    private TextView commentTextView;

    CommentsViewHolder(View itemView) {
        super(itemView);
        commentTextView = itemView.findViewById(R.id.comment_text_view);
    }

    public void bind(Comments comments)
    {
        commentTextView.setText(comments.getComment());
    }
}
