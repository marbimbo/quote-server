package org.misio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("benchmark")
public class BenchmarkConfig {

    private boolean deltaEnabled;

    public boolean isDeltaEnabled() {
        return deltaEnabled;
    }

    public void setDeltaEnabled(boolean deltaEnabled) {
        this.deltaEnabled = deltaEnabled;
    }
}
