package com.deffe.macros.status;

import android.view.View;

public class DefaultSingleItemCalculatorCallback implements SingleListViewItemActiveCalculator.Callback<ListItem>{

    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = DefaultSingleItemCalculatorCallback.class.getSimpleName();

    @Override
    public void activateNewCurrentItem(ListItem newListItem, View newView, int newViewPosition) {
        if(SHOW_LOGS){
            Logger.v(TAG, "activateNewCurrentItem, newListItem " + newListItem);
            Logger.v(TAG, "activateNewCurrentItem, newViewPosition " + newViewPosition);
        }

        newListItem.setActive(newView, newViewPosition);
    }

}
