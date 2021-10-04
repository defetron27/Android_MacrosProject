package com.deffe.macros.grindersouls.Models;

public class UserGroupsItemsModel
{
    private String key;
    private String time;

    public UserGroupsItemsModel(String key, String time)
    {
        this.key = key;
        this.time = time;
    }

    public UserGroupsItemsModel()
    {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
