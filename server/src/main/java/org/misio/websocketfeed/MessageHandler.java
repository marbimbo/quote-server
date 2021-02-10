package org.misio.websocketfeed;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface MessageHandler {
    void handleMessage(String message) throws JsonProcessingException;
}
