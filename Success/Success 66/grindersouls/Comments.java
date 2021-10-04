package com.deffe.macros.grindersouls;

public class Comments
{
    private String userComment;

    Comments(String userComment)
    {
        this.userComment = userComment;
    }

    public String getUserComment()
    {
        return userComment;
    }

    public void setUserComment(String userComment)
    {
        this.userComment = userComment;
    }
}
