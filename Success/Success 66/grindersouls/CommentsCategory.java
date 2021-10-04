package com.deffe.macros.grindersouls;

import java.util.ArrayList;

public class CommentsCategory implements ParentArrayListItem
{
    private ArrayList<Comments> comments;
    private String mComment,CommentsCount;

    CommentsCategory(ArrayList<Comments> comments, String mComment, String commentsCount)
    {
        this.comments = comments;
        this.mComment = mComment;
        CommentsCount = commentsCount;
    }

    public String getComment() {
        return mComment;
    }

    public String getCommentsCount() {
        return CommentsCount;
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
