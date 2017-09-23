package cz.jskrabal.proxy.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import cz.jskrabal.proxy.config.enums.ConfigurationParameter;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Created by janskrabal on 11/06/16.
 */
@RunWith(JUnitParamsRunner.class)
public class ProxyConfigurationHeadersTest extends AbstractConfigurationTest {
	private static final Map<String, String> CUSTOM_HEADERS;

	static {
		Map<String, String> headers = new HashMap<>();
		headers.put("Via", "VertxProxy");
		headers.put("X-Test-Header", "test value");
		CUSTOM_HEADERS = Collections.unmodifiableMap(headers);
	}

	@SuppressWarnings("unused")
	private static Object[] provideDefaultValueTestParams() {
		return new Object[] {
				new Object[] { ConfigurationParameter.ADD_FORWARDED_BY_HEADERS },
				new Object[] { ConfigurationParameter.ADD_FORWARDED_FOR_HEADERS },
				new Object[] { ConfigurationParameter.ADD_TRANSFER_ID_HEADER },
				new Object[] { ConfigurationParameter.REMOVE_RESPONSE_HEADERS },
				new Object[] { ConfigurationParameter.REMOVE_REQUEST_HEADERS },
				new Object[] { ConfigurationParameter.CUSTOM_RESPONSE_HEADERS },
				new Object[] { ConfigurationParameter.CUSTOM_REQUEST_HEADERS }
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
				new Object[] { ConfigurationParameter.ADD_FORWARDED_BY_HEADERS, true },
				new Object[] { ConfigurationParameter.ADD_FORWARDED_FOR_HEADERS, true },
				new Object[] { ConfigurationParameter.ADD_TRANSFER_ID_HEADER, true }
		};
	}

	@Test
	@Parameters(method = "provideSetAndGetTestParams")
	public void testSetAndGet(ConfigurationParameter param, Object value) {
		addValueToConfig(param, value);
		assertConfigValue(param, value);
	}

	@SuppressWarnings("unused")
	private static Object[] provideSetAndGetPojoTestParams() {
		return new Object[] {
				new Object[] { ConfigurationParameter.REMOVE_RESPONSE_HEADERS, CUSTOM_HEADERS.keySet() },
				new Object[] { ConfigurationParameter.REMOVE_REQUEST_HEADERS, CUSTOM_HEADERS.keySet() },
				new Object[] { ConfigurationParameter.CUSTOM_RESPONSE_HEADERS, CUSTOM_HEADERS },
				new Object[] { ConfigurationParameter.CUSTOM_REQUEST_HEADERS, CUSTOM_HEADERS }
		};
	}

	@Test
	@Parameters(method = "provideSetAndGetPojoTestParams")
	public void testSetAndGetPojo(ConfigurationParameter param, Object value) {
		addPojoToConfig(param, value);
		assertConfigValue(param, value);
	}

	@SuppressWarnings("unused")
	private static Object[] provideInvalidValueTestParams() {
		return new Object[] {
				new Object[] { ConfigurationParameter.ADD_FORWARDED_BY_HEADERS, INVALID_NUMBER_VALUE },
				new Object[] { ConfigurationParameter.ADD_FORWARDED_FOR_HEADERS, INVALID_NUMBER_VALUE },
				new Object[] { ConfigurationParameter.ADD_TRANSFER_ID_HEADER, INVALID_NUMBER_VALUE },
				new Object[] { ConfigurationParameter.REMOVE_RESPONSE_HEADERS, INVALID_NUMBER_VALUE },
				new Object[] { ConfigurationParameter.REMOVE_REQUEST_HEADERS, INVALID_NUMBER_VALUE },
				new Object[] { ConfigurationParameter.CUSTOM_RESPONSE_HEADERS, INVALID_NUMBER_VALUE },
				new Object[] { ConfigurationParameter.CUSTOM_REQUEST_HEADERS, INVALID_NUMBER_VALUE }
		};
	}

	@Test
	@Parameters(method = "provideInvalidValueTestParams")
	public void testInvalidValue(ConfigurationParameter param, Object invalidValue) {
		addValueToConfig(param, invalidValue);
		assertDefaultValue(param);
	}
}
