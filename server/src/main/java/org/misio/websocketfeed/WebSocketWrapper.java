package org.misio.websocketfeed;

import org.misio.websocketfeed.config.WebSocketConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class WebSocketWrapper {

    private String[] productIds;

    private SymbolFeed symbolFeed;
    private WebSocketConfig webSocketConfig;

    @Value("${productIds}")
    public void setProductIds(String[] productIds) {
        this.productIds = productIds;
    }

    @Autowired
    public void setWebSocketConfig(WebSocketConfig webSocketConfig) {
        this.webSocketConfig = webSocketConfig;
    }

    @PostConstruct
    private void init() {
        symbolFeed = new SymbolFeed();
        symbolFeed.setProductIds(productIds);
        symbolFeed.setWebsocketUrl(webSocketConfig.getBaseUrl());
    }

    public SymbolFeed getSymbolFeed() {
        return symbolFeed;
    }
}
