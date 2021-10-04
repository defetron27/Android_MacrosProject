package com.deffe.macros.grindersouls.Models;

import java.util.Date;

public class TrendPostsModel
{
    private String url;
    private String user_key;
    private String post_key;
    private Date time;
    private String desc;
    private int stars;

    public TrendPostsModel() {
    }

    public TrendPostsModel(String url, String user_key, String post_key, Date time, String desc, int stars) {
        this.url = url;
        this.user_key = user_key;
        this.post_key = post_key;
        this.time = time;
        this.desc = desc;
        this.stars = stars;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(String user_key) {
        this.user_key = user_key;
    }

    public String getPost_key() {
        return post_key;
    }

    public void setPost_key(String post_key) {
        this.post_key = post_key;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
