package com.deffe.macros.grindersouls;

import java.util.ArrayList;

public class CommentsCategory implements ParentArrayListItem
{
    private ArrayList<Comments> comments;
    private String mComment;
    private String mCommentSize;

    CommentsCategory(ArrayList<Comments> comments, String mComment, String mCommentSize)
    {
        this.comments = comments;
        this.mComment = mComment;
        this.mCommentSize = mCommentSize;
    }

    public String getmCommentSize() {
        return mCommentSize;
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
