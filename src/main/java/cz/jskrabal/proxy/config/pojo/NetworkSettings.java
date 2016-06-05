package cz.jskrabal.proxy.config.pojo;

/**
 * Created by janskrabal on 04/06/16.
 */
public class NetworkSettings {
    private String host;
    private Integer port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
