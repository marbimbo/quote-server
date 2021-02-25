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

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setSide(String side) {
        this.side = side;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "symbol='" + symbol + '\'' +
                ", price=" + price +
                ", side='" + side + '\'' +
                '}';
    }
}
