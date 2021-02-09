package org.misio.websocketfeed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.misio.websocketfeed.handler.LiveOrderBookHandler;
import org.misio.websocketfeed.message.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ClientEndpoint
public class SymbolFeed {
    private static final Logger log = LoggerFactory.getLogger(SymbolFeed.class);
    private Session userSession = null;
    private WebsocketFeed.MessageHandler messageHandler;
    private int counter = 0;
    private String websocketUrl;
    private Set<String> subscribedChannels = new HashSet<>();
    private String productId;

    public void setWebsocketUrl(String websocketUrl) {
        this.websocketUrl = websocketUrl;
    }

    public void setSubscribedChannels(Set<String> subscribedChannels) {
        this.subscribedChannels = subscribedChannels;
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
        setSubscribedChannels(productIds);
        Subscribe msg = new Subscribe(productIds);
        String jsonSubscribeMessage = signObject(msg);

        long startTime = System.currentTimeMillis();

        log.info("channels: " + String.valueOf(subscribedChannels));

        setMessageHandler(new WebsocketFeed.MessageHandler() {

            long start = System.currentTimeMillis();

            @Override
            public void handleMessage(String message) throws JsonProcessingException {
//                ++counter;
//                System.out.println(counter + " : " + System.currentTimeMillis());
                liveOrderBook.handleMessages(message);
//                System.out.println(1000 * counter / (System.currentTimeMillis() - start));
//                liveOrderBook.handleMessages("" + counter);
            }
        });

        sendMessage(jsonSubscribeMessage);

        log.info("Initialising order book for {} complete", productIds);
    }

    public void subscribe(String[] productIds) {
        log.info("WebSocketFeed subscribing to {}", productIds);
        setSubscribedChannels(productIds);
        Subscribe msg = new Subscribe((String[]) Arrays.asList(productIds).toArray());
        String jsonSubscribeMessage = signObject(msg);
        sendMessage(jsonSubscribeMessage);
        log.info("WebSocketFeed subscribtion message sent");
    }

    private String signObject(Subscribe jsonObj) {
        Gson gson = new Gson();

        String timestamp = Instant.now().getEpochSecond() + "";
        jsonObj.setTimestamp(timestamp);

        return gson.toJson(jsonObj);
    }

    private <T> T getObject(String json, TypeReference<T> type) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setSubscribedChannels(String[] subscribedProducts) {
        subscribedChannels.clear();
        subscribedChannels.addAll(Arrays.asList(subscribedProducts));
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public interface MessageHandler {
        void handleMessage(String message);
    }
}
