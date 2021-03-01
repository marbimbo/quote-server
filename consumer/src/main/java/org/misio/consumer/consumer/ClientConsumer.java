package org.misio.consumer.consumer;

import org.misio.client.MarketDataClient;
import org.misio.client.MarketDataListener;
import org.misio.client.impl.ZmqConfig;
import org.misio.client.impl.ZmqMarketDataClient;
import org.misio.client.model.LimitOrderBook;
import org.misio.client.model.Trade;
import org.misio.consumer.config.qs.QuoteServerConfig;
import org.misio.consumer.config.qs.TopicSecurity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;

@Component
@ConditionalOnProperty(name = "consumers.market-data", havingValue = "true")
public class ClientConsumer implements MarketDataListener {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private QuoteServerConfig quoteServerConfig;
    private TopicSecurity topicSecurity;

    @Autowired
    public void setQuoteServerConfig(QuoteServerConfig quoteServerConfig) {
        this.quoteServerConfig = quoteServerConfig;
    }

    @Autowired
    public void setTopicSecurity(TopicSecurity topicSecurity) {
        this.topicSecurity = topicSecurity;
    }

    @PostConstruct
    private void initClient() {
        ZmqConfig zmqConfig = new ZmqConfig();
        zmqConfig.setHostname(quoteServerConfig.getHostname());
        zmqConfig.setPort(quoteServerConfig.getServicePort());
        // encryption
        zmqConfig.setCurveEnabled(true);
        zmqConfig.setClientPrivateKey(topicSecurity.getClientConfig().getPrivateKey());
        zmqConfig.setClientPublicKey(topicSecurity.getClientConfig().getPublicKey());
        zmqConfig.setServerPublicKey(topicSecurity.getServerConfig().getPublicKey());

        MarketDataClient marketDataClient = new ZmqMarketDataClient(zmqConfig);

        marketDataClient.connect(this);
        marketDataClient.subscribe("BTC-GBP");
    }

    @Override
    public void onTrade(Trade trade) {
        LOG.info(trade.toString());
    }

    @Override
    public void onBook(LimitOrderBook book) {
        LOG.info(book.toString());
    }

    @Override
    public void onError(String error) {
        LOG.warn(error);
    }
}
