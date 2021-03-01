package org.misio.client.model;

public enum OrderMessageType {

    RECEIVED("received"),
    OPEN("open"),
    MATCH("match"),
    CANCEL("cancel"),
    DONE("done"),
    CHANGE("change");

    private final String type;

    OrderMessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
