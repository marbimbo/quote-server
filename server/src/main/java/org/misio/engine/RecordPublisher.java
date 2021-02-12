package org.misio.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class RecordPublisher implements MessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private TopicSecurityConfig topicSecurityConfig;
    private BenchmarkConfig benchmarkConfig;

    private ZMQ.Socket socket;
    private int port;
    private ObjectMapper objectMapper;

    @Autowired
    public void setTopicSecurityConfig(TopicSecurityConfig topicSecurityConfig) {
        this.topicSecurityConfig = topicSecurityConfig;
    }

    @Autowired
    public void setBenchmarkConfig(BenchmarkConfig benchmarkConfig) {
        this.benchmarkConfig = benchmarkConfig;
    }

    @Value("${port}")
    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    @PostConstruct
    public void init() {
        objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ZContext context = new ZContext();
        socket = context.createSocket(SocketType.PUB);
        if (topicSecurityConfig.isEnabled()) {
            socket.setCurveServer(true);
            socket.setCurveSecretKey(hexStringToByteArray(topicSecurityConfig.getPrivateKey()));
        }
        socket.bind("tcp://*:" + port);
    }

    @Override
    public void handleMessage(String message) throws JsonProcessingException {
        LOG.debug(message);
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
    }
}
