package com.deffe.macros.grindersouls;


import android.support.v7.widget.RecyclerView;
import android.view.View;

public class TrendPostParentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{

    private ParentListItemExpandCollapseListener mParentListItemExpandCollapseListener;
    private boolean mExpanded;


    public interface ParentListItemExpandCollapseListener
    {
        void onParentListItemExpanded(int position);

        void onParentListItemCollapsed(int position);
    }

    TrendPostParentViewHolder(View itemView)
    {
        super(itemView);
        mExpanded = false;
    }

    public void setMainItemClickToExpand()
    {
        itemView.setOnClickListener(this);
    }

    public boolean isExpanded()
    {
        return mExpanded;
    }

    public void setExpanded(boolean expanded)
    {
        mExpanded = expanded;
    }

    void onExpansionToggled(boolean expanded)
    {

    }

    public ParentListItemExpandCollapseListener getParentListItemExpandCollapseListener()
    {
        return mParentListItemExpandCollapseListener;
    }

    public void setParentListItemExpandCollapseListener(ParentListItemExpandCollapseListener parentListItemExpandCollapseListener)
    {
        mParentListItemExpandCollapseListener = parentListItemExpandCollapseListener;
    }

    @Override
    public void onClick(View v)
    {
        if (mExpanded)
        {
            collapseView();
        }
        else
        {
            expandView();
        }
    }

    public boolean shouldItemViewClickToggleExpansion()
    {
        return true;
    }

    private void expandView()
    {
        setExpanded(true);
        onExpansionToggled(false);

        if (mParentListItemExpandCollapseListener != null)
        {
            mParentListItemExpandCollapseListener.onParentListItemExpanded(getAdapterPosition());
        }
    }

    private void collapseView() {
        setExpanded(false);
        onExpansionToggled(true);

        if (mParentListItemExpandCollapseListener != null) {
            mParentListItemExpandCollapseListener.onParentListItemCollapsed(getAdapterPosition());
        }
    }
}
