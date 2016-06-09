package cz.jskrabal.proxy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by janskrabal on 04/06/16.
 */
public class NetworkSettings {
    @JsonProperty
    private String host;

    @JsonProperty
    private Integer port;

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
