package org.misio.client.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static org.misio.client.model.OrderMessageType.*;
import static org.misio.client.model.OrderSide.BUY;

public class LimitOrderBook {

    private final BidsComparator bidsComparator = new BidsComparator();
    private final AsksComparator asksComparator = new AsksComparator();
    private final List<Order> bids = new ArrayList<>();
    private final List<Order> asks = new ArrayList<>();
    private String symbol;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<Order> getBids() {
        return bids;
    }

    public List<Order> getAsks() {
        return asks;
    }

    public void handleOrder(Order order, String side, String type) {
        if (side.equals(BUY.getSide())) {
            handleBid(order, type);
        } else {
            handleAsk(order, type);
        }

    }

    private void handleBid(Order order, String type) {
        int indexOfOrder = Collections.binarySearch(bids, order, bidsComparator);
        if (indexOfOrder < 0) {
            int insertionPoint = -1 * indexOfOrder - 1; // from binarySearch doc
            addItem(bids, order, insertionPoint, type);
        } else {
            updateItem(bids, order, indexOfOrder, type);
        }
    }

    private void handleAsk(Order order, String type) {
        int indexOfOrder = Collections.binarySearch(asks, order, asksComparator);
        if (indexOfOrder < 0) {
            int insertionPoint = -1 * indexOfOrder - 1; // from binarySearch doc
            addItem(asks, order, insertionPoint, type);
        } else {
            updateItem(asks, order, indexOfOrder, type);
        }
    }

    private void addItem(List<Order> orders, Order order, int insertionPoint, String type) {
        if (type.equals(RECEIVED.getType())) {
            order.setQuantity(1);
            orders.add(insertionPoint, order);
        }
    }

    private void updateItem(List<Order> orders, Order order, int index, String type) {
        Order updatedOrder = orders.get(index);
        updateSize(updatedOrder, order, type);
        updateQuantity(updatedOrder, type);
        if (updatedOrder.getSize().compareTo(ZERO) <= 0) { // should ever be less than zero ?
            orders.remove(index);
        }
    }

    private void updateSize(Order updatedOrder, Order order, String type) {
        if (type.equals(RECEIVED.getType())) {
            updatedOrder.setSize(updatedOrder.getSize().add(order.getSize()));
        } else if (type.equals(OPEN.getType())) {
            // do nothing
        } else if (type.equals(MATCH.getType())) {
            // do nothing
        } else if (type.equals(CANCEL.getType())) {
            // do nothing
        } else if (type.equals(DONE.getType())) {
            updatedOrder.setSize(updatedOrder.getSize().subtract(order.getSize()));
        } else if (type.equals(CHANGE.getType())) {
            updatedOrder.setSize(updatedOrder.getSize().add(order.getSize())); // because we are just sending a diff (new-old)
        }
    }

    private void updateQuantity(Order updatedOrder, String type) {
        if (type.equals(RECEIVED.getType())) {
            updatedOrder.setQuantity(updatedOrder.getQuantity() + 1);
        } else if (type.equals(OPEN.getType())) {
            // do nothing
        } else if (type.equals(MATCH.getType())) {
            // do nothing
        } else if (type.equals(CANCEL.getType())) {
            // do nothing
        } else if (type.equals(DONE.getType())) {
            updatedOrder.setQuantity(updatedOrder.getQuantity() - 1);
        } else if (type.equals(CHANGE.getType())) {
            // do nothing
        }
    }

    @Override
    public String toString() {
        return "LimitOrderBook{" +
                "bids=" + bids +
                ", asks=" + asks +
                ", symbol='" + symbol + '\'' +
                '}';
    }

    private static class BidsComparator implements Comparator<Order> {
        @Override
        public int compare(Order o1, Order o2) {
            return o2.getPrice().compareTo(o1.getPrice());
        }
    }

    private static class AsksComparator implements Comparator<Order> {
        @Override
        public int compare(Order o1, Order o2) {
            return o1.getPrice().compareTo(o2.getPrice());
        }
    }
}
