package org.misio.consumer.config.qs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("quote-server")
public class QuoteServerConfig {

    private int port;
    private int exceptionPort;
    private String schema;
    private String hostname;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getExceptionPort() {
        return exceptionPort;
    }

    public void setExceptionPort(int exceptionPort) {
        this.exceptionPort = exceptionPort;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
