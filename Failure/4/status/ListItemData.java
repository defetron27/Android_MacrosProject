package com.deffe.macros.status;

import android.app.LauncherActivity;
import android.view.View;

import java.util.List;


public class ListItemData
{
    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;
    private static final String TAG = LauncherActivity.ListItem.class.getSimpleName();

    private Integer mIndexInAdapter;
    private View mView;

    private boolean mIsMostVisibleItemChanged;

    public int getIndex() {
        return mIndexInAdapter;
    }

    public View getView() {
        return mView;
    }

    public ListItemData fillWithData(int indexInAdapter, View view)
    {
        mIndexInAdapter = indexInAdapter;
        mView = view;
        return this;
    }

    public boolean isAvailable()
    {
        boolean isAvailable = mIndexInAdapter != null && mView != null;
        if(SHOW_LOGS) Logger.v(TAG, "isAvailable " + isAvailable);
        return isAvailable;
    }

    @Override
    public String toString() {
        return "ListItemData{" +
                "mIndexInAdapter=" + mIndexInAdapter +
                ", mView=" + mView +
                ", mIsMostVisibleItemChanged=" + mIsMostVisibleItemChanged +
                '}';
    }
}
