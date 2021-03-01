# quote-server
## Quote Server

+ **server** - Quote Server application
+ **consumer** - consumer sample applications that use InfluxDB (RecordingConsumer.java) and Market Data Client (ClientConsumer.java)
+ **marketdata-client** - API that provides client which can be used to receive market data updates
+ **config** - common configuration files

## Running application

In order to start QS we need to run main class org.misio.QuoteServerApp.

Parameters to override:
+ `orderPort` - port used by ZeroMQ to publish records
+ `exceptionPort` - port used by ZeroMQ to publish error messages (for instance after closing
socket abnormally)
+ `server.port` - port used by Spring Boot to start Tomcat server
+ `productIds` – list of symbols used in single Coinbase subscription; static list provided in
application-common.yml consists of 72 symbols

In order to start recording application we need to run main class org.misio.consumer.ConsumerApp .

Parameters to override:
+ `quoteServer.orderPort` - port used by ZeroMQ to publish records
+ `quoteServer.exceptionPort` - port used by ZeroMQ to publish error messages (for instance
after closing socket abnormally)
+ `productIds` – list of symbols used in single Coinbase subscription; static list provided in
application-common.yml consists of 72 symbols
+ `datastore.url`, `datastore.token`, `datastore.org`, `datastore.bucket` - InfluxDB bucket details

## Enabling & disabling consumers

Available consumers:
+ **market-data** consumer - uses MarketDataClient to connect with QS
+ **recording** consumer - records incoming Flux records directly in InfluxDB

In order to enable specific consumer its corresponding property `consumers.<consumer-name>` needs to be set to `true` in application.yml or injected.