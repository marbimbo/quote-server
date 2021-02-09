package org.misio.consumer.config.datastore;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("datastore")
public class DatastoreConfig {

    private String bucket;
    private String org;
    private String url;
    private String token;
    private boolean enabled;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
