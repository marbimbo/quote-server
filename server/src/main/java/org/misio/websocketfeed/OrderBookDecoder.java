package org.misio.websocketfeed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.misio.websocketfeed.message.OrderMessage;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class OrderBookDecoder implements Decoder.Text<OrderMessage> {

    private ObjectMapper objectMapper;

    @Override
    public OrderMessage decode(String message) throws DecodeException {
        try {
            return objectMapper.readValue(message, OrderMessage.class);
        } catch (JsonProcessingException e) {
            throw new DecodeException(message, "not instance of OrderMessage");
        }
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
        objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void destroy() {
        // empty
    }
}
