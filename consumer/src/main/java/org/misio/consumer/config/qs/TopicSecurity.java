package org.misio.consumer.config.qs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("security")
public class TopicSecurity {

    private ClientConfig clientConfig;

    private ServerConfig serverConfig;

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public void setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }
}
