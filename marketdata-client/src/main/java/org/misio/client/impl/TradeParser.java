package org.misio.client.impl;

import org.misio.client.model.Trade;

import java.util.function.Consumer;

public class TradeParser {
    private Consumer<Trade> tradeCallback;

    public void setTradeCallback(Consumer<Trade> tradeCallback) {
        this.tradeCallback = tradeCallback;
    }

    public void consume(String record) {
        // transform record
        tradeCallback.accept(new Trade());
    }
}
