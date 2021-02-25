package org.misio.engine;

import org.misio.config.BenchmarkConfig;
import org.misio.websocketfeed.MessageHandler;
import org.misio.websocketfeed.config.TopicSecurityConfig;
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

import static org.misio.config.CurveEncryptUtil.hexStringToByteArray;

@Component
class OrderRecordPublisher implements MessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private TopicSecurityConfig topicSecurityConfig;
    private BenchmarkConfig benchmarkConfig;

    private ZMQ.Socket socket;
    private int orderPort;

    @Autowired
    public void setTopicSecurityConfig(TopicSecurityConfig topicSecurityConfig) {
        this.topicSecurityConfig = topicSecurityConfig;
    }

    @Autowired
    public void setBenchmarkConfig(BenchmarkConfig benchmarkConfig) {
        this.benchmarkConfig = benchmarkConfig;
    }

    public int getOrderPort() {
        return orderPort;
    }

    @Value("${orderPort}")
    public void setOrderPort(int orderPort) {
        this.orderPort = orderPort;
    }

    @PostConstruct
    public void init() {
        ZContext context = new ZContext();
        socket = context.createSocket(SocketType.PUB);
        if (topicSecurityConfig.isEnabled()) {
            socket.setCurveServer(true);
            socket.setCurveSecretKey(hexStringToByteArray(topicSecurityConfig.getPrivateKey()));
        }
        socket.bind("tcp://*:" + orderPort);
    }

    @Override
    public void handleMessage(OrderMessage message) {
        LOG.debug(message.toString());

        // TODO decode
        if (message.getType().equals("subscriptions")) {
            return;
        }
        if (message.getOrder_type() != null && message.getOrder_type().equals("market")) { // return if received market 'received' message
            return;
        }
        // market orders will not have a remaining_size or price field as they are never on the open order book at a given price.
        // https://docs.pro.coinbase.com/#the-full-channel
        if (message.getType().equals("done") && message.getRemaining_size() == null) { // FIXME
            return;
        }

        BigDecimal size;
        // TODO OrderMessage should be extended and decoded to specific types
        if (message.getRemaining_size() != null) {
            size = message.getRemaining_size();
        } else if (message.getSize() != null) {
            size = message.getSize();
        } else if (message.getNew_size() != null) {
            size = message.getNew_size().subtract(message.getOld_size());
        } else if (message.getNew_funds() != null) {
            size = message.getNew_funds().subtract(message.getOld_funds());
        } else { // should not happen
            throw new RuntimeException("size of order unknown! " + message);
        }

        if (message.getTime() != null && message.getPrice() != null) {
            Instant instant = Instant.parse(message.getTime());
            long epochSecond = instant.getEpochSecond();
            int nano = instant.getNano();
            String zeroMqMessage;
            if (benchmarkConfig.isDeltaEnabled()) {
                long delta = System.nanoTime();
                zeroMqMessage = message.getProduct_id() + ",type=" + message.getType() + " price=" + message.getPrice() + ",side=\"" + message.getSide() + "\",size=" + size + ",t1=" + delta + ",t2=<placeholder> " + (1_000_000_000 * epochSecond + nano);
            } else {
                zeroMqMessage = message.getProduct_id() + ",type=" + message.getType() + " price=" + message.getPrice() + ",side=\"" + message.getSide() + "\",size=" + size + " " + (1_000_000_000 * epochSecond + nano);
            }
            LOG.debug("sending in thread: {} {}", Thread.currentThread().getName(), message.getProduct_id());
            socket.send(zeroMqMessage);
        }
    }
}
