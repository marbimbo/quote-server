package org.misio;

import org.misio.websocketfeed.config.BenchmarkConfig;
import org.misio.websocketfeed.config.TopicSecurityConfig;
import org.misio.websocketfeed.config.WebSocketConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({WebSocketConfig.class, TopicSecurityConfig.class, BenchmarkConfig.class})
public class QuoteServerApp {

    public static void main(String[] args) {
        SpringApplication.run(QuoteServerApp.class, args);
    }

}
