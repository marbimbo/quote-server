package org.misio.client.model;

import java.math.BigDecimal;

public class Order {

    private BigDecimal price;
    private BigDecimal size;
    private int quantity;

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getSize() {
        return size;
    }

    public void setSize(BigDecimal size) {
        this.size = size;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Order{" +
                "price=" + price +
                ", size=" + size +
                ", quantity=" + quantity +
                '}';
    }
}
