package com.deffe.macros.status;

import android.view.View;

import java.util.List;


public abstract class SingleListViewItemActiveCalculator extends BaseItemsVisibilityCalculator {

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = SingleListViewItemActiveCalculator.class.getSimpleName();

    private static final int INACTIVE_LIST_ITEM_VISIBILITY_PERCENTS = 70;

    private final Callback<ListItem> mCallback;
    private final List<? extends ListItem> mListItems;

    /** Initial scroll direction should be UP in order to set as active most top item if no active item yet*/
    private ScrollDirectionDetector.ScrollDirection mScrollDirection = ScrollDirectionDetector.ScrollDirection.UP;

    /**
     * The data of this member will be changing all the time
     */
    private final ListItemData mCurrentItem = new ListItemData();

    SingleListViewItemActiveCalculator(Callback<ListItem> callback, List<? extends ListItem> listItems) {
        mCallback = callback;
        mListItems = listItems;
    }

    /**
     * Methods of this callback will be called when new active item is found {@link Callback#activateNewCurrentItem(ListItem, View, int)}
     * or when there is no active item {@link Callback#deactivateCurrentItem(ListItem, View, int)} - this might happen when user scrolls really fast
     */
    public interface Callback<T extends ListItem>{
        void activateNewCurrentItem(T newListItem, View currentView, int position);
        void deactivateCurrentItem(T listItemToDeactivate, View view, int position);
    }

    /**
     * When Scrolling list is in this state we start calculating Active Item.
     * Here we assume that scroll state was idle previously and {@link #mCurrentItem} already contains some data
     *
     * @param itemsPositionGetter
     */
    @Override
    protected void onStateTouchScroll(ItemsPositionGetter itemsPositionGetter) {
        if(SHOW_LOGS) Logger.v(TAG, ">> onStateTouchScroll, mScrollDirection " + mScrollDirection);

        ListItemData listItemData = mCurrentItem;
        if(SHOW_LOGS) Logger.v(TAG, "onStateTouchScroll, listItemData " + listItemData);

        if(SHOW_LOGS) Logger.v(TAG, "<< onStateTouchScroll, mScrollDirection " + mScrollDirection);
    }

    /**
     * This method calculates visibility of next item.
     * There are some cases when next item cannot be filled with data:
     *
     * 1. When current data is last item in the list. In this case there is no next data
     * 2. Index of current view cannot be calculated because view was already recycled
     *
     * @param itemsPositionGetter
     * @param currentIem - the item that is active right now
     * @param outNextItemData - out parameter. It will be filled with next item data if the one exists
     */
    private void findNextItem(ItemsPositionGetter itemsPositionGetter, ListItemData currentIem, ListItemData outNextItemData) {
        int nextItemVisibilityPercents = 0;
        int nextItemIndex = currentIem.getIndex() + 1;
        if(SHOW_LOGS) Logger.v(TAG, "findNextItem, nextItemIndex " + nextItemIndex);

        if(nextItemIndex < mListItems.size()){
            int indexOfCurrentView = itemsPositionGetter.indexOfChild(currentIem.getView());
            if(SHOW_LOGS) Logger.v(TAG, "findNextItem, indexOfCurrentView " + indexOfCurrentView);

            if(indexOfCurrentView >= 0){
                View nextView = itemsPositionGetter.getChildAt(indexOfCurrentView + 1);
                if(nextView != null){
                    ListItem next = mListItems.get(nextItemIndex);
                    if(SHOW_LOGS) Logger.v(TAG, "findNextItem, next " + next + ", nextView " + nextView);

                    nextItemVisibilityPercents = next.getVisibilityPercents(nextView);
                    outNextItemData.fillWithData(nextItemIndex, nextView);

                } else {
                    if(SHOW_LOGS) Logger.v(TAG, "findNextItem, nextView null. There is no view next to current");
                }

            } else {
                if(SHOW_LOGS) Logger.v(TAG, "findNextItem, current view is no longer attached to listView");
            }
        }
        if(SHOW_LOGS) Logger.v(TAG, "findNextItem, nextItemVisibilityPercents " + nextItemVisibilityPercents);
    }

