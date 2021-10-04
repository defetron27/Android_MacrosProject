package com.deffe.macros.status;

import android.view.View;

public interface ListItem
{
    void setActive(View newActiveView, int newActiveViewPosition);

    void deactivate(View currentView, int position);
}
