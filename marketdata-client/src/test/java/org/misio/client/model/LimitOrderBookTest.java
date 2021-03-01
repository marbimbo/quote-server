package org.misio.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.misio.client.model.OrderMessageType.*;
import static org.misio.client.model.OrderSide.BUY;
import static org.misio.client.model.OrderSide.SELL;

class LimitOrderBookTest {

    private static LimitOrderBook orderBook;
    private static final String symbol = "BTC-GBP";

    private static Order createOrder(BigDecimal price, BigDecimal size, int quantity) {
        Order order = new Order();
        order.setPrice(price);
        order.setSize(size);
        order.setQuantity(quantity);
        return order;
    }

    @BeforeEach
    private void setUp() {
        orderBook = new LimitOrderBook();
        orderBook.setSymbol(symbol);
    }

    @Test
    void testReceivedBuyOrders() {
        String sideBuy = BUY.getSide();
        String type = RECEIVED.getType();

        Order order1 = new Order();
        order1.setPrice(BigDecimal.valueOf(40_000));
        order1.setSize(BigDecimal.valueOf(100));

        Order order2 = new Order();
        order2.setPrice(BigDecimal.valueOf(30_000));
        order2.setSize(BigDecimal.valueOf(200));

        Order order3 = new Order();
        order3.setPrice(BigDecimal.valueOf(40_000));
        order3.setSize(BigDecimal.valueOf(100));

        orderBook.handleOrder(order1, sideBuy, type);
        orderBook.handleOrder(order2, sideBuy, type);
        orderBook.handleOrder(order3, sideBuy, type);

        List<Order> expectedOrders = new ArrayList<>();
        expectedOrders.add(createOrder(BigDecimal.valueOf(40_000), BigDecimal.valueOf(200), 2));
        expectedOrders.add(createOrder(BigDecimal.valueOf(30_000), BigDecimal.valueOf(200), 1));

        Assertions.assertEquals(expectedOrders, orderBook.getBids());
    }

    @Test
    void testReceivedSellOrders() {
        String sideSell = SELL.getSide();
        String type = RECEIVED.getType();

        Order order1 = new Order();
        order1.setPrice(BigDecimal.valueOf(50_000));
        order1.setSize(BigDecimal.valueOf(200));

        Order order2 = new Order();
        order2.setPrice(BigDecimal.valueOf(45_000));
        order2.setSize(BigDecimal.valueOf(100));

        Order order3 = new Order();
        order3.setPrice(BigDecimal.valueOf(45_000));
        order3.setSize(BigDecimal.valueOf(100));

        orderBook.handleOrder(order1, sideSell, type);
        orderBook.handleOrder(order2, sideSell, type);
        orderBook.handleOrder(order3, sideSell, type);

        List<Order> expectedOrders = new ArrayList<>();
        expectedOrders.add(createOrder(BigDecimal.valueOf(45_000), BigDecimal.valueOf(200), 2));
        expectedOrders.add(createOrder(BigDecimal.valueOf(50_000), BigDecimal.valueOf(200), 1));

        Assertions.assertEquals(expectedOrders, orderBook.getAsks());
    }

    @Test
    void testReceivedAndDoneBuyOrders() {
        String sideBuy = BUY.getSide();
        String typeReceived = RECEIVED.getType();
        String typeDone = DONE.getType();

        Order order1 = new Order();
        order1.setPrice(BigDecimal.valueOf(40_000));
        order1.setSize(BigDecimal.valueOf(200));

        Order order2 = new Order();
        order2.setPrice(BigDecimal.valueOf(40_000));
        order2.setSize(BigDecimal.valueOf(200));

        orderBook.handleOrder(order1, sideBuy, typeReceived);
        orderBook.handleOrder(order2, sideBuy, typeDone);

        Assertions.assertEquals(Collections.emptyList(), orderBook.getBids());
    }

    @Test
    void testReceivedAndChangeBuyOrders() {
        String sideBuy = BUY.getSide();
        String typeReceived = RECEIVED.getType();
        String typeChange = CHANGE.getType();

        Order order1 = new Order();
        order1.setPrice(BigDecimal.valueOf(40_000));
        order1.setSize(BigDecimal.valueOf(200));

        Order order2 = new Order();
        order2.setPrice(BigDecimal.valueOf(40_000));
        order2.setSize(BigDecimal.valueOf(-100)); // diff: new_size - old_size

        orderBook.handleOrder(order1, sideBuy, typeReceived);
        orderBook.handleOrder(order2, sideBuy, typeChange);

        List<Order> expectedOrders = new ArrayList<>();
        expectedOrders.add(createOrder(BigDecimal.valueOf(40_000), BigDecimal.valueOf(100), 1));

        Assertions.assertEquals(expectedOrders, orderBook.getBids());
    }
}