package com.deffe.macros.status;


public interface ListItemsVisibilityCalculator {
    void onScroll(ItemsPositionGetter itemsPositionGetter, int firstVisibleItem, int visibleItemCount, int scrollState);
}
