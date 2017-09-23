package cz.jskrabal.proxy.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import cz.jskrabal.proxy.config.enums.ConfigurationParameter;
import cz.jskrabal.proxy.config.enums.IdGeneratorType;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Created by janskrabal on 10/06/16.
 */
@RunWith(JUnitParamsRunner.class)
public class ProxyConfigurationBasicTest extends AbstractConfigurationTest {

	@Test
	public void initProxyConfigWithExistingValidJson() {
		addValueToConfig(ConfigurationParameter.NETWORK_PORT, HTTP_RESERVED_PORT);
		ProxyConfiguration proxyConfiguration = new ProxyConfiguration(config);

		int port = proxyConfiguration.getValue(ConfigurationParameter.NETWORK_PORT, Integer.class);
		assertEquals(HTTP_RESERVED_PORT, port);
	}

	@Test
	public void initProxyConfigWithExistingInvalidJson() {
		addValueToConfig(ConfigurationParameter.NETWORK_PORT, INVALID_STRING_VALUE);
		ProxyConfiguration proxyConfiguration = new ProxyConfiguration(config);

		int port = proxyConfiguration.getValue(ConfigurationParameter.NETWORK_PORT, Integer.class);
		assertEquals(ConfigurationParameter.NETWORK_PORT.getDefaultValue(), port);
	}

	@Test(expected = ClassCastException.class)
	public void getConfigValueTypeMismatchTest() {
		proxyConfig.getValue(ConfigurationParameter.NETWORK_PORT, String.class);
	}

	@SuppressWarnings("unused")
	private static Object[] provideDefaultValueTestParams() {
		return new Object[] {
				new Object[] { ConfigurationParameter.ID_GENERATOR },
				new Object[] { ConfigurationParameter.NETWORK_HOST },
				new Object[] { ConfigurationParameter.NETWORK_PORT }
		};
	}

	@Test
	@Parameters(method = "provideDefaultValueTestParams")
	public void testDefaultValue(ConfigurationParameter param) {
		assertDefaultValue(param);
	}

	@SuppressWarnings("unused")
	private static Object[] provideSetAndGetTestParams() {
		return new Object[] {
				new Object[] { ConfigurationParameter.ID_GENERATOR, IdGeneratorType.SEQUENCE.name() },
				new Object[] { ConfigurationParameter.NETWORK_HOST, LOCALHOST_IP },
				new Object[] { ConfigurationParameter.NETWORK_PORT, HTTP_RESERVED_PORT }
		};
	}

	@Test
	@Parameters(method = "provideSetAndGetTestParams")
	public void testSetAndGet(ConfigurationParameter param, Object value) {
		addValueToConfig(param, value);
		assertConfigValue(param, value);
	}

	@SuppressWarnings("unused")
	private static Object[] provideInvalidValueTestParams() {
		return new Object[] {
				new Object[] { ConfigurationParameter.ID_GENERATOR, INVALID_NUMBER_VALUE },
				new Object[] { ConfigurationParameter.NETWORK_HOST, INVALID_NUMBER_VALUE },
				new Object[] { ConfigurationParameter.NETWORK_PORT, INVALID_STRING_VALUE },
				new Object[] { ConfigurationParameter.NETWORK_PORT, INVALID_NUMBER_VALUE }
		};
	}

	@Test
	@Parameters(method = "provideInvalidValueTestParams")
	public void testInvalidValue(ConfigurationParameter param, Object invalidValue) {
		addValueToConfig(param, invalidValue);
		assertDefaultValue(param);
	}

}
