package cz.jskrabal.proxy.config;

import cz.jskrabal.proxy.config.enums.ConfigurationParameter;
import cz.jskrabal.proxy.config.enums.IdGeneratorType;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created by janskrabal on 10/06/16.
 */
public class ProxyConfigurationBasicTest extends AbstractConfigurationTest {

	@Test
	public void initProxyConfigWithExistingValidJson(){
		addValueToConfig(ConfigurationParameter.NETWORK_PORT, HTTP_RESERVED_PORT);
		ProxyConfiguration proxyConfiguration = new ProxyConfiguration(config);

		int port = proxyConfiguration.getValue(ConfigurationParameter.NETWORK_PORT, Integer.class);
		assertEquals(HTTP_RESERVED_PORT, port);
	}

	@Test
	public void initProxyConfigWithExistingInvalidJson(){
		addValueToConfig(ConfigurationParameter.NETWORK_PORT, INVALID_STRING_VALUE);
		ProxyConfiguration proxyConfiguration = new ProxyConfiguration(config);

		int port = proxyConfiguration.getValue(ConfigurationParameter.NETWORK_PORT, Integer.class);
		assertEquals(ConfigurationParameter.NETWORK_PORT.getDefaultValue(), port);
	}

	@Test
	public void getNetworkPortTest() {
		addValueToConfig(ConfigurationParameter.NETWORK_PORT, HTTP_RESERVED_PORT);
		assertConfigValue(ConfigurationParameter.NETWORK_PORT, HTTP_RESERVED_PORT);
	}

	@Test
	public void getNetworkPortDefaultValueTest() {
		assertDefaultValue(ConfigurationParameter.NETWORK_PORT);
	}

	@Test
	public void getNetworkPortInvalidValueTest() {
		addValueToConfig(ConfigurationParameter.NETWORK_PORT, INVALID_STRING_VALUE);
		assertDefaultValue(ConfigurationParameter.NETWORK_PORT);
	}

	@Test
	public void getNetworkPortInvalidPortNumberTest() {
		addValueToConfig(ConfigurationParameter.NETWORK_PORT, INVALID_NUMBER_VALUE);
		assertDefaultValue(ConfigurationParameter.NETWORK_PORT);
	}

	@Test
	public void getNetworkHostTest() {
		addValueToConfig(ConfigurationParameter.NETWORK_HOST, LOCALHOST_IP);
		assertConfigValue(ConfigurationParameter.NETWORK_HOST, LOCALHOST_IP);
	}

	@Test
	public void getNetworkHostDefaultValueTest() {
		assertDefaultValue(ConfigurationParameter.NETWORK_HOST);
	}

	@Test
	public void getNetworkHostInvalidValueTest() {
		addValueToConfig(ConfigurationParameter.NETWORK_HOST, INVALID_NUMBER_VALUE);
		assertDefaultValue(ConfigurationParameter.NETWORK_HOST);
	}

	@Test
	public void getIdGeneratorTest() {
		addValueToConfig(ConfigurationParameter.ID_GENERATOR, IdGeneratorType.SEQUENCE.name());
		assertConfigValue(ConfigurationParameter.ID_GENERATOR, IdGeneratorType.SEQUENCE.name());
	}

	@Test
	public void getIdGeneratorDefaultValueTest() {
		assertDefaultValue(ConfigurationParameter.ID_GENERATOR);
	}

	@Test
	public void getIdGeneratorInvalidValueTest() {
		addValueToConfig(ConfigurationParameter.NETWORK_HOST, INVALID_NUMBER_VALUE);
		assertDefaultValue(ConfigurationParameter.ID_GENERATOR);
	}

	@Test(expected = ClassCastException.class)
	public void getConfigValueTypeMismatchTest() {
		proxyConfig.getValue(ConfigurationParameter.NETWORK_PORT, String.class);
	}

}
