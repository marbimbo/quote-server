package org.misio.client.impl;

public class ZmqConfig {

    private String hostname;
    private int port;

    // CURVE
    private boolean isCurveEnabled;
    private String serverPublicKey;
    private String clientPublicKey;
    private String clientPrivateKey;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isCurveEnabled() {
        return isCurveEnabled;
    }

    public void setCurveEnabled(boolean curveEnabled) {
        isCurveEnabled = curveEnabled;
    }

    public String getServerPublicKey() {
        return serverPublicKey;
    }

    public void setServerPublicKey(String serverPublicKey) {
        this.serverPublicKey = serverPublicKey;
    }

    public String getClientPublicKey() {
        return clientPublicKey;
    }

    public void setClientPublicKey(String clientPublicKey) {
        this.clientPublicKey = clientPublicKey;
    }

    public String getClientPrivateKey() {
        return clientPrivateKey;
    }

    public void setClientPrivateKey(String clientPrivateKey) {
        this.clientPrivateKey = clientPrivateKey;
    }
}
