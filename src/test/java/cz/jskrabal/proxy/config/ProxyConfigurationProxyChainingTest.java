package cz.jskrabal.proxy.config;

import org.junit.Test;
import org.junit.runner.RunWith;

import cz.jskrabal.proxy.config.enums.ConfigurationParameter;
import cz.jskrabal.proxy.dto.NetworkSettings;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Created by janskrabal on 11/06/16.
 */
@RunWith(JUnitParamsRunner.class)
public class ProxyConfigurationProxyChainingTest extends AbstractConfigurationTest {

	@SuppressWarnings("unused")
	private static Object[] provideDefaultValueTestParams() {
		return new Object[] {
				new Object[] { ConfigurationParameter.NEXT_TUNNEL_PROXY },
				new Object[] { ConfigurationParameter.NEXT_HTTP_PROXY }
		};
	}

	@Test
	@Parameters(method = "provideDefaultValueTestParams")
	public void testDefaultValue(ConfigurationParameter param) {
		assertDefaultValue(param);
	}

	@SuppressWarnings("unused")
	private static Object[] provideSetAndGetPojoTestParams() {
		return new Object[] {
				new Object[] { ConfigurationParameter.NEXT_TUNNEL_PROXY,
						new NetworkSettings(LOCALHOST_IP, HTTP_RESERVED_PORT) },
				new Object[] { ConfigurationParameter.NEXT_HTTP_PROXY,
						new NetworkSettings(LOCALHOST_IP, HTTP_RESERVED_PORT) }
		};
	}

	@Test
	@Parameters(method = "provideSetAndGetPojoTestParams")
	public void testSetAndGetPojo(ConfigurationParameter param, Object value) {
		addPojoToConfig(param, value);
		assertConfigValue(param, value);
	}

	@SuppressWarnings("unused")
	private static Object[] provideInvalidPojoValueTestParams() {
		return new Object[] {
				new Object[] { ConfigurationParameter.NEXT_TUNNEL_PROXY,
						new NetworkSettings(null, HTTP_RESERVED_PORT) },
				new Object[] { ConfigurationParameter.NEXT_TUNNEL_PROXY,
						new NetworkSettings(LOCALHOST_IP, INVALID_NUMBER_VALUE) },
				new Object[] { ConfigurationParameter.NEXT_HTTP_PROXY, new NetworkSettings(null, HTTP_RESERVED_PORT) },
				new Object[] { ConfigurationParameter.NEXT_HTTP_PROXY,
						new NetworkSettings(LOCALHOST_IP, INVALID_NUMBER_VALUE) }
		};
	}

	@Test
	@Parameters(method = "provideInvalidPojoValueTestParams")
	public void testInvalidPojoValue(ConfigurationParameter param, Object invalidValue) {
		addPojoToConfig(param, invalidValue);
		assertDefaultValue(param);
	}

	@SuppressWarnings("unused")
	private static Object[] provideInvalidValueTestParams() {
		return new Object[] {
				new Object[] { ConfigurationParameter.NEXT_TUNNEL_PROXY, INVALID_STRING_VALUE },
				new Object[] { ConfigurationParameter.NEXT_HTTP_PROXY, INVALID_STRING_VALUE }
		};
	}

	@Test
	@Parameters(method = "provideInvalidValueTestParams")
	public void testInvalidValue(ConfigurationParameter param, Object invalidValue) {
		addValueToConfig(param, invalidValue);
		assertDefaultValue(param);
	}

}
