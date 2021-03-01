package org.misio.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

class LimitOrderBookTest {

    private static LimitOrderBook orderBook;

    @BeforeEach
    private void setUp() {
        orderBook = new LimitOrderBook();
        orderBook.setSymbol("BTC-GBP");
    }

    @Test
    void testHandleReceivedBuyOrders() {
        String sideBuy = "\"buy\"";
        String type = "received";

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
    void testHandleReceivedSellOrders() {
        String sideSell = "\"sell\"";
        String type = "received";

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

    // TODO: 01.03.2021 change orders, done orders

    private static Order createOrder(BigDecimal price, BigDecimal size, int quantity) {
        Order order = new Order();
        order.setPrice(price);
        order.setSize(size);
        order.setQuantity(quantity);
        return order;
    }
}