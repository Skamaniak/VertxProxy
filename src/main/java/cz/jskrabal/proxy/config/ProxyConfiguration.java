package cz.jskrabal.proxy.config;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.jskrabal.proxy.config.enums.ConfigurationParameter;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by janskrabal on 01/06/16.
 */
public class ProxyConfiguration {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProxyConfiguration.class);
	private JsonObject config;

	public ProxyConfiguration(JsonObject config) {
		this.config = config;
		checkConfig(config);
	}

	private void checkConfig(JsonObject config) {
		for (ConfigurationParameter parameter : ConfigurationParameter.values()) {
			Optional<Object> value = getValue(config, parameter.getJsonKeyParts());
			if (value.isPresent()) {
				Object trueValue = toPojoIfNeeded(parameter.getType(), value.get());
				if (!parameter.validate(trueValue)) {
					LOGGER.warn("Parameter '{}' contains invalid value '{}'. Parameter will be ignored.",
							parameter.getFullJsonKey(), trueValue);
				}
			}
		}
	}

	public <T> T getValue(ConfigurationParameter param, Class<T> type) {
		return getValue(config, param, type);
	}

	public static <T> T getValue(JsonObject config, ConfigurationParameter param, Class<T> type) {
		Optional<Object> valueCandidate = getValue(config, param.getJsonKeyParts());
		Object value = valueCandidate.orElse(param.getDefaultValue());
		value = toPojoIfNeeded(type, value);

		if (type.isAssignableFrom(param.getType())) {
			if (!param.validate(value)) {
				value = param.getDefaultValue();
			}

			@SuppressWarnings("unchecked")
			T castedVal = (T) value;

			return castedVal;
		}
		throw new ClassCastException("Required type " + type + " is not compatible with the type " + param.getType()
				+ " of parameter" + param);
	}

	private static <T> Object toPojoIfNeeded(Class<T> type, Object value) {
		if (value instanceof JsonObject || value instanceof JsonArray) {
			value = Json.decodeValue(value.toString(), type);
		}
		return value;
	}

	private static Optional<Object> getValue(JsonObject config, List<String> key) {
		Object value = config;
		for (String keyPart : key) {
			if (value == null) {
				return Optional.empty();
			}
			if (value instanceof JsonObject) {
				value = ((JsonObject) value).getValue(keyPart);
			}
		}
		return value == null ? Optional.empty() : Optional.of(value);
	}

	public boolean isDownstreamDebugLoggingEnabled() {
		return getValue(ConfigurationParameter.STREAM_DOWNSTREAM_DEBUG_LOGGING, Boolean.class);
	}

	public boolean isUpstreamDebugLoggingEnabled() {
		return getValue(ConfigurationParameter.STREAM_UPSTREAM_DEBUG_LOGGING, Boolean.class);
	}

	public int getUpstreamIdleTimeout() {
		return getValue(ConfigurationParameter.STREAM_UPSTREAM_IDLE_TIMEOUT_MILLIS, Integer.class);
	}

	public int getDownstreamIdleTimeout() {
		return getValue(ConfigurationParameter.STREAM_DOWNSTREAM_IDLE_TIMEOUT_MILLIS, Integer.class);
	}

	public int getDownstreamConnectionTimeout() {
		return getValue(ConfigurationParameter.STREAM_DOWNSTREAM_CONNECTION_TIMEOUT_MILLIS, Integer.class);
	}

	public int getDownstreamRequestTimeout() {
		return getValue(ConfigurationParameter.STREAM_DOWNSTREAM_REQUEST_TIMEOUT_MILLIS, Integer.class);
	}

	public int getProxyPort() {
		return getValue(ConfigurationParameter.NETWORK_PORT, Integer.class);
	}

	public String getProxyHost() {
		return getValue(ConfigurationParameter.NETWORK_HOST, String.class);
	}

}
