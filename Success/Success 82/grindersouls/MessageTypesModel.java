package com.deffe.macros.grindersouls;

import com.google.firebase.Timestamp;

public class MessageTypesModel
{
    public static final int MESSAGE_TYPE_TEXT_PLAIN = 0;
    public static final int MESSAGE_TYPE_TEXT_LINK = 1;
    public static final int MESSAGE_TYPE_DOCUMENT = 2;
    public static final int MESSAGE_TYPE_IMAGE = 3;
    public static final int MESSAGE_TYPE_GIF = 4;
    public static final int MESSAGE_TYPE_AUDIO = 5;
    public static final int MESSAGE_TYPE_VIDEO = 6;
    public static final int MESSAGE_TYPE_DAY_DATE = 7;

    private String message;
    private int type;
    private String time;
    private boolean seen;
    private String from;
    private String ref;
    private String size;
    private String key;
    private String duration;
    private String exe;
    private String file_name;
    private Timestamp date;
    private String video_thumbnail;
    private String today_date;

    public MessageTypesModel() {
    }

    public MessageTypesModel(String message, int type, String time, boolean seen, String from, String ref, String size, String key, String duration, String exe, String file_name, Timestamp date, String video_thumbnail, String today_date) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.seen = seen;
        this.from = from;
        this.ref = ref;
        this.size = size;
        this.key = key;
        this.duration = duration;
        this.exe = exe;
        this.file_name = file_name;
        this.date = date;
        this.video_thumbnail = video_thumbnail;
        this.today_date = today_date;
    }

    public String getVideo_thumbnail() {
        return video_thumbnail;
    }

    public void setVideo_thumbnail(String video_thumbnail) {
        this.video_thumbnail = video_thumbnail;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getToday_date() {
        return today_date;
    }

    public void setToday_date(String today_date) {
        this.today_date = today_date;
    }

    public boolean isSeen()
    {
        return seen;
    }

    public void setSeen(boolean seen)
    {
        this.seen = seen;
    }

    public String getExe() {
        return exe;
    }

    public void setExe(String exe) {
        this.exe = exe;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRef()
    {
        return ref;
    }

    public void setRef(String ref)
    {
        this.ref = ref;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

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

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

}
