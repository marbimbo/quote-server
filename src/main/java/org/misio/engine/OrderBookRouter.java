package org.misio.engine;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.misio.websocketfeed.SymbolFeed;
import org.misio.websocketfeed.WebSocketWrapper;
import org.misio.websocketfeed.handler.LiveOrderBookHandler;
import org.misio.websocketfeed.message.OrderMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class OrderBookRouter implements LiveOrderBookHandler {

    private String[] productIds;
    private ZMQ.Socket socket;

//    private List<WebsocketFeed> feeds;

    private WebSocketWrapper webSocketWrapper;

    private static int port = 5555;

    @Autowired
    public void setWebSocketWrapper(WebSocketWrapper webSocketWrapper) {
        this.webSocketWrapper = webSocketWrapper;
    }

    private int counter = 0;

//    @Autowired
//    public void setFeed(WebsocketFeed feed) {
//        this.feed = feed;
//    }

    @Override
    public void handleMessages(String message) {
//        System.out.println(message.getProduct_id());
        socket.send("BTC : Hello".getBytes(ZMQ.CHARSET), 0);
    }

    public String[] getProductIds() {
        return productIds;
    }

    @Value("${productIds}")
    public void setProductIds(String[] productIds) {
        this.productIds = productIds;
    }

    @PostConstruct
    public void startServer() throws InterruptedException {
        ZContext context = new ZContext();
        ExecutorService executorService = Executors.newFixedThreadPool(webSocketWrapper.getSymbolFeeds().size());
//        webSocketWrapper.getSymbolFeeds().forEach(feed -> {
//            executorService.execute(() -> subscribe(feed, context));
//        });
        webSocketWrapper.getSymbolFeeds().forEach(feed -> subscribe(feed, context));
//        feeds.add(subscribe("BTC-GBP", context, 5555));
//        feeds.add(subscribe("ETH-USD", context, 5556));
//        feeds.add(subscribe("BTC-GBP", context, 5557));
//        try (ZContext context = new ZContext()) {

            //  Socket to talk to clients
            System.out.println("open publishers");
//            socket = context.createSocket(SocketType.PUB);
//            socket.bind("tcp://*:5555");
//
////            feed.subscribe(productIds,
////                    message -> {
//////                        ++counter;
//////                        socket.send(("BTC : " +  counter).getBytes(ZMQ.CHARSET), 0);
////                        socket.send(("BTC : " + message).getBytes(ZMQ.CHARSET), 0);
////                    });
//
//        feed.subscribe(new String[]{"BTC-GBP"},
//                message -> {
//                    socket.send(("BTC : " + message).getBytes(ZMQ.CHARSET), 0);
//                });
//
//            ZMQ.Socket ethSocket = context.createSocket(SocketType.PUB);
//            ethSocket.bind("tcp://*:5556");
//
//            feed.subscribe(new String[]{"ETH-USD"},
//                message -> {
//                    ethSocket.send(("ETH : " + message).getBytes(ZMQ.CHARSET), 0);
//                });
//
//        ZMQ.Socket omgSocket = context.createSocket(SocketType.PUB);
//        omgSocket.bind("tcp://*:5557");
//
//        feed.subscribe(new String[]{"OMG-EUR"},
//                message -> {
//                    omgSocket.send(("OMG : " + message).getBytes(ZMQ.CHARSET), 0);
//                });



//            while (!Thread.currentThread().isInterrupted()) {
////                byte[] reply = socket.recv(0);
////                System.out.println(
////                        "Received " + ": [" + new String(reply, ZMQ.CHARSET) + "]"
////                );
////                String response = "world";
////                socket.send(response.getBytes(ZMQ.CHARSET), 0);
//                socket.send("BTC : Hello".getBytes(ZMQ.CHARSET), 0);
////                Thread.sleep(1000); //  Do some 'work'
//            }
//        }
    }

    private static void subscribe(SymbolFeed symbol, ZContext context) {
        System.out.println("subscribed in thread: " + Thread.currentThread().getName());
        ZMQ.Socket socket = context.createSocket(SocketType.PUB);
        int zPort = symbol.getProductId().equals("BTC-GBP") ? 5555 : symbol.getProductId().equals("ETH-USDC") ? 5556 : 5557;
//        socket.bind("tcp://*:" + port);
//        socket.bind("tcp://*:" + zPort);
        socket.bind("tcp://*:" + port);
        symbol.init();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        symbol.subscribe(new String[]{symbol.getProductId()},
                message -> {
                    System.out.println(message);
                    OrderMessage orderMessage =  objectMapper.readValue(message, OrderMessage.class);
                    if (orderMessage.getRemaining_size() == null) {
                        orderMessage.setRemaining_size(BigDecimal.valueOf(1));
//                        System.out.println("remaining_size: " + orderMessage.getRemaining_size());
//                        System.out.println(message);
//                        System.out.println(orderMessage);
                    }
                    if (orderMessage.getTime() != null && orderMessage.getPrice() != null) {
                        Instant instant = Instant.parse(orderMessage.getTime());
                        long epochSecond = instant.getEpochSecond();
                        int nano = instant.getNano();
//                        System.out.println("epoch : " + epochSecond + " " + nano + " nano sum : " + (1000000 * epochSecond + nano));
                        String zeroMqMessage = orderMessage.getProduct_id() + " price=" + orderMessage.getPrice() + ",type=\"" + orderMessage.getType() + "\",side=\"" + orderMessage.getSide() + "\",remaining_size=" + orderMessage.getRemaining_size() + " " + (1_000_000_000 * epochSecond + nano);
//                    System.out.println("sending " + symbol.getProductId());
//                    socket.send(("OMG-EUR : " + message).getBytes(ZMQ.CHARSET), 0);
//                    System.out.println("sending in thread: " + Thread.currentThread().getName() + " : " + symbol.getProductId());
//                    socket.send((symbol.getProductId() + " : " + message).getBytes(ZMQ.CHARSET), 0);
//                    socket.send(symbol.getProductId() + " : " + orderMessage/*.getBytes(ZMQ.CHARSET)*/, 0);
                        socket.send(zeroMqMessage);
                    }
                });
        System.out.println("subscribed and publishing " + symbol.getProductId() + " on " + port);
        ++port;
    }
}
