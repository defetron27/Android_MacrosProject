package com.deffe.macros.status;

import android.view.View;

public class CurrentItemMetaData implements MetaData {

    private final int positionOfCurrentItem;
    private final View currentItemView;

    CurrentItemMetaData(int positionOfCurrentItem, View currentItemView) {
        this.positionOfCurrentItem = positionOfCurrentItem;
        this.currentItemView = currentItemView;
    }

    @Override
    public String toString() {
        return "CurrentItemMetaData{" +
                "positionOfCurrentItem=" + positionOfCurrentItem +
                ", currentItemView=" + currentItemView +
                '}';
    }
}
