package org.misio.consumer;

import org.misio.consumer.config.datastore.DatastoreConfig;
import org.misio.consumer.config.qs.BenchmarkConfig;
import org.misio.consumer.config.qs.QuoteServerConfig;
import org.misio.consumer.config.qs.TopicSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({QuoteServerConfig.class, TopicSecurity.class, DatastoreConfig.class, BenchmarkConfig.class})
public class ConsumerApp {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApp.class, args);
    }

}
