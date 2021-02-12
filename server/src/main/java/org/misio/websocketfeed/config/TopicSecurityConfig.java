package org.misio.websocketfeed.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("security")
public class TopicSecurityConfig {

    private boolean enabled;
    private String privateKey;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
