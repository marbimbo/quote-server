package org.misio.websocketfeed.message;

public class Subscribe {

    String type;
    Channel[] channels;

    String signature;
    String passphrase;
    String timestamp;
    String apiKey;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Subscribe setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    public Subscribe setPassphrase(String passphrase) {
        this.passphrase = passphrase;
        return this;
    }

    public Subscribe setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Subscribe setKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public Channel[] getChannels() {
        return channels;
    }

    public void setChannels(Channel[] channels) {
        this.channels = channels;
    }
}
