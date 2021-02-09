package org.misio.websocketfeed.handler;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface LiveOrderBookHandler {
    void handleMessages(String message) throws JsonProcessingException;
}
