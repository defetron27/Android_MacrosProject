package com.deffe.macros.grindersouls;

import java.util.ArrayList;

public class CommentsCategory implements ParentArrayListItem
{
    private ArrayList<Comments> comments;
    private String mComment;

    CommentsCategory(ArrayList<Comments> comments, String mComment)
    {
        this.comments = comments;
        this.mComment = mComment;
    }

    public String getComment() {
        return mComment;
    }

    @Override
    public ArrayList<?> getChildItemList()
    {
        return comments;
    }

    @Override
    public boolean isInitiallyExpanded()
    {
        return false;
    }
}
