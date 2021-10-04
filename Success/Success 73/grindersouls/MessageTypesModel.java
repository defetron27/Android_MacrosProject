package com.deffe.macros.grindersouls;

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
    private String date;
    private String pdf_thumbnail;

    public String getPdf_thumbnail() {
        return pdf_thumbnail;
    }

    public void setPdf_thumbnail(String pdf_thumbnail) {
        this.pdf_thumbnail = pdf_thumbnail;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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
