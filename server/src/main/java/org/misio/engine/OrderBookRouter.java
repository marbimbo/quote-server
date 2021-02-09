package org.misio.engine;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.misio.websocketfeed.SymbolFeed;
import org.misio.websocketfeed.WebSocketWrapper;
import org.misio.websocketfeed.config.BenchmarkConfig;
import org.misio.websocketfeed.config.TopicSecurityConfig;
import org.misio.websocketfeed.handler.LiveOrderBookHandler;
import org.misio.websocketfeed.message.OrderMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ch.qos.logback.core.encoder.ByteArrayUtil.hexStringToByteArray;

@Component
public class OrderBookRouter implements LiveOrderBookHandler {

    private static int port = 5555;
    private ZMQ.Socket socket;
    private WebSocketWrapper webSocketWrapper;
    private TopicSecurityConfig topicSecurityConfig;
    private BenchmarkConfig benchmarkConfig;
    private int counter = 0;

    @Autowired
    public void setWebSocketWrapper(WebSocketWrapper webSocketWrapper) {
        this.webSocketWrapper = webSocketWrapper;
    }

    @Autowired
    public void setTopicSecurityConfig(TopicSecurityConfig topicSecurityConfig) {
        this.topicSecurityConfig = topicSecurityConfig;
    }

    @Autowired
    public void setBenchmarkConfig(BenchmarkConfig benchmarkConfig) {
        this.benchmarkConfig = benchmarkConfig;
    }

    @Override
    public void handleMessages(String message) {
        socket.send("BTC : Hello".getBytes(ZMQ.CHARSET), 0);
    }


    @PostConstruct
    public void startServer() throws InterruptedException {
        ZContext context = new ZContext();
        ExecutorService executorService = Executors.newFixedThreadPool(webSocketWrapper.getSymbolFeeds().size());
//        webSocketWrapper.getSymbolFeeds().forEach(feed -> {
//            executorService.execute(() -> subscribe(feed, context));
//        });
        webSocketWrapper.getSymbolFeeds().forEach(feed -> subscribe(feed, context));
        System.out.println("open publishers");
    }

    private void subscribe(SymbolFeed symbol, ZContext context) {
        System.out.println("subscribed in thread: " + Thread.currentThread().getName());
        ZMQ.Socket socket = context.createSocket(SocketType.PUB);
        int zPort = symbol.getProductId().equals("BTC-GBP") ? 5555 : symbol.getProductId().equals("ETH-USDC") ? 5556 : 5557;
//        socket.bind("tcp://*:" + port);
//        socket.bind("tcp://*:" + zPort);
        socket.setCurveServer(true);
//        socket.setCurvePublicKey(hexStringToByteArray("54FCBA24E93249969316FB617C872BB0C1D1FF14800427C594CBFACF1BC2D652"));
        socket.setCurveSecretKey(hexStringToByteArray(topicSecurityConfig.getPrivateKey()));
        socket.bind("tcp://*:" + port);
        symbol.init();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        symbol.subscribe(new String[]{symbol.getProductId()},
                message -> {
//                    System.out.println(message);
                    OrderMessage orderMessage = objectMapper.readValue(message, OrderMessage.class);
                    if (orderMessage.getRemaining_size() == null) {
                        orderMessage.setRemaining_size(BigDecimal.valueOf(1));
                    }
                    if (orderMessage.getTime() != null && orderMessage.getPrice() != null) {
                        Instant instant = Instant.parse(orderMessage.getTime());
                        long epochSecond = instant.getEpochSecond();
                        int nano = instant.getNano();
                        String zeroMqMessage;
                        if (benchmarkConfig.isDeltaEnabled()) {
                            long delta = System.nanoTime();
                            zeroMqMessage = orderMessage.getProduct_id() + ",type=" + orderMessage.getType() + " price=" + orderMessage.getPrice() + ",side=\"" + orderMessage.getSide() + "\",remaining_size=" + orderMessage.getRemaining_size() + ",t1=" + delta + ",t2=<placeholder> " + (1_000_000_000 * epochSecond + nano);
                        } else {
                            zeroMqMessage = orderMessage.getProduct_id() + ",type=" + orderMessage.getType() + " price=" + orderMessage.getPrice() + ",side=\"" + orderMessage.getSide() + "\",remaining_size=" + orderMessage.getRemaining_size() + " " + (1_000_000_000 * epochSecond + nano);
                        }
//                        System.out.println("sending in thread: " + Thread.currentThread().getName() + " : " + symbol.getProductId());
                        socket.send(zeroMqMessage);
                    }
                });
        System.out.println("subscribed and publishing " + symbol.getProductId() + " on " + port);
        ++port;
    }
}
