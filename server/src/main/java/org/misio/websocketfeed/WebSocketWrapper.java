package org.misio.websocketfeed;

import org.misio.websocketfeed.config.WebSocketConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class WebSocketWrapper {

    private String[] productIds; // TODO: 04.02.21 strange... why does it work with list ?
    private String[] productA; // TODO: 04.02.21 strange... why does it work with list ?
    private String[] productB; // TODO: 04.02.21 strange... why does it work with list ?
    private String option;

    private SymbolFeed symbolFeed;
    private WebSocketConfig webSocketConfig;

    @Value("${productIds}")
    public void setProductIds(String[] productIds) {
        this.productIds = productIds;
    }

    @Value("${productA}")
    public void setProductA(String[] productA) {
        this.productA = productA;
    }

    @Value("${productB}")
    public void setProductB(String[] productB) {
        this.productB = productB;
    }

    @Value("${option}")
    public void setOption(String option) {
        this.option = option;
    }

    @Autowired
    public void setWebSocketConfig(WebSocketConfig webSocketConfig) {
        this.webSocketConfig = webSocketConfig;
    }

    @PostConstruct
    private void init() {
        symbolFeed = new SymbolFeed();
        if (option.equals("A")) {
            symbolFeed.setProductIds(productA);
        } else if (option.equals("B")) {
            symbolFeed.setProductIds(productB);
        } else {
            symbolFeed.setProductIds(productIds);
        }
        symbolFeed.setWebsocketUrl(webSocketConfig.getBaseUrl());
        symbolFeed.init();
    }

    public SymbolFeed getSymbolFeed() {
        return symbolFeed;
    }
}
