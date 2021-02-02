package org.misio.engine;

import org.misio.websocketfeed.WebsocketFeed;
import org.misio.websocketfeed.handler.LiveOrderBookHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.annotation.PostConstruct;

@Component
public class OrderBookRouter implements LiveOrderBookHandler {

    private String[] productIds;
    private ZMQ.Socket socket;

    private WebsocketFeed feed;

    private int counter = 0;

    @Autowired
    public void setFeed(WebsocketFeed feed) {
        this.feed = feed;
    }

    @Override
    public void handleMessages(String message) {
//        System.out.println(message.getProduct_id());
        socket.send("BTC : Hello".getBytes(ZMQ.CHARSET), 0);
    }

    public String[] getProductIds() {
        return productIds;
    }

    @Value("${productIds}")
    public void setProductIds(String[] productIds) {
        this.productIds = productIds;
    }

    @PostConstruct
    public void startServer() throws InterruptedException {
        ZContext context = new ZContext();
//        try (ZContext context = new ZContext()) {

            //  Socket to talk to clients
            System.out.println("open publisher");
            socket = context.createSocket(SocketType.PUB);
            socket.bind("tcp://*:5555");

//            feed.subscribe(productIds,
//                    message -> {
////                        ++counter;
////                        socket.send(("BTC : " +  counter).getBytes(ZMQ.CHARSET), 0);
//                        socket.send(("BTC : " + message).getBytes(ZMQ.CHARSET), 0);
//                    });

        feed.subscribe(new String[]{"BTC-GBP"},
                message -> {
                    socket.send(("BTC : " + message).getBytes(ZMQ.CHARSET), 0);
                });

            ZMQ.Socket ethSocket = context.createSocket(SocketType.PUB);
            ethSocket.bind("tcp://*:5556");

            feed.subscribe(new String[]{"ETH-USDC"},
                message -> {
                    ethSocket.send(("ETH : " + message).getBytes(ZMQ.CHARSET), 0);
                });

        ZMQ.Socket omgSocket = context.createSocket(SocketType.PUB);
        omgSocket.bind("tcp://*:5557");

        feed.subscribe(new String[]{"OMG-EUR"},
                message -> {
                    omgSocket.send(("OMG : " + message).getBytes(ZMQ.CHARSET), 0);
                });



//            while (!Thread.currentThread().isInterrupted()) {
////                byte[] reply = socket.recv(0);
////                System.out.println(
////                        "Received " + ": [" + new String(reply, ZMQ.CHARSET) + "]"
////                );
////                String response = "world";
////                socket.send(response.getBytes(ZMQ.CHARSET), 0);
//                socket.send("BTC : Hello".getBytes(ZMQ.CHARSET), 0);
////                Thread.sleep(1000); //  Do some 'work'
//            }
//        }
    }
}
