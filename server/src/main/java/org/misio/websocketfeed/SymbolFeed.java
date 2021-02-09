package org.misio.websocketfeed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import org.misio.websocketfeed.handler.LiveOrderBookHandler;
import org.misio.websocketfeed.message.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.net.URI;
import java.time.Instant;

@ClientEndpoint
public class SymbolFeed {
    private static final Logger log = LoggerFactory.getLogger(SymbolFeed.class);
    private Session userSession = null;
    private WebsocketFeed.MessageHandler messageHandler;
    private String websocketUrl;
    private String productId;

    public void setWebsocketUrl(String websocketUrl) {
        this.websocketUrl = websocketUrl;
    }

    public void init() {
        log.info("Subscribing to websocket");
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(websocketUrl));
        } catch (Exception e) {
            log.error("Could not connect to remote server: " + e.getMessage() + ", " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session userSession) {
        log.info("opening websocket");
        this.userSession = userSession;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        log.info("closing websocket: {}", reason);
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
        log.error("WebsocketFeed error!!!");
        t.printStackTrace();
    }

    private void setMessageHandler(WebsocketFeed.MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    private void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

    public void subscribe(String[] productIds, LiveOrderBookHandler liveOrderBook) {
        log.info("Subscribing to {}", (Object[]) productIds);
        Subscribe msg = new Subscribe(productIds);
        String jsonSubscribeMessage = signObject(msg);

        setMessageHandler(liveOrderBook::handleMessages);

        sendMessage(jsonSubscribeMessage);

        log.info("Initialising order book for {} complete", productIds);
    }

    private String signObject(Subscribe jsonObj) {
        Gson gson = new Gson();

        String timestamp = Instant.now().getEpochSecond() + "";
        jsonObj.setTimestamp(timestamp);

        return gson.toJson(jsonObj);
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
