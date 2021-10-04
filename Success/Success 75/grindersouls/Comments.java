package com.deffe.macros.grindersouls;

public class Comments
{
    private String userComment;

    private String comment;
    private String commented_key;
    private String commented_user_key;
    private String commented_time;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommented_key() {
        return commented_key;
    }

    public void setCommented_key(String commented_key) {
        this.commented_key = commented_key;
    }

    public String getCommented_user_key() {
        return commented_user_key;
    }

    public void setCommented_user_key(String commented_user_key) {
        this.commented_user_key = commented_user_key;
    }

    public String getCommented_time() {
        return commented_time;
    }

    public void setCommented_time(String commented_time) {
        this.commented_time = commented_time;
    }

    public Comments(String comment, String commented_key, String commented_user_key, String commented_time)
    {
        this.comment = comment;
        this.commented_key = commented_key;
        this.commented_user_key = commented_user_key;
        this.commented_time = commented_time;
    }

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
