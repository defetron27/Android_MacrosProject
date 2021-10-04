package com.deffe.macros.apps;

/**
 * This generic interface for messages
 */
public interface Message {
    void runMessage();
    void polledFromQueue();
    void messageFinished();
}
