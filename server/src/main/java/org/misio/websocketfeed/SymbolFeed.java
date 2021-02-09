package org.misio.websocketfeed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import org.misio.websocketfeed.handler.LiveOrderBookHandler;
import org.misio.websocketfeed.message.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;

@ClientEndpoint
public class SymbolFeed {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Session userSession = null;
    private WebsocketFeed.MessageHandler messageHandler;
    private String websocketUrl;
    private String[] productIds;

    public void setWebsocketUrl(String websocketUrl) {
        this.websocketUrl = websocketUrl;
    }

    public void init() {
        LOG.info("Subscribing to websocket");
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(websocketUrl));
        } catch (Exception e) {
            LOG.error("Could not connect to remote server: " + e.getMessage() + ", " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session userSession) {
        LOG.info("opening websocket {}", userSession.getId());
        this.userSession = userSession;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        LOG.info("closing websocket: {} {}", reason, userSession.getId());
        this.userSession = null;
    }

    @OnMessage
    public void onMessage(String message) throws JsonProcessingException {
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
        }
    }

    @OnError
    public void onError(Session s, Throwable t) {
        LOG.error("WebsocketFeed error!!!");
        t.printStackTrace();
    }

    private void setMessageHandler(WebsocketFeed.MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    private void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

    public void subscribe(String[] productIds, LiveOrderBookHandler liveOrderBook) {
        LOG.info("Subscribing to {}", Arrays.toString(productIds));
        Subscribe msg = new Subscribe(productIds);
        String jsonSubscribeMessage = signObject(msg);

        setMessageHandler(liveOrderBook::handleMessages);

        sendMessage(jsonSubscribeMessage);

        LOG.info("Initialising order book for {} complete", Arrays.toString(productIds));
    }

    private String signObject(Subscribe jsonObj) {
        Gson gson = new Gson();

        String timestamp = Instant.now().getEpochSecond() + "";
        jsonObj.setTimestamp(timestamp);

        return gson.toJson(jsonObj);
    }

    public String[] getProductIds() {
        return productIds;
    }

    public void setProductIds(String[] productIds) {
        this.productIds = productIds;
    }
}
