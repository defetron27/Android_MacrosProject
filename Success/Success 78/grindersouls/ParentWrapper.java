package com.deffe.macros.grindersouls;

import java.util.ArrayList;

public class ParentWrapper {

    private boolean mExpanded;
    private ParentArrayListItem mParentListItem;

    ParentWrapper(ParentArrayListItem parentListItem) {
        mParentListItem = parentListItem;
        mExpanded = false;
    }

    public ParentArrayListItem getParentListItem() {
        return mParentListItem;
    }


    public void setParentListItem(ParentArrayListItem parentListItem) {
        mParentListItem = parentListItem;
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    public void setExpanded(boolean expanded) {
        mExpanded = expanded;
    }

    public boolean isInitiallyExpanded() {
        return mParentListItem.isInitiallyExpanded();
    }

    public ArrayList<?> getChildItemList() {
        return mParentListItem.getChildItemList();
    }
}
