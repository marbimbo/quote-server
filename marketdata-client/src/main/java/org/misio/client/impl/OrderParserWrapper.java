package org.misio.client.impl;

import org.misio.client.model.LimitOrderBook;
import org.misio.client.model.Trade;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class OrderParserWrapper {

    private Consumer<LimitOrderBook> orderBookCallback;
    private Consumer<Trade> tradeCallback;
    private List<OrderParser> parsers = new ArrayList<>(); // keep for unsubscribe

    public void setOrderBookCallback(Consumer<LimitOrderBook> orderBookCallback) {
        this.orderBookCallback = orderBookCallback;
    }

    public void setTradeCallback(Consumer<Trade> tradeCallback) {
        this.tradeCallback = tradeCallback;
    }

    public Consumer<String> subscribe(String symbol) {
        OrderParser parser = new OrderParser();
        parser.setSymbol(symbol);
        parser.setOrderBookCallback(orderBookCallback);
        parser.setTradeCallback(tradeCallback);
        parsers.add(parser);
        return parser::consume;
    }
}
