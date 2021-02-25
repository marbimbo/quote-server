package org.misio.client;

public interface MarketDataClient {

    /**
     * Connects to Quote Server. Needs to be invoked before calling {@link MarketDataClient#subscribe MarketDataClient.subscribe}
     *
     * @param listener the listener that will handle market data
     */
    void connect(MarketDataListener listener);

    /**
     * Subscribes to market data associated with particular symbol, for example: 'BTC-GBP', 'ETH-USDC'.
     *
     * @param symbol the cryptocurrency symbol
     */
    void subscribe(String symbol);

    /**
     * Closes connection to Quote Server and releases resources
     */
    void close();

}
