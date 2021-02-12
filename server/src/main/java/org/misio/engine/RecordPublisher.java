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
class RecordPublisher implements MessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private TopicSecurityConfig topicSecurityConfig;
    private BenchmarkConfig benchmarkConfig;

    private ZMQ.Socket socket;
    private int port;

    @Autowired
    public void setTopicSecurityConfig(TopicSecurityConfig topicSecurityConfig) {
        this.topicSecurityConfig = topicSecurityConfig;
    }

    @Autowired
    public void setBenchmarkConfig(BenchmarkConfig benchmarkConfig) {
        this.benchmarkConfig = benchmarkConfig;
    }

    public int getPort() {
        return port;
    }

    @Value("${port}")
    public void setPort(int port) {
        this.port = port;
    }

    @PostConstruct
    public void init() {
        ZContext context = new ZContext();
        socket = context.createSocket(SocketType.PUB);
        if (topicSecurityConfig.isEnabled()) {
            socket.setCurveServer(true);
            socket.setCurveSecretKey(hexStringToByteArray(topicSecurityConfig.getPrivateKey()));
        }
        socket.bind("tcp://*:" + port);
    }

    @Override
    public void handleMessage(OrderMessage message) {
        LOG.debug(message.toString());
        if (message.getRemaining_size() == null) {
            message.setRemaining_size(BigDecimal.valueOf(1));
        }
        if (message.getTime() != null && message.getPrice() != null) {
            Instant instant = Instant.parse(message.getTime());
            long epochSecond = instant.getEpochSecond();
            int nano = instant.getNano();
            String zeroMqMessage;
            if (benchmarkConfig.isDeltaEnabled()) {
                long delta = System.nanoTime();
                zeroMqMessage = message.getProduct_id() + ",type=" + message.getType() + " price=" + message.getPrice() + ",side=\"" + message.getSide() + "\",remaining_size=" + message.getRemaining_size() + ",t1=" + delta + ",t2=<placeholder> " + (1_000_000_000 * epochSecond + nano);
            } else {
                zeroMqMessage = message.getProduct_id() + ",type=" + message.getType() + " price=" + message.getPrice() + ",side=\"" + message.getSide() + "\",remaining_size=" + message.getRemaining_size() + " " + (1_000_000_000 * epochSecond + nano);
            }
            LOG.debug("sending in thread: {} {}", Thread.currentThread().getName(), message.getProduct_id());
            socket.send(zeroMqMessage);
        }
    }
}
