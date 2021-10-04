package com.deffe.macros.grindersouls;

import java.util.ArrayList;

public class TrendPostOpinionCategory implements ParentArrayListItem
{
    private ArrayList<TrendPostOpinions> opinions;
    private String mOpinion;
    private String mOpinionSize;

    TrendPostOpinionCategory(ArrayList<TrendPostOpinions> comments, String mComment, String mCommentSize)
    {
        this.opinions = comments;
        this.mOpinion = mComment;
        this.mOpinionSize = mCommentSize;
    }

    public String getmOpinion() {

        return mOpinion;
    }

    public String getmOpinionSize() {
        return mOpinionSize;
    }

    @Override
    public ArrayList<?> getChildItemList()
    {
        return opinions;
    }

    @Override
    public boolean isInitiallyExpanded()
    {
        return false;
    }
}
