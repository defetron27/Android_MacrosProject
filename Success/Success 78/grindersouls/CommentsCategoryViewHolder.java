package com.deffe.macros.grindersouls;

import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentsCategoryViewHolder extends ParentViewHolder
{
    private static final float INITIAL_POSITION = 0.0f;
    private static final float ROTATED_POSITION = 180f;

    private final ImageView mArrowExpandImageView;
    private TextView mCommentsTextView;
    private TextView mCommentsCountTextView;

    CommentsCategoryViewHolder(View itemView)
    {
        super(itemView);

        mCommentsTextView = itemView.findViewById(R.id.comments);

        mCommentsCountTextView = itemView.findViewById(R.id.comments_count);

        mArrowExpandImageView = itemView.findViewById(R.id.comments_expand_arrow);
    }

    public void bind(CommentsCategory commentsCategory)
    {
        mCommentsTextView.setText(commentsCategory.getComment());
        mCommentsCountTextView.setText(String.valueOf(commentsCategory.getChildItemList().size()));
    }

    @Override
    public void setExpanded(boolean expanded)
    {
        super.setExpanded(expanded);

        if (expanded)
        {
            mArrowExpandImageView.setRotation(ROTATED_POSITION);
        }
        else
        {
            mArrowExpandImageView.setRotation(INITIAL_POSITION);
        }
    }

    @Override
    public void onExpansionToggled(boolean expanded)
    {
        super.onExpansionToggled(expanded);

        RotateAnimation rotateAnimation;
        if (expanded) { // rotate clockwise
            rotateAnimation = new RotateAnimation(ROTATED_POSITION,
                    INITIAL_POSITION,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        } else { // rotate counterclockwise
            rotateAnimation = new RotateAnimation(-1 * ROTATED_POSITION, INITIAL_POSITION,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        }

        rotateAnimation.setDuration(200);
        rotateAnimation.setFillAfter(true);
        mArrowExpandImageView.startAnimation(rotateAnimation);
    }
}
