package org.misio.websocketfeed;

import org.misio.websocketfeed.message.OrderMessage;

public interface MessageHandler {
    void handleMessage(OrderMessage message);
}
