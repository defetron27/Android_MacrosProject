package com.deffe.macros.grindersouls;

import java.util.ArrayList;

public class ExpandableRecyclerAdapterHelper {

    public static ArrayList<Object> generateParentChildItemList(ArrayList<? extends ParentArrayListItem> parentItemList) {
        ArrayList<Object> parentWrapperList = new ArrayList<>();
        ParentArrayListItem parentListItem;
        ParentWrapper parentWrapper;

        int parentListItemCount = parentItemList.size();
        for (int i = 0; i < parentListItemCount; i++) {
            parentListItem = parentItemList.get(i);
            parentWrapper = new ParentWrapper(parentListItem);
            parentWrapperList.add(parentWrapper);

            if (parentWrapper.isInitiallyExpanded()) {
                parentWrapper.setExpanded(true);

                int childListItemCount = parentWrapper.getChildItemList().size();
                for (int j = 0; j < childListItemCount; j++) {
                    parentWrapperList.add(parentWrapper.getChildItemList().get(j));
                }
            }
        }

        return parentWrapperList;
    }
}
