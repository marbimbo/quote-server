package org.misio.client.impl;

import org.misio.client.MarketDataClient;
import org.misio.client.MarketDataListener;
import org.springframework.web.reactive.function.client.WebClient;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.time.Duration;
import java.util.function.Consumer;

import static org.misio.config.CurveEncryptUtil.hexStringToByteArray;

public class ZmqMarketDataClient implements MarketDataClient {

    private final String TCP_SCHEMA = "tcp"; // unicast

    private ZMQ.Socket orderSocket;
    private ZMQ.Socket exceptionSocket;
    private ZmqConfig zmqConfig;
    private ZContext context;

    private OrderParserWrapper orderParserWrapper;

    public ZmqMarketDataClient(final ZmqConfig zmqConfig) {
        this.zmqConfig = zmqConfig;
    }

    @Override
    public void connect(final MarketDataListener listener) {
        context = new ZContext();

        WebClient webClient = WebClient.create("http://" + zmqConfig.getHostname() + ":" + zmqConfig.getPort());

        // TODO HATEOAS
        Integer bookPort = webClient.get()
                .uri("/ports/order")
                .retrieve()
                .bodyToMono(Integer.class)
                .block(Duration.ofSeconds(10));

        Integer exceptionPort = webClient.get()
                .uri("/ports/exception")
                .retrieve()
                .bodyToMono(Integer.class)
                .block(Duration.ofSeconds(10));

        orderSocket = createZmqSubSocket(bookPort);
        exceptionSocket = createZmqSubSocket(exceptionPort);

        // setup parsers
        orderParserWrapper = new OrderParserWrapper();
        orderParserWrapper.setOrderBookCallback(listener::onBook);
        orderParserWrapper.setTradeCallback(listener::onTrade);

        // connect to exception topic
        subscribeOnSocket(exceptionSocket, "", listener::onError);
    }

    private ZMQ.Socket createZmqSubSocket(final Integer port) {
        ZMQ.Socket socket = context.createSocket(SocketType.SUB);
        socket.setCurveServerKey(hexStringToByteArray(zmqConfig.getServerPublicKey())); // server public key
        socket.setCurvePublicKey(hexStringToByteArray(zmqConfig.getClientPublicKey())); // client public key
        socket.setCurveSecretKey(hexStringToByteArray(zmqConfig.getClientPrivateKey())); // client private key
        socket.connect(TCP_SCHEMA + "://" + zmqConfig.getHostname() + ":" + port);
        return socket;
    }

    @Override
    public void subscribe(final String symbol) {
        subscribeOnSocket(orderSocket, symbol, orderParserWrapper.subscribe(symbol));
    }

    private void subscribeOnSocket(final ZMQ.Socket socket, final String symbol, final Consumer<String> consumer) {
        socket.subscribe(symbol);
        new Thread(() -> {
            while (true) {
                consumer.accept(socket.recvStr());
            }
        }).start();
    }

    @Override
    public void close() {
        context.close();
    }
}
