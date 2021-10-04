package com.deffe.macros.grindersouls.Models;

public class UserGroupsItemsModel
{
    private String key;
    private String type;

    public UserGroupsItemsModel()
    {
    }

    public UserGroupsItemsModel(String key, String type) {
        this.key = key;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
