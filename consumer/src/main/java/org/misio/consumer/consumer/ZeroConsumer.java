package org.misio.consumer.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.annotation.PostConstruct;

//@Component
public class ZeroConsumer implements Consumer {

    private int port;

    private String topic;

    private String schema;

    private String hostname;
    private boolean isRecording;

//    private InfluxDB influxDB = InfluxDBFactory.connect(databaseURL, userName, password);

    @Value("${port}")
    public void setPort(int port) {
        this.port = port;
    }

    @Value("${topic}")
    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Value("${schema}")
    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Value("${hostname}")
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Value("${isEnabled}")
    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    @Override
    @PostConstruct
    public void startConsuming() {
        try (ZContext context = new ZContext()) {
            System.out.println("Connecting to hello world server");

            //  Socket to talk to server
            ZMQ.Socket socket = context.createSocket(SocketType.SUB);
            socket.connect(schema + "://" + hostname + ":" + port);
            socket.subscribe(topic);

            int counter = 0;
            long start = System.currentTimeMillis();
            while (counter < 2000) {
//            for (int requestNbr = 0; requestNbr != 10; requestNbr++) {

//                System.out.println("Receiving Hello ");
//                socket.send(request.getBytes(ZMQ.CHARSET), 0);

                byte[] record = socket.recv(0);
                if (isRecording) {

                }
                ++counter;
//                System.out.println("Received " + Arrays.toString(reply));
//                System.out.println(new String(reply, ZMQ.CHARSET));
//                System.out.println(new String(reply, ZMQ.CHARSET) + " : " + System.currentTimeMillis());
//                System.out.println(1000 * counter / (System.currentTimeMillis() - start));
            }
            System.out.println(1000 * counter / (System.currentTimeMillis() - start));
        }
    }
}
