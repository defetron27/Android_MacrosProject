package com.deffe.macros.status;

import android.view.View;

import java.util.List;


public class SingleListViewItemActiveCalculator extends BaseItemsVisibilityCalculator {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = SingleListViewItemActiveCalculator.class.getSimpleName();

    private static final int INACTIVE_LIST_ITEM_VISIBILITY_PERCENTS = 70;

    private final Callback<ListItem> mCallback;
    private final List<? extends ListItem> mListItems;

    private ScrollDirectionDetector.ScrollDirection mScrollDirection = ScrollDirectionDetector.ScrollDirection.UP;


    private final ListItemData mCurrentItem = new ListItemData();

    SingleListViewItemActiveCalculator(Callback<ListItem> callback, List<? extends ListItem> listItems) {
        mCallback = callback;
        mListItems = listItems;
    }

    public interface Callback<T extends ListItem>{
        void activateNewCurrentItem(T newListItem, View currentView, int position);
        void deactivateCurrentItem(T listItemToDeactivate, View view, int position);
    }

    @Override
    protected void onStateTouchScroll(ItemsPositionGetter itemsPositionGetter) {
        if(SHOW_LOGS) Logger.v(TAG, ">> onStateTouchScroll, mScrollDirection " + mScrollDirection);

        ListItemData listItemData = mCurrentItem;
        if(SHOW_LOGS) Logger.v(TAG, "onStateTouchScroll, listItemData " + listItemData);

        if(SHOW_LOGS) Logger.v(TAG, "<< onStateTouchScroll, mScrollDirection " + mScrollDirection);
    }

    @Override
    public void onScrollDirectionChanged(ScrollDirectionDetector.ScrollDirection scrollDirection) {
        if(SHOW_LOGS) Logger.v(TAG, "onScrollDirectionChanged, scrollDirection " + scrollDirection);
        mScrollDirection = scrollDirection;
    }

    private void setCurrentItem(ListItemData newCurrentItem) {
        if(SHOW_LOGS) Logger.v(TAG, "setCurrentItem, newCurrentItem " + newCurrentItem);

        int itemPosition = newCurrentItem.getIndex();
        View view = newCurrentItem.getView();

        mCurrentItem.fillWithData(itemPosition, view);

        mCallback.activateNewCurrentItem(
                mListItems.get(itemPosition)
                , view
                , itemPosition);
    }
}
