package cz.jskrabal.proxy.config;

import org.junit.Test;
import org.junit.runner.RunWith;

import cz.jskrabal.proxy.config.enums.ConfigurationParameter;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class ProxyConfigurationStreamsTest extends AbstractConfigurationTest {

	private static final int TIMEOUT = 1234;

	@SuppressWarnings("unused")
	private static Object[] provideDefaultValueTestParams() {
		return new Object[] {
				new Object[] { ConfigurationParameter.STREAM_DOWNSTREAM_CONNECTION_TIMEOUT_MILLIS },
				new Object[] { ConfigurationParameter.STREAM_DOWNSTREAM_IDLE_TIMEOUT_MILLIS },
				new Object[] { ConfigurationParameter.STREAM_UPSTREAM_IDLE_TIMEOUT_MILLIS },
				new Object[] { ConfigurationParameter.STREAM_DOWNSTREAM_REQUEST_TIMEOUT_MILLIS },
				new Object[] { ConfigurationParameter.STREAM_DOWNSTREAM_DEBUG_LOGGING },
				new Object[] { ConfigurationParameter.STREAM_UPSTREAM_DEBUG_LOGGING }
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
				new Object[] { ConfigurationParameter.STREAM_DOWNSTREAM_CONNECTION_TIMEOUT_MILLIS, TIMEOUT },
				new Object[] { ConfigurationParameter.STREAM_DOWNSTREAM_IDLE_TIMEOUT_MILLIS, TIMEOUT },
				new Object[] { ConfigurationParameter.STREAM_UPSTREAM_IDLE_TIMEOUT_MILLIS, TIMEOUT },
				new Object[] { ConfigurationParameter.STREAM_DOWNSTREAM_REQUEST_TIMEOUT_MILLIS, TIMEOUT },
				new Object[] { ConfigurationParameter.STREAM_DOWNSTREAM_DEBUG_LOGGING, true },
				new Object[] { ConfigurationParameter.STREAM_UPSTREAM_DEBUG_LOGGING, true }

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
				new Object[] { ConfigurationParameter.STREAM_DOWNSTREAM_CONNECTION_TIMEOUT_MILLIS,
						INVALID_STRING_VALUE },
				new Object[] { ConfigurationParameter.STREAM_DOWNSTREAM_IDLE_TIMEOUT_MILLIS, INVALID_STRING_VALUE },
				new Object[] { ConfigurationParameter.STREAM_UPSTREAM_IDLE_TIMEOUT_MILLIS, INVALID_STRING_VALUE },
				new Object[] { ConfigurationParameter.STREAM_DOWNSTREAM_DEBUG_LOGGING, INVALID_STRING_VALUE },
				new Object[] { ConfigurationParameter.STREAM_DOWNSTREAM_REQUEST_TIMEOUT_MILLIS, INVALID_STRING_VALUE },
				new Object[] { ConfigurationParameter.STREAM_UPSTREAM_DEBUG_LOGGING, INVALID_STRING_VALUE }
		};
	}

	@Test
	@Parameters(method = "provideInvalidValueTestParams")
	public void testInvalidValue(ConfigurationParameter param, Object invalidValue) {
		addValueToConfig(param, invalidValue);
		assertDefaultValue(param);
	}
}
