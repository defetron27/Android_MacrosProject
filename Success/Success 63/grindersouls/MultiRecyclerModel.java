package com.deffe.macros.grindersouls;

public class MultiRecyclerModel
{
    private int type;
    private String post;
    private long uploaded_time;
    private String storage_ref;
    private String post_key;
    private String size;

    public String getSize()
    {
        return size;
    }

    public void setSize(String size)
    {
        this.size = size;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public long getUploaded_time() {
        return uploaded_time;
    }

    public void setUploaded_time(long uploaded_time) {
        this.uploaded_time = uploaded_time;
    }

    public String getStorage_ref() {
        return storage_ref;
    }

    public void setStorage_ref(String storage_ref) {
        this.storage_ref = storage_ref;
    }

    public String getPost_key() {
        return post_key;
    }

    public void setPost_key(String post_key) {
        this.post_key = post_key;
    }

    public MultiRecyclerModel()
    {
    }

    public MultiRecyclerModel(int type, String post, long uploaded_time, String storage_ref, String post_key) {
        this.type = type;
        this.post = post;
        this.uploaded_time = uploaded_time;
        this.storage_ref = storage_ref;
        this.post_key = post_key;
    }
}
