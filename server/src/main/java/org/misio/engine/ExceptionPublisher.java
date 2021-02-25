package org.misio.engine;

import org.misio.websocketfeed.ExceptionHandler;
import org.misio.websocketfeed.config.TopicSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.annotation.PostConstruct;

import static org.misio.config.CurveEncryptUtil.hexStringToByteArray;

@Component
class ExceptionPublisher implements ExceptionHandler {

    static final String EXCEPTION_TOPIC = "EXCEPTION";

    private TopicSecurityConfig topicSecurityConfig;

    private ZMQ.Socket socket;
    private int exceptionPort;

    @Autowired
    public void setTopicSecurityConfig(TopicSecurityConfig topicSecurityConfig) {
        this.topicSecurityConfig = topicSecurityConfig;
    }

    @Value("${exceptionPort}")
    public void setExceptionPort(int exceptionPort) {
        this.exceptionPort = exceptionPort;
    }

    public int getExceptionPort() {
        return exceptionPort;
    }

    @PostConstruct
    public void init() {
        ZContext context = new ZContext();
        socket = context.createSocket(SocketType.PUB);
        if (topicSecurityConfig.isEnabled()) {
            socket.setCurveServer(true);
            socket.setCurveSecretKey(hexStringToByteArray(topicSecurityConfig.getPrivateKey()));
        }
        socket.bind("tcp://*:" + exceptionPort);
    }

    @Override
    public void handleException(String exception) {
        socket.send(EXCEPTION_TOPIC + exception);
    }
}
