package org.misio.client.model;

import java.math.BigDecimal;

/**
 * https://docs.pro.coinbase.com/#the-full-channel
 * Match
 * A trade occurred between two orders.
 */
public class Trade {
    private String symbol;
    private BigDecimal price;
    private String side;
    private long timestamp;

    public void setSymbol(final String symbol) {
        this.symbol = symbol;
    }

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }

    public void setSide(final String side) {
        this.side = side;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "symbol='" + symbol + '\'' +
                ", price=" + price +
                ", side='" + side + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
