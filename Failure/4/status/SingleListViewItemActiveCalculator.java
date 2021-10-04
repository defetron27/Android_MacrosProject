package com.deffe.macros.status;

import android.view.View;

import java.util.List;


public abstract class SingleListViewItemActiveCalculator extends BaseItemsVisibilityCalculator
{

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = SingleListViewItemActiveCalculator.class.getSimpleName();


    private final Callback<ListItem> mCallback;
    private final List<? extends ListItem> mListItems;

    private ScrollDirectionDetector.ScrollDirection mScrollDirection = ScrollDirectionDetector.ScrollDirection.UP;

    private final ListItemData mCurrentItem = new ListItemData();

    SingleListViewItemActiveCalculator(Callback<ListItem> callback, List<? extends ListItem> listItems) {
        mCallback = callback;
        mListItems = listItems;
    }


    public interface Callback<T extends ListItem>
    {
        void activateNewCurrentItem(T newListItem, View currentView, int position);
    }

    @Override
    protected void onStateTouchScroll(ItemsPositionGetter itemsPositionGetter) {
        if(SHOW_LOGS) Logger.v(TAG, ">> onStateTouchScroll, mScrollDirection " + mScrollDirection);

        ListItemData listItemData = mCurrentItem;
        if(SHOW_LOGS) Logger.v(TAG, "onStateTouchScroll, listItemData " + listItemData);

        if(SHOW_LOGS) Logger.v(TAG, "<< onStateTouchScroll, mScrollDirection " + mScrollDirection);
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
