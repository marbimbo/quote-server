package org.misio.consumer.consumer;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import org.misio.config.BenchmarkConfig;
import org.misio.consumer.config.datastore.DatastoreConfig;
import org.misio.consumer.config.qs.QuoteServerConfig;
import org.misio.consumer.config.qs.TopicSecurity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;

import static org.misio.config.CurveEncryptUtil.hexStringToByteArray;

public class RecordingConsumer implements Consumer {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private InfluxDBClient client;

    private QuoteServerConfig quoteServerConfig;
    private TopicSecurity topicSecurity;
    private DatastoreConfig datastoreConfig;
    private BenchmarkConfig benchmarkConfig;

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
        try (ZContext context = new ZContext()) {
            WriteApi writeApi = client.getWriteApi();
            startConsumer(context, writeApi, quoteServerConfig.getPort());
        }
    }

    private void startConsumer(ZContext context, WriteApi writeApi, final int port) {
        LOG.info("Connecting to topics on port {}", port);

        //  Socket to talk to server
        ZMQ.Socket socket = context.createSocket(SocketType.SUB);
        String serverPublicKey = topicSecurity.getServerConfig().getPublicKey();
        String clientPublicKey = topicSecurity.getClientConfig().getPublicKey();
        String clientPrivateKey = topicSecurity.getClientConfig().getPrivateKey();
        socket.setCurveServerKey(hexStringToByteArray(serverPublicKey)); // server public key
        socket.setCurvePublicKey(hexStringToByteArray(clientPublicKey)); // client public key
        socket.setCurveSecretKey(hexStringToByteArray(clientPrivateKey)); // client private key
        socket.connect(quoteServerConfig.getSchema() + "://" + quoteServerConfig.getHostname() + ":" + port);
        socket.subscribe(""); // subscribe to all symbols
        while (true) {
            String stringRecord = socket.recvStr();
            LOG.debug(stringRecord);
            if (datastoreConfig.isEnabled()) {
                if (benchmarkConfig.isDeltaEnabled()) {
                    String benchmarkRecord = stringRecord.replace("<placeholder>", String.valueOf(System.nanoTime()));
                    writeApi.writeRecord(datastoreConfig.getBucket(), datastoreConfig.getOrg(), WritePrecision.NS, benchmarkRecord);
                } else {
                    writeApi.writeRecord(datastoreConfig.getBucket(), datastoreConfig.getOrg(), WritePrecision.NS, stringRecord);
                }
            }
        }
    }
}
