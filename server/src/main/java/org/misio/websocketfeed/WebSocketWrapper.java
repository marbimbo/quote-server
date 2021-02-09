package org.misio.websocketfeed;

import org.misio.websocketfeed.config.WebSocketConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WebSocketWrapper {

    private List<String> productIds; // TODO: 04.02.21 strange... why does it work with list ?

    private List<SymbolFeed> symbolFeeds;
    private WebSocketConfig webSocketConfig;

    @Value("${productIds}")
    public void setProductIds(List<String> productIds) {
        this.productIds = productIds;
    }

    @Autowired
    public void setWebSocketConfig(WebSocketConfig webSocketConfig) {
        this.webSocketConfig = webSocketConfig;
    }

    @PostConstruct
    private void init() {
        symbolFeeds = productIds.stream()
                .map(productId -> { // TODO: 03.02.2021
                    SymbolFeed feed = new SymbolFeed();
                    feed.setProductId(productId);
                    feed.setWebsocketUrl(webSocketConfig.getBaseUrl());
                    feed.init();
                    return feed;
                })
                .collect(Collectors.toList());
    }

    public List<SymbolFeed> getSymbolFeeds() {
        return symbolFeeds;
    }
}
