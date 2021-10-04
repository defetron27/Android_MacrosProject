package com.deffe.macros.grindersouls;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Comments
{
    private String comment;
    private String comment_key;
    private long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Comments(String comment, String comment_key, long time) {
        this.comment = comment;
        this.comment_key = comment_key;
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment_key() {
        return comment_key;
    }

    public void setComment_key(String comment_key) {
        this.comment_key = comment_key;
    }

    public Comments() {

    }
}
