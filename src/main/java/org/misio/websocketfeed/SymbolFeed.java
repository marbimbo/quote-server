package org.misio.websocketfeed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.misio.websocketfeed.handler.LiveOrderBookHandler;
import org.misio.websocketfeed.message.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

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

//    Signature signature;
    private WebsocketFeed.MessageHandler messageHandler;
    private int counter = 0;
    private String websocketUrl;
    private Set<String> subscribedChannels = new HashSet<>();;
    private Boolean isEnabled;
    private String key;
    private String passphrase;
    private String productId;

    @Value("${websocket.baseUrl}")
    public void setWebsocketUrl(String websocketUrl) {
        this.websocketUrl = websocketUrl;
    }


    public void setSubscribedChannels(Set<String> subscribedChannels) {
        this.subscribedChannels = subscribedChannels;
    }

    @Value("${websocket.enabled}")
    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    @Value("${gdax.key}")
    public void setKey(String key) {
        this.key = key;
    }

    @Value("${gdax.passphrase}")
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

//    @Autowired
//    public WebsocketFeed(@Value("${websocket.baseUrl}") String websocketUrl,
//                         @Value("${websocket.enabled}") Boolean isEnabled,
//                         @Value("${gdax.key}") String key,
//                         @Value("${gdax.passphrase}") String passphrase/*,
//                         Signature signature*/) {
//        this.key = key;
//        this.passphrase = passphrase;
////        this.signature = signature;
//        this.websocketUrl = websocketUrl;
//        this.isEnabled = isEnabled;
//        this.subscribedChannels = new HashSet<>();
//        init();
//    }

    public void init() {
        log.info("Subscribing to websocket");
        if (isEnabled) {
            try {
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                container.connectToServer(this, new URI(websocketUrl));
            } catch (Exception e) {
                log.error("Could not connect to remote server: " + e.getMessage() + ", " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        log.info("opening websocket");
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason      the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        log.info("closing websocket: {}", reason);
        this.userSession = null;
    }

    /**
     * Callback hook for events. This method will be invoked when a client sends a message.
     */
//    @OnMessage
//    public void onMessage(String message) {
//        if (this.messageHandler != null) {
//            this.messageHandler.handleMessage(message);
//        }
//    }

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
        if (isEnabled) {
            this.userSession.getAsyncRemote().sendText(message);
        }
    }

    public void subscribe(String[] productIds, LiveOrderBookHandler liveOrderBook) {
        log.info("Subscribing to {}", (Object[]) productIds);
        setSubscribedChannels(productIds);
        Subscribe msg = new Subscribe(productIds);
        String jsonSubscribeMessage = signObject(msg);

        long startTime = System.currentTimeMillis();

        log.info("channels: " + String.valueOf(subscribedChannels));

//        setMessageHandler(json -> {
//            SwingWorker<Void, OrderBookMessage> worker = new SwingWorker<Void, OrderBookMessage>() {
//
//                @Override
//                public Void doInBackground() {
//                    try (ZContext context = new ZContext()) {
//                        ++counter;
//
//                        long currentTime = System.currentTimeMillis();
////                    log.info(json);
////                    log.info("messages: {}", counter);
//                        log.info("messages: {}", counter * 1000 / (currentTime - startTime));
////                    System.out.println("messages: " + counter * 1000 / (currentTime - startTime));
//                        OrderBookMessage message = getObject(json, new TypeReference<OrderBookMessage>() {
//                        });
////                    log.info("Message Recieved: {}", message.getSequence());
//                        publish(message);
//                        return null;
//                    }
//                }
//
//                @Override
//                protected void process(List<OrderBookMessage> chunks) {
//                    if (chunks != null && chunks.size() > 0) {
//                        for (OrderBookMessage message : chunks) {
//                            liveOrderBook.handleMessages(message);
//                        }
//                    }
//                }
//            };
//            worker.execute();
//        });

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

    public String signObject(Subscribe jsonObj) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonObj);

        String timestamp = Instant.now().getEpochSecond() + "";
        jsonObj.setTimestamp(timestamp);
//        jsonObj.setSignature(signature.generate("", "GET", jsonString, timestamp));
        jsonObj.setPassphrase(passphrase);
        jsonObj.setKey(key);

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

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }

    /**
     * OrderBookMessage handler. Functional Interface.
     *
     * @author Jiji_Sasidharan
     */
    public interface MessageHandler {
        void handleMessage(String message);
    }
}
