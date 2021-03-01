package org.misio.client.model;

public enum OrderSide {

    BUY("\"buy\""),
    SELL("\"sell\"");

    private final String side;

    OrderSide(String side) {
        this.side = side;
    }

    public String getSide() {
        return side;
    }
}
