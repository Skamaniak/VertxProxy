package cz.jskrabal.proxy.config;

import cz.jskrabal.proxy.config.enums.ConfigurationParameter;
import cz.jskrabal.proxy.dto.NetworkSettings;
import org.junit.Test;

/**
 * Created by janskrabal on 11/06/16.
 */
public class ProxyConfigurationProxyChainingTest extends AbstractConfigurationTest{

    @Test
    public void getNextHttpProxyTest() {
        NetworkSettings networkSettings = new NetworkSettings(LOCALHOST_IP, HTTP_RESERVED_PORT);
        addPojoToConfig(ConfigurationParameter.NEXT_HTTP_PROXY, networkSettings);

        assertConfigValue(ConfigurationParameter.NEXT_HTTP_PROXY, networkSettings);
    }

    @Test
    public void getNextHttpProxyDefaultValueTest() {
        assertDefaultValue(ConfigurationParameter.NEXT_HTTP_PROXY);
    }

    @Test
    public void getNextHttpProxyInvalidHostTest() {
        NetworkSettings networkSettings = new NetworkSettings(null, HTTP_RESERVED_PORT);
        addPojoToConfig(ConfigurationParameter.NEXT_HTTP_PROXY, networkSettings);

        assertDefaultValue(ConfigurationParameter.NEXT_HTTP_PROXY);
    }

    @Test
    public void getNextHttpProxyInvalidPortTest() {
        NetworkSettings networkSettings = new NetworkSettings(LOCALHOST_IP, INVALID_NUMBER_VALUE);
        addPojoToConfig(ConfigurationParameter.NEXT_HTTP_PROXY, networkSettings);

        assertDefaultValue(ConfigurationParameter.NEXT_HTTP_PROXY);
    }

    @Test
    public void getNextHttpProxyInvalidValueTest() {
        addValueToConfig(ConfigurationParameter.NEXT_HTTP_PROXY, INVALID_STRING_VALUE);

        assertDefaultValue(ConfigurationParameter.NEXT_HTTP_PROXY);
    }

    @Test
    public void getNextTunnelProxyTest() {
        NetworkSettings networkSettings = new NetworkSettings(LOCALHOST_IP, HTTP_RESERVED_PORT);
        addPojoToConfig(ConfigurationParameter.NEXT_TUNNEL_PROXY, networkSettings);

        assertConfigValue(ConfigurationParameter.NEXT_TUNNEL_PROXY, networkSettings);
    }

    @Test
    public void getNextTunnelProxyDefaultValueTest() {
        assertDefaultValue(ConfigurationParameter.NEXT_TUNNEL_PROXY);
    }

    @Test
    public void getNextTunnelProxyInvalidHostTest() {
        NetworkSettings networkSettings = new NetworkSettings(null, HTTP_RESERVED_PORT);
        addPojoToConfig(ConfigurationParameter.NEXT_TUNNEL_PROXY, networkSettings);

        assertDefaultValue(ConfigurationParameter.NEXT_TUNNEL_PROXY);
    }

    @Test
    public void getNextTunnelProxyInvalidPortTest() {
        NetworkSettings networkSettings = new NetworkSettings(LOCALHOST_IP, INVALID_NUMBER_VALUE);
        addPojoToConfig(ConfigurationParameter.NEXT_TUNNEL_PROXY, networkSettings);

        assertDefaultValue(ConfigurationParameter.NEXT_TUNNEL_PROXY);
    }

    @Test
    public void getNextTunnelProxyInvalidValueTest() {
        addValueToConfig(ConfigurationParameter.NEXT_TUNNEL_PROXY, INVALID_STRING_VALUE);

        assertDefaultValue(ConfigurationParameter.NEXT_TUNNEL_PROXY);
    }

}
