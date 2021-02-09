package org.misio.consumer.consumer;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import org.misio.consumer.config.datastore.DatastoreConfig;
import org.misio.consumer.config.qs.BenchmarkConfig;
import org.misio.consumer.config.qs.QuoteServerConfig;
import org.misio.consumer.config.qs.TopicSecurity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class RecordingConsumer implements Consumer {

    private static final Logger LOG = LoggerFactory.getLogger(RecordingConsumer.class);
    private static final int THREAD_COUNT = 64;

    private String[] productIds;
    private InfluxDBClient client;

    private QuoteServerConfig quoteServerConfig;
    private TopicSecurity topicSecurity;
    private DatastoreConfig datastoreConfig;
    private BenchmarkConfig benchmarkConfig;

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] ba = new byte[len / 2];

        for (int i = 0; i < ba.length; i++) {
            int j = i * 2;
            int t = Integer.parseInt(s.substring(j, j + 2), 16);
            byte b = (byte) (t & 0xFF);
            ba[i] = b;
        }
        return ba;
    }

    @Value("${recording.productIds}")
    public void setProductIds(String[] productIds) {
        this.productIds = productIds;
    }

    @Autowired
    public void setQuoteServerConfig(QuoteServerConfig quoteServerConfig) {
        this.quoteServerConfig = quoteServerConfig;
    }

    @Autowired
    public void setTopicSecurity(TopicSecurity topicSecurity) {
        this.topicSecurity = topicSecurity;
    }

    @Autowired
    public void setDatastoreConfig(DatastoreConfig datastoreConfig) {
        this.datastoreConfig = datastoreConfig;
    }

    @Autowired
    public void setBenchmarkConfig(BenchmarkConfig benchmarkConfig) {
        this.benchmarkConfig = benchmarkConfig;
    }

    @PostConstruct
    private void initClient() {
        client = InfluxDBClientFactory.create(datastoreConfig.getUrl(), datastoreConfig.getToken().toCharArray());
        startConsuming();
    }

    @Override
    public void startConsuming() {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        try (ZContext context = new ZContext()) {
            WriteApi writeApi = client.getWriteApi();
            Arrays.stream(productIds).forEach(productId -> {
                final int zPort = quoteServerConfig.getPort();
                executorService.execute(() ->
                        startConsumer(productId, context, writeApi, zPort));
                quoteServerConfig.setPort(zPort + 1);
            });
        }
    }

    private void startConsumer(String productId, ZContext context, WriteApi writeApi, int zPort) {
        System.out.println("Connecting to hello world server on topic : " + productId + " and port " + zPort);

        //  Socket to talk to server
        ZMQ.Socket socket = context.createSocket(SocketType.SUB);
        String serverPublicKey = topicSecurity.getServerConfig().getPublicKey();
        String clientPublicKey = topicSecurity.getClientConfig().getPublicKey();
        String clientPrivateKey = topicSecurity.getClientConfig().getPrivateKey();
        socket.setCurveServerKey(hexStringToByteArray(serverPublicKey)); // server public key
        socket.setCurvePublicKey(hexStringToByteArray(clientPublicKey)); // client public key
        socket.setCurveSecretKey(hexStringToByteArray(clientPrivateKey)); // client private key
        socket.connect(quoteServerConfig.getSchema() + "://" + quoteServerConfig.getHostname() + ":" + zPort);
        socket.subscribe(productId);
        int counter = 0;
        long start = System.currentTimeMillis();
//        while (counter < 2000) {
        while (true) {
            String stringRecord = socket.recvStr();
//            System.out.println("Received " + stringRecord);
//            System.out.println(System.nanoTime());
            if (datastoreConfig.isEnabled()) { // TODO: 05.02.2021 add buffering
                if (benchmarkConfig.isDeltaEnabled()) {
                    String benchmarkRecord = stringRecord.replace("<placeholder>", String.valueOf(System.nanoTime()));
                    writeApi.writeRecord(datastoreConfig.getBucket(), datastoreConfig.getOrg(), WritePrecision.NS, benchmarkRecord);
                } else {
                    writeApi.writeRecord(datastoreConfig.getBucket(), datastoreConfig.getOrg(), WritePrecision.NS, stringRecord);
                }
            }
            ++counter;
        }
//        System.out.println(1000 * counter / (System.currentTimeMillis() - start));
    }
}