    /**
     * This method calculates visibility of previous item.
     * There are some cases when previous item cannot be filled with data:
     *
     * 1. When current data is first item in the list. in this case there is no previous data
     * 2. Index of current view cannot be calculated because view was already recycled
     *
     * @param itemsPositionGetter
     * @param currentIem - the item that is active right now
     * @param outPreviousItemData - out parameter. It will be filled with previous item data if the one exists
     */
    private void findPreviousItem(ItemsPositionGetter itemsPositionGetter, ListItemData currentIem, ListItemData outPreviousItemData) {
        int previousItemVisibilityPercents = 0;
        int previousItemIndex = currentIem.getIndex() -1;
        if(SHOW_LOGS) Logger.v(TAG, "findPreviousItem, previousItemIndex " + previousItemIndex);

        if(previousItemIndex >= 0){
            int indexOfCurrentView = itemsPositionGetter.indexOfChild(currentIem.getView());
            if(SHOW_LOGS) Logger.v(TAG, "findPreviousItem, indexOfCurrentView " + indexOfCurrentView);

            if(indexOfCurrentView > 0){
                View previousView = itemsPositionGetter.getChildAt(indexOfCurrentView - 1);
                ListItem previous = mListItems.get(previousItemIndex);
                if(SHOW_LOGS) Logger.v(TAG, "findPreviousItem, previous " + previous + ", previousView " + previousView);

                previousItemVisibilityPercents = previous.getVisibilityPercents(previousView);
                outPreviousItemData.fillWithData(previousItemIndex, previousView);

            } else {
                if(SHOW_LOGS) Logger.v(TAG, "findPreviousItem, current view is no longer attached to listView");
            }
        }
        if(SHOW_LOGS) Logger.v(TAG, "findPreviousItem, previousItemVisibilityPercents " + previousItemVisibilityPercents);
    }

    @Override
    public void onScrollStateIdle(ItemsPositionGetter itemsPositionGetter, int firstVisiblePosition, int lastVisiblePosition) {

        if(SHOW_LOGS) Logger.v(TAG, "onScrollStateIdle, firstVisiblePosition " + firstVisiblePosition + ", lastVisiblePosition " + lastVisiblePosition);
    }

    private ListItemData getMockCurrentItem(ItemsPositionGetter itemsPositionGetter, int firstVisiblePosition, int lastVisiblePosition) {
        if(SHOW_LOGS) Logger.v(TAG, "getMockCurrentItem, mScrollDirection " + mScrollDirection);
        if(SHOW_LOGS) Logger.v(TAG, "getMockCurrentItem, firstVisiblePosition " + firstVisiblePosition);
        if(SHOW_LOGS) Logger.v(TAG, "getMockCurrentItem, lastVisiblePosition " + lastVisiblePosition);

        ListItemData mockCurrentItemData;
        switch (mScrollDirection){
            case UP:
                int lastVisibleItemIndex;
                if(lastVisiblePosition < 0/*-1 may be returned from ListView*/){
                    lastVisibleItemIndex = firstVisiblePosition;
                } else {
                    lastVisibleItemIndex = lastVisiblePosition;
                }

                mockCurrentItemData = new ListItemData().fillWithData(lastVisibleItemIndex, itemsPositionGetter.getChildAt(itemsPositionGetter.getChildCount() - 1));
                break;
            case DOWN:
                mockCurrentItemData = new ListItemData().fillWithData(firstVisiblePosition, itemsPositionGetter.getChildAt(0/*first visible*/));
                break;
            default:
                throw new RuntimeException("not handled mScrollDirection " + mScrollDirection);
        }
        return mockCurrentItemData;
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
