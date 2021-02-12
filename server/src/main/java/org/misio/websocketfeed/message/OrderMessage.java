package org.misio.websocketfeed.message;

import java.math.BigDecimal;

public class OrderMessage {

    String product_id;
    String type;
    Long sequence;
    String side;
    BigDecimal price;
    BigDecimal remaining_size;
    String time;

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getRemaining_size() {
        return remaining_size;
    }

    public void setRemaining_size(BigDecimal remaining_size) {
        this.remaining_size = remaining_size;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "OrderMessage{" +
                "product_id='" + product_id + '\'' +
                ", type='" + type + '\'' +
                ", sequence=" + sequence +
                ", side='" + side + '\'' +
                ", price=" + price +
                ", remaining_size=" + remaining_size +
                ", time='" + time + '\'' +
                '}';
    }
}
