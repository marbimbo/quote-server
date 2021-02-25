package org.misio.engine;

import org.misio.websocketfeed.SymbolFeed;
import org.misio.websocketfeed.WebSocketWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;

import static org.misio.engine.ExceptionPublisher.EXCEPTION_TOPIC;

@Component
public class OrderBookRouter {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private OrderRecordPublisher orderRecordPublisher;
    private ExceptionPublisher exceptionPublisher;
    private WebSocketWrapper webSocketWrapper;

    @Autowired
    public void setOrderRecordPublisher(OrderRecordPublisher orderRecordPublisher) {
        this.orderRecordPublisher = orderRecordPublisher;
    }

    @Autowired
    public void setExceptionPublisher(ExceptionPublisher exceptionPublisher) {
        this.exceptionPublisher = exceptionPublisher;
    }

    @Autowired
    public void setWebSocketWrapper(WebSocketWrapper webSocketWrapper) {
        this.webSocketWrapper = webSocketWrapper;
    }

    @PostConstruct
    public void startServer() {
        ZContext context = new ZContext();
        subscribe(webSocketWrapper.getSymbolFeed());
        LOG.info("open publishers");
    }

    private void subscribe(SymbolFeed symbol) {
        symbol.setMessageHandler(orderRecordPublisher);
        symbol.setExceptionHandler(exceptionPublisher);
        symbol.init();
        LOG.info("subscribed and publishing {} on port {}", symbol.getProductIds(), orderRecordPublisher.getOrderPort());
        LOG.info("subscribed and publishing {} on port {}", EXCEPTION_TOPIC, exceptionPublisher.getExceptionPort());
    }
}
