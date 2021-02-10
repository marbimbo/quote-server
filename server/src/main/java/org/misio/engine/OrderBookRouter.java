package org.misio.engine;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.misio.config.BenchmarkConfig;
import org.misio.websocketfeed.SymbolFeed;
import org.misio.websocketfeed.WebSocketWrapper;
import org.misio.websocketfeed.config.TopicSecurityConfig;
import org.misio.websocketfeed.handler.LiveOrderBookHandler;
import org.misio.websocketfeed.message.OrderMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.Instant;

import static ch.qos.logback.core.encoder.ByteArrayUtil.hexStringToByteArray;

@Component
public class OrderBookRouter implements LiveOrderBookHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private int port;
    private ZMQ.Socket socket;
    private WebSocketWrapper webSocketWrapper;
    private TopicSecurityConfig topicSecurityConfig;
    private BenchmarkConfig benchmarkConfig;
    private int counter = 0;

    @Value("${port}")
    public void setPort(int port) {
        this.port = port;
    }

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
//        ExecutorService executorService = Executors.newFixedThreadPool(webSocketWrapper.getSymbolFeed().size());
//        webSocketWrapper.getSymbolFeed().forEach(feed -> {
//            executorService.execute(() -> subscribe(feed, context));
//        });
        subscribe(webSocketWrapper.getSymbolFeed(), context);
//        webSocketWrapper.getSymbolFeed().forEach(feed -> subscribe(feed, context));
        LOG.info("open publishers");
    }

    private void subscribe(SymbolFeed symbol, ZContext context) {
        LOG.info("subscribed in thread: " + Thread.currentThread().getName());
        ZMQ.Socket socket = context.createSocket(SocketType.PUB);
        socket.setCurveServer(true);
        socket.setCurveSecretKey(hexStringToByteArray(topicSecurityConfig.getPrivateKey()));
        socket.bind("tcp://*:" + port);
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        symbol.subscribe(symbol.getProductIds(),
                message -> {
//                    LOG.debug(message);
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
                        LOG.debug("sending in thread: {} {}", Thread.currentThread().getName(), orderMessage.getProduct_id());
                        socket.send(zeroMqMessage);
                    }
                });
        LOG.debug("subscribed and publishing {} on port {}", symbol.getProductIds(), port);
        ++port;
    }
}
