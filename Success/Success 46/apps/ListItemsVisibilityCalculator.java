package com.deffe.macros.apps;


public interface ListItemsVisibilityCalculator {
    void onScrollStateIdle(ItemsPositionGetter itemsPositionGetter, int firstVisiblePosition, int lastVisiblePosition);
    void onScroll(ItemsPositionGetter itemsPositionGetter, int firstVisibleItem, int visibleItemCount, int scrollState);
}
