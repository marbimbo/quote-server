package org.misio.websocketfeed.handler;

public interface LiveOrderBookHandler {
    void handleMessages(byte[] message);
}
