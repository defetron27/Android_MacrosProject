package com.deffe.macros.grindersouls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class TrendPostOpinionCategoryAdapter extends ExpandableRecyclerAdapter<TrendPostOpinionCategoryViewHolder, TrendPostOpinionViewHolder>
{
    private LayoutInflater mInflator;

    TrendPostOpinionCategoryAdapter(Context context, ArrayList<? extends ParentArrayListItem> parentItemList)
    {
        super(parentItemList);
        mInflator = LayoutInflater.from(context);
    }

    @Override
    public TrendPostOpinionCategoryViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup)
    {
        View movieCategoryView = mInflator.inflate(R.layout.comments_main_layout_items, parentViewGroup, false);
        return new TrendPostOpinionCategoryViewHolder(movieCategoryView);
    }

    @Override
    public TrendPostOpinionViewHolder onCreateChildViewHolder(ViewGroup childViewGroup)
    {
        View moviesView = mInflator.inflate(R.layout.comment_layout_items, childViewGroup, false);
        return new TrendPostOpinionViewHolder(moviesView);
    }

    @Override
    public void onBindParentViewHolder(TrendPostOpinionCategoryViewHolder trendPostOpinionCategoryViewHolder, int position, ParentArrayListItem parentListItem)
    {
        TrendPostOpinionCategory commentsCategory = (TrendPostOpinionCategory) parentListItem;
        trendPostOpinionCategoryViewHolder.bind(commentsCategory);
    }

    @Override
    public void onBindChildViewHolder(TrendPostOpinionViewHolder trendPostOpinionViewHolder, int position, Object childListItem)
    {
        TrendPostOpinions comments = (TrendPostOpinions) childListItem;
        trendPostOpinionViewHolder.bind(comments);
    }
}
