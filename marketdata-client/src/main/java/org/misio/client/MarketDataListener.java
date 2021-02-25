package org.misio.client;

import org.misio.client.model.LimitOrderBook;
import org.misio.client.model.Trade;

public interface MarketDataListener {

    /**
     * Callback method that is called when there is a MATCH message received from exchange.
     * MATCH message is normalized to trade object.
     *
     * @param trade the trade that is received from exchange
     */
    void onTrade(Trade trade);

    /**
     * Callback method that is called when order book is updated with new order item.
     *
     * @param book limit order book that is created from order items
     */
    void onBook(LimitOrderBook book);

    /**
     * Callback method that is called when error occurs on Quote Server side, e.g: lost connection to exchange server
     *
     * @param error error message
     */
    void onError(String error);

}
