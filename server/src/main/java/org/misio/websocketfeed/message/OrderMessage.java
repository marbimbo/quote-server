package org.misio.websocketfeed.message;

import java.math.BigDecimal;

/**
 * One class used for all types of messages. Should be split into extending classes
 */
public class OrderMessage {

    private String product_id;
    private String type;
    private Long sequence;
    private String side;
    private BigDecimal price;
    private BigDecimal remaining_size;
    private BigDecimal size;
    private String time;
    private String order_type;

    // for CHANGE messages
    private BigDecimal new_size;
    private BigDecimal old_size;
    private BigDecimal new_funds;
    private BigDecimal old_funds;

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

    public BigDecimal getSize() {
        return size;
    }

    public void setSize(BigDecimal size) {
        this.size = size;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOrder_type() {
        return order_type;
    }

    public void setOrder_type(String order_type) {
        this.order_type = order_type;
    }

    public BigDecimal getNew_size() {
        return new_size;
    }

    public void setNew_size(BigDecimal new_size) {
        this.new_size = new_size;
    }

    public BigDecimal getOld_size() {
        return old_size;
    }

    public void setOld_size(BigDecimal old_size) {
        this.old_size = old_size;
    }

    public BigDecimal getNew_funds() {
        return new_funds;
    }

    public void setNew_funds(BigDecimal new_funds) {
        this.new_funds = new_funds;
    }

    public BigDecimal getOld_funds() {
        return old_funds;
    }

    public void setOld_funds(BigDecimal old_funds) {
        this.old_funds = old_funds;
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
                ", size=" + size +
                ", time='" + time + '\'' +
                ", order_type='" + order_type + '\'' +
                ", new_size=" + new_size +
                ", old_size=" + old_size +
                ", new_funds=" + new_funds +
                ", old_funds=" + old_funds +
                '}';
    }
}
