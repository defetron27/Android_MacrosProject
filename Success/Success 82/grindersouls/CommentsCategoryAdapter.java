package com.deffe.macros.grindersouls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class CommentsCategoryAdapter extends ExpandableRecyclerAdapter<CommentsCategoryViewHolder, CommentsViewHolder>
{
    private LayoutInflater mInflator;

    CommentsCategoryAdapter(Context context, ArrayList<? extends ParentArrayListItem> parentItemList)
    {
        super(parentItemList);
        mInflator = LayoutInflater.from(context);
    }

    @Override
    public CommentsCategoryViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup)
    {
        View movieCategoryView = mInflator.inflate(R.layout.comments_main_layout_items, parentViewGroup, false);
        return new CommentsCategoryViewHolder(movieCategoryView);
    }

    @Override
    public CommentsViewHolder onCreateChildViewHolder(ViewGroup childViewGroup)
    {
        View moviesView = mInflator.inflate(R.layout.comment_layout_items, childViewGroup, false);
        return new CommentsViewHolder(moviesView);
    }

    @Override
    public void onBindParentViewHolder(CommentsCategoryViewHolder commentsCategoryViewHolder, int position, ParentArrayListItem parentListItem)
    {
        CommentsCategory commentsCategory = (CommentsCategory) parentListItem;
        commentsCategoryViewHolder.bind(commentsCategory);
    }

    @Override
    public void onBindChildViewHolder(CommentsViewHolder commentsViewHolder, int position, Object childListItem)
    {
        Comments comments = (Comments) childListItem;
        commentsViewHolder.bind(comments);
    }
}
