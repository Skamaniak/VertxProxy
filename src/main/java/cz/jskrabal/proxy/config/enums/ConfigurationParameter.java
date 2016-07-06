package cz.jskrabal.proxy.config.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import cz.jskrabal.proxy.dto.NetworkSettings;
import cz.jskrabal.proxy.util.ValidationUtils;

/**
 * Created by janskrabal on 04/06/16.
 */
public enum ConfigurationParameter implements Validable<Object> {

	/* Basic settings */
	NETWORK_PORT(Arrays.asList("network", "port"), Integer.class, 8080) {
		@Override
		public boolean validate(Object value) {
			return getType().isInstance(value) && ValidationUtils.validatePort(value);
		}
	},
	NETWORK_HOST(Arrays.asList("network", "host"), String.class, "0.0.0.0"),
	ID_GENERATOR("idGenerator", String.class, IdGeneratorType.RANDOM.name()) {
		@Override
		public boolean validate(Object value) {
			return ValidationUtils.validateOptionalEnumOption(value, IdGeneratorType.class);
		}
	},
	NETWORK_DEBUG_LOGGING(Arrays.asList("network", "debugLogging"), Boolean.class, false),

	/* Proxy chaining */
	NEXT_HTTP_PROXY("nextHttpProxy", NetworkSettings.class, null) {
		@Override
		public boolean validate(Object value) {
			return ValidationUtils.validateOptionalNetworkSettings(value);
		}
	},
	NEXT_TUNNEL_PROXY("nextTunnelProxy", NetworkSettings.class, null) {
		@Override
		public boolean validate(Object value) {
			return ValidationUtils.validateOptionalNetworkSettings(value);
		}
	},

	/* Custom headers */
	CUSTOM_REQUEST_HEADERS(Arrays.asList("customHeaders", "appendToRequest"), Map.class, Collections.emptyMap()) {
		@Override
		public boolean validate(Object value) {
			return ValidationUtils.validateCustomHeaders(value);
		}
	},
	CUSTOM_RESPONSE_HEADERS(Arrays.asList("customHeaders", "appendToResponse"), Map.class, Collections.emptyMap()) {
		@Override
		public boolean validate(Object value) {
			return ValidationUtils.validateCustomHeaders(value);
		}
	},
	REMOVE_REQUEST_HEADERS(Arrays.asList("customHeaders", "removeFromRequest"), Set.class, Collections.emptySet()) {
		@Override
		public boolean validate(Object value) {
			return ValidationUtils.validateOptionalSetOfTypes(value, String.class);
		}
	},
	REMOVE_RESPONSE_HEADERS(Arrays.asList("customHeaders", "removeFromRespobse"), Set.class, Collections.emptySet()) {
		@Override
		public boolean validate(Object value) {
			return ValidationUtils.validateOptionalSetOfTypes(value, String.class);
		}
	},
	ADD_TRANSFER_ID_HEADER(Arrays.asList("customHeaders", "addTransferIdHeader"), Boolean.class, false),
	ADD_FORWARDED_FOR_HEADERS(Arrays.asList("customHeaders", "addForwardedForHeaders"), Boolean.class, false),
	ADD_FORWARDED_BY_HEADERS(Arrays.asList("customHeaders", "addForwardedByHeaders"), Boolean.class, false);

	private List<String> compositionKey;
	private Object defaultValue;
	private Class type;

	ConfigurationParameter(List<String> compositionKey, Class type, Object defaultValue) {
		this.defaultValue = defaultValue;
		this.type = type;
		this.compositionKey = compositionKey;
	}

	ConfigurationParameter(String key, Class type, Object defaultValue) {
		this(Arrays.asList(key), type, defaultValue);
	}

	@Override
	public Class getType() {
		return type;
	}

	public String getFullJsonKey() {
		StringJoiner keyComposer = new StringJoiner(".");
		compositionKey.forEach(keyComposer::add);
		return keyComposer.toString();
	}

	public List<String> getJsonKeyParts() {
		return Collections.unmodifiableList(compositionKey);
	}

	public Object getDefaultValue() {
		return defaultValue;
	}
}
