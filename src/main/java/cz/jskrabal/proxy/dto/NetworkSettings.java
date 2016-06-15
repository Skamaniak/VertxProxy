package cz.jskrabal.proxy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Created by janskrabal on 04/06/16.
 */
public class NetworkSettings {
    @JsonProperty
    private String host;

    @JsonProperty
    private Integer port;

    public NetworkSettings() {}

    public NetworkSettings(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        NetworkSettings other = (NetworkSettings) obj;
        return Objects.equals(host, other.host) &&
                Objects.equals(port, other.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
