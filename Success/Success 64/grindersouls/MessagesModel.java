package com.deffe.macros.grindersouls;

import android.net.Uri;

public class MessagesModel
{
    public static final int MESSAGE_TYPE_TEXT = 0;
    public static final int MESSAGE_TYPE_IMAGE = 1;
    public static final int MESSAGE_TYPE_VIDEO = 2;
    public static final int MESSAGE_TYPE_DAY_DATE = 3;

    private String message;
    private int type;
    private String time;
    private boolean seen;
    private String from;

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public boolean isSeen()
    {
        return seen;
    }

    public void setSeen(boolean seen)
    {
        this.seen = seen;
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

}
