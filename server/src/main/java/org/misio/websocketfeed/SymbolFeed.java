package org.misio.websocketfeed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import org.misio.websocketfeed.message.Channel;
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
    private Session userSession;
    private MessageHandler messageHandler;
    private ExceptionHandler exceptionHandler;
    private String websocketUrl;
    private String[] productIds;

    public void setWebsocketUrl(String websocketUrl) {
        this.websocketUrl = websocketUrl;
    }

    public void setMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public void init() {
        LOG.info("Subscribing to websocket");
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(websocketUrl));
        } catch (Exception e) {
            LOG.error("Could not connect to remote server: {} {}", e.getMessage(), e.getLocalizedMessage());
            e.printStackTrace();
            exceptionHandler.handleException(e.getMessage());
        }
    }

    @OnOpen
    public void onOpen(Session userSession) {
        LOG.info("opening websocket {}", userSession.getId());
        this.userSession = userSession;
        subscribe();
    }

    private void subscribe() {
        LOG.info("Subscribing to {}", Arrays.toString(productIds));
        Channel channel = new Channel();
        channel.setName("full");
        channel.setProduct_ids(productIds);
        Subscribe subscribe = new Subscribe();
        subscribe.setChannels(new Channel[]{channel});
        subscribe.setType("subscribe");

        String jsonSubscribeMessage = signObject(subscribe);
        sendMessage(jsonSubscribeMessage);

        LOG.info("Initialising order book for {} complete", Arrays.toString(productIds));
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        LOG.warn("closing websocket: {} {}", reason, userSession.getId());
        LOG.info("reconnecting");
        new Thread(this::init).start();
        exceptionHandler.handleException(reason.getReasonPhrase());
    }

    @OnMessage
    public void onMessage(String message) throws JsonProcessingException {
        LOG.debug("onMessage");
        messageHandler.handleMessage(message);
    }

    @OnError
    public void onError(Session s, Throwable t) {
        LOG.error("WebsocketFeed error!!!");
        t.printStackTrace();
        exceptionHandler.handleException(t.getLocalizedMessage());
    }

    private void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
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
