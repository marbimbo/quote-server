package org.misio.client.impl;

import org.misio.client.model.LimitOrderBook;
import org.misio.client.model.Order;
import org.misio.client.model.Trade;

import java.math.BigDecimal;
import java.util.function.Consumer;

import static org.misio.client.model.OrderMessageType.MATCH;

class OrderParser {

    private final LimitOrderBook realtimeOrderBook = new LimitOrderBook();
    private String symbol;
    private Consumer<LimitOrderBook> orderBookCallback;
    private Consumer<Trade> tradeCallback;

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setOrderBookCallback(Consumer<LimitOrderBook> orderBookCallback) {
        this.orderBookCallback = orderBookCallback;
    }

    public void setTradeCallback(Consumer<Trade> tradeCallback) {
        this.tradeCallback = tradeCallback;
    }

    public void consume(String record) {
        // parse and transform
        parse(record);
        orderBookCallback.accept(realtimeOrderBook);
    }

    // Zmq/Flux format: 'LTC-GBP,type=done price=130.43,side="buy",remaining_size=101.01740227,t1=33647716972635,t2=<placeholder> 1614189593660848000'
    private void parse(String record) {
        // TODO proper dynamic parser
        Order order = new Order();
        String[] recordArray = record.split(" ");
        String[] topicAndTag = recordArray[0].split(",");
        realtimeOrderBook.setSymbol(topicAndTag[0]);
        String[] typeKVPair = topicAndTag[1].split("=");
        String type = typeKVPair[1];

        String[] fields = recordArray[1].split(",");
        String[] priceKVPair = fields[0].split("=");
        order.setPrice(new BigDecimal(priceKVPair[1]));
        String[] sideKVPair = fields[1].split("=");
        String side = sideKVPair[1];

        String[] sizeKVPair = fields[2].split("=");
        order.setSize(new BigDecimal(sizeKVPair[1]));

        if (type.equals(MATCH.getType())) {
            long timestamp = Long.parseLong(recordArray[2]);
            tradeCallback.accept(createTrade(order, side, timestamp));
        }

        realtimeOrderBook.handleOrder(order, side, type);
    }

    private Trade createTrade(Order order, String side, long timestamp) {
        Trade trade = new Trade();
        trade.setSymbol(symbol);
        trade.setPrice(order.getPrice());
        trade.setSide(side);
        trade.setTimestamp(timestamp);
        return trade;
    }
}
