package org.misio.websocketfeed.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("websocket")
public class WebSocketConfig {

    private String baseUrl;

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
