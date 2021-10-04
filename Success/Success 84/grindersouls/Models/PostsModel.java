package com.deffe.macros.grindersouls.Models;

import com.google.firebase.Timestamp;

import java.util.Date;

public class PostsModel
{
    private String post;
    private String post_key;
    private String post_user_key;
    private String ref;
    private String size;
    private String type;
    private String description;
    private long uploaded_time;
    private String video_thumbnail;
    private Timestamp date;
    private String duration;

    public PostsModel() {
    }

    public PostsModel(String post, String post_key, String post_user_key, String ref, String size, String type, String description, long uploaded_time, String video_thumbnail, Timestamp date,String duration) {
        this.post = post;
        this.post_key = post_key;
        this.post_user_key = post_user_key;
        this.ref = ref;
        this.size = size;
        this.type = type;
        this.description = description;
        this.uploaded_time = uploaded_time;
        this.video_thumbnail = video_thumbnail;
        this.date = date;
        this.duration = duration;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public long getUploaded_time() {
        return uploaded_time;
    }

    public void setUploaded_time(long uploaded_time) {
        this.uploaded_time = uploaded_time;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getVideo_thumbnail() {
        return video_thumbnail;
    }

    public void setVideo_thumbnail(String video_thumbnail) {
        this.video_thumbnail = video_thumbnail;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getPost_key() {
        return post_key;
    }

    public void setPost_key(String post_key) {
        this.post_key = post_key;
    }

    public String getPost_user_key() {
        return post_user_key;
    }

    public void setPost_user_key(String post_user_key) {
        this.post_user_key = post_user_key;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
