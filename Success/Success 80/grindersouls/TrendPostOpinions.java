package com.deffe.macros.grindersouls;

import com.google.firebase.Timestamp;

public class TrendPostOpinions
{
    private String opinion;
    private String opinion_key;
    private Timestamp time;

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public String getOpinion_key() {
        return opinion_key;
    }

    public void setOpinion_key(String opinion_key) {
        this.opinion_key = opinion_key;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
    this.time = time;
}

    public TrendPostOpinions(String opinion, String opinion_key, Timestamp time) {
        this.opinion = opinion;
        this.opinion_key = opinion_key;
        this.time = time;
    }

    public TrendPostOpinions() {

    }
}
