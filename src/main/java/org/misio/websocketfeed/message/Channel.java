package org.misio.websocketfeed.message;

public class Channel {

    String name;
    String[] product_ids;

    public void setName(String name) {
        this.name = name;
    }

    public void setProduct_ids(String[] product_ids) {
        this.product_ids = product_ids;
    }
}
