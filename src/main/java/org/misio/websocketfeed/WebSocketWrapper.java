package org.misio.websocketfeed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WebSocketWrapper {

    private List<String> productIds; // TODO: 04.02.21 strange... why does it work with list ?

    private List<SymbolFeed> symbolFeeds;

    private String websocketUrl;
    private Set<String> subscribedChannels;
    private Boolean isEnabled;
    private String key;
    private String passphrase;

    @Value("${productIds}")
    public void setProductIds(List<String> productIds) {
        this.productIds = productIds;
    }

    @Value("${websocket.baseUrl}")
    public void setWebsocketUrl(String websocketUrl) {
        this.websocketUrl = websocketUrl;
    }

    @Value("${websocket.enabled}")
    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    @Value("${gdax.key}")
    public void setKey(String key) {
        this.key = key;
    }

    @Value("${gdax.passphrase}")
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    @PostConstruct
    private void init() {
        symbolFeeds = productIds.stream()
                .map(productId -> { // TODO: 03.02.2021
                    SymbolFeed feed = new SymbolFeed();
                    feed.setProductId(productId);
                    feed.setEnabled(isEnabled);
                    feed.setWebsocketUrl(websocketUrl);
                    feed.setKey(key);
                    feed.setPassphrase(passphrase);
                    feed.init();
                    return feed;
                })
                .collect(Collectors.toList());
    }

    public List<SymbolFeed> getSymbolFeeds() {
        return symbolFeeds;
    }
}
