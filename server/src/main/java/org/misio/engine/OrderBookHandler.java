package org.misio.engine;

import org.misio.websocketfeed.WebsocketFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Component
public class OrderBookHandler {

    private static Logger log = LoggerFactory.getLogger(OrderBookHandler.class);

    private WebsocketFeed feed;

//    private List<OrderBookRouter> routers;
    private OrderBookRouter router;

    private String[] productIds;

    @Value("${productIds}")
    public void setProductIds(String[] productIds) {
        this.productIds = productIds;
    }

//    @Autowired
    public void setFeed(WebsocketFeed feed) {
        this.feed = feed;
    }

    @Autowired
    public void setRouter(OrderBookRouter router) {
        this.router = router;
    }

    @PostConstruct
    private void init() {
        log.info("productIds: {}", productIds.length);
        log.info("productIds: {}", Arrays.toString(productIds));
//        router.setProductIds(new String[]{"BTC-GBP", "ETH-USDC", "OMG-EUR"});
//        feed.subscribe(router.getProductIds(), router);
//        routers.forEach(router -> feed.subscribe(router.getProductIds(), router));


//        feed.subscribe("BTC-GBP", this);
//        feed.subscribe("ETH-USDC", this);
//        feed.subscribe("OMG-EUR", this);
    }
}
