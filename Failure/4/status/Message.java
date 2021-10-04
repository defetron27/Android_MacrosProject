package com.deffe.macros.status;


public interface Message
{
    void runMessage();
    void polledFromQueue();
    void messageFinished();
}
